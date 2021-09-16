package edu.uci.ics.texera.web.resource

import com.github.tototoshi.csv.CSVWriter
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.util.Lists
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.{File, FileList, Permission}
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.{Spreadsheet, SpreadsheetProperties, ValueRange}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.texera.Utils.retry
import edu.uci.ics.texera.web.model.event.ResultExportResponse
import edu.uci.ics.texera.web.model.request.ResultExportRequest
import edu.uci.ics.texera.web.resource.WorkflowWebsocketResource.{
  getExecutionContext,
  getSessionContext,
  getWId
}
import edu.uci.ics.texera.web.resource.auth.UserResource
import edu.uci.ics.texera.web.resource.dashboard.file.UserFileResource
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import org.jooq.types.UInteger
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util
import java.util.concurrent.{Executors, ThreadPoolExecutor}

import javax.servlet.http.HttpSession

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.collection.mutable
object ResultExportResource {

  private final val UPLOAD_BATCH_ROW_COUNT = 10000
  private final val RETRY_ATTEMPTS = 7
  private final val BASE_BACK_OOF_TIME_IN_MS = 1000
  private final val WORKFLOW_RESULT_FOLDER_NAME = "workflow_results"
  private final val pool: ThreadPoolExecutor =
    Executors.newFixedThreadPool(3).asInstanceOf[ThreadPoolExecutor]
  @volatile private var WORKFLOW_RESULT_FOLDER_ID: String = _

  def apply(
      sessionId: String,
      request: ResultExportRequest
  ): ResultExportResponse = {
    // retrieve the file link saved in the session if exists
    val wId = getWId(sessionId).get
    val ec = getExecutionContext(wId).get
    if (ec.exportCache.contains(request.exportType)) {
      return ResultExportResponse(
        "success",
        s"Link retrieved from cache ${ec.exportCache(request.exportType)}"
      )
    }

    // By now the workflow should finish running
    val operatorWithResult: Option[OperatorResultService] =
      ec.resultService.operatorResults.get(request.operatorId)
    if (operatorWithResult.isEmpty) {
      return ResultExportResponse("error", "The workflow contains no results")
    }

    // convert the ITuple into tuple
    val results: List[Tuple] =
      operatorWithResult.get.getResult.map(iTuple => iTuple.asInstanceOf[Tuple])
    val attributeNames = results.head.getSchema.getAttributeNames.asScala.toList

    // handle the request according to export type
    request.exportType match {
      case "google_sheet" =>
        handleGoogleSheetRequest(wId, request, results, attributeNames)
      case "csv" =>
        handleCSVRequest(getSessionContext(sessionId).httpSession, request, results, attributeNames)
      case _ =>
        ResultExportResponse("error", s"Unknown export type: ${request.exportType}")
    }

  }

  def handleCSVRequest(
      httpSession: HttpSession,
      request: ResultExportRequest,
      results: List[Tuple],
      headers: List[String]
  ): ResultExportResponse = {
    val stream = new ByteArrayOutputStream()
    val writer = CSVWriter.open(stream)
    writer.writeRow(headers)
    writer.writeAll(results.map(tuple => tuple.getFields.toList))
    writer.close()
    val fileName = s"${request.workflowName}-${request.operatorId}.csv"
    val uid = UserResource
      .getUser(httpSession)
      .map(u => u.getUid)
      .get
    val fileNameStored = UserFileResource.saveUserFileSafe(
      uid,
      fileName,
      new ByteArrayInputStream(stream.toByteArray),
      UInteger.valueOf(stream.toByteArray.length),
      "generated by workflow"
    )

    ResultExportResponse("success", s"File saved to User Dashboard as $fileNameStored")
  }

  private def handleGoogleSheetRequest(
      wId: String,
      request: ResultExportRequest,
      results: List[ITuple],
      header: List[String]
  ): ResultExportResponse = {
    // create google sheet
    val sheetService: Sheets = GoogleResource.getSheetService
    val sheetId: String =
      createGoogleSheet(sheetService, request.workflowName)
    if (sheetId == null) {
      return ResultExportResponse("error", "Fail to create google sheet")
    }

    val driveService: Drive = GoogleResource.getDriveService
    moveToResultFolder(driveService, sheetId)

    // allow user to access this sheet in the service account
    val sharePermission: Permission = new Permission()
      .setType("anyone")
      .setRole("reader")
    driveService
      .permissions()
      .create(sheetId, sharePermission)
      .execute()

    // upload the content asynchronously to avoid long waiting on the user side.
    pool
      .submit(() =>
        {
          uploadHeader(sheetService, sheetId, header)
          uploadResult(sheetService, sheetId, results)
        }.asInstanceOf[Runnable]
      )

    // generate success response
    val link = s"https://docs.google.com/spreadsheets/d/$sheetId/edit"
    val message: String =
      s"Google sheet created. The results may be still uploading. You can access the sheet $link"
    // save the file link in the session cache
    val ec = getExecutionContext(wId).get
    ec.exportCache(request.exportType) = link
    ResultExportResponse("success", message)
  }

  /**
    * create the google sheet and return the sheet Id
    */
  private def createGoogleSheet(sheetService: Sheets, workflowName: String): String = {
    val createSheetRequest = new Spreadsheet()
      .setProperties(new SpreadsheetProperties().setTitle(workflowName))
    val targetSheet: Spreadsheet = sheetService.spreadsheets
      .create(createSheetRequest)
      .setFields("spreadsheetId")
      .execute
    targetSheet.getSpreadsheetId
  }

  /**
    * move the workflow results to a specific folder
    */
  @tailrec
  private def moveToResultFolder(
      driveService: Drive,
      sheetId: String,
      retry: Boolean = true
  ): Unit = {
    try {
      driveService
        .files()
        .update(sheetId, null)
        .setAddParents(WORKFLOW_RESULT_FOLDER_ID)
        .execute()
    } catch {
      case exception: GoogleJsonResponseException =>
        if (retry) {
          // This exception maybe caused by the full deletion of the target folder and
          // the cached folder id is obsolete.
          //  * note: by full deletion, the folder has to be deleted from trash as well.
          // In this case, retrieve the folder id to try again.
          retrieveResultFolderId(driveService)
          moveToResultFolder(driveService, sheetId, retry = false)
        } else {
          // if the exception continues to show up then just throw it normally.
          throw exception
        }
    }
  }

  private def retrieveResultFolderId(driveService: Drive): String =
    synchronized {

      val folderResult: FileList = driveService
        .files()
        .list()
        .setQ(
          s"mimeType = 'application/vnd.google-apps.folder' and name='${WORKFLOW_RESULT_FOLDER_NAME}'"
        )
        .setSpaces("drive")
        .execute()

      if (folderResult.getFiles.isEmpty) {
        val fileMetadata: File = new File()
        fileMetadata.setName(WORKFLOW_RESULT_FOLDER_NAME)
        fileMetadata.setMimeType("application/vnd.google-apps.folder")
        val targetFolder: File = driveService.files.create(fileMetadata).setFields("id").execute
        WORKFLOW_RESULT_FOLDER_ID = targetFolder.getId
      } else {
        WORKFLOW_RESULT_FOLDER_ID = folderResult.getFiles.get(0).getId
      }
      WORKFLOW_RESULT_FOLDER_ID
    }

  /**
    * upload the result header to the google sheet
    */
  private def uploadHeader(
      sheetService: Sheets,
      sheetId: String,
      header: List[AnyRef]
  ): Unit = {
    uploadContent(sheetService, sheetId, List(header.asJava).asJava)
  }

  /**
    * upload the result body to the google sheet
    */
  private def uploadResult(sheetService: Sheets, sheetId: String, result: List[ITuple]): Unit = {
    val content: util.List[util.List[AnyRef]] =
      Lists.newArrayListWithCapacity(UPLOAD_BATCH_ROW_COUNT)
    // use for loop to avoid copying the whole result at the same time
    for (tuple: ITuple <- result) {

      val tupleContent: util.List[AnyRef] =
        tuple
          .asInstanceOf[Tuple]
          .getFields
          .stream()
          .map(convertUnsupported)
          .toArray
          .toList
          .asJava
      content.add(tupleContent)

      if (content.size() == UPLOAD_BATCH_ROW_COUNT) {
        uploadContent(sheetService, sheetId, content)
        content.clear()
      }
    }

    if (!content.isEmpty) {
      uploadContent(sheetService, sheetId, content)
    }
  }

  /**
    * convert the tuple content into the type the Google Sheet API supports
    */
  private def convertUnsupported(content: AnyRef): AnyRef = {
    content match {

      // if null, use empty string to represent.
      case null => ""

      // Google Sheet API supports String and number(long, int, double and so on)
      case _: String | _: Number => content

      // convert all the other type into String
      case _ => content.toString
    }

  }

  /**
    * upload the content to the google sheet
    * The type of content is java list because the google API is in java
    */
  private def uploadContent(
      sheetService: Sheets,
      sheetId: String,
      content: util.List[util.List[AnyRef]]
  ): Unit = {
    val body: ValueRange = new ValueRange().setValues(content)
    val range: String = "A1"
    val valueInputOption: String = "RAW"

    // using retry logic here, to handle possible API errors, i.e., rate limit exceeded.
    retry(attempts = RETRY_ATTEMPTS, baseBackoffTimeInMS = BASE_BACK_OOF_TIME_IN_MS) {
      sheetService.spreadsheets.values
        .append(sheetId, range, body)
        .setValueInputOption(valueInputOption)
        .execute
    }

  }
}
