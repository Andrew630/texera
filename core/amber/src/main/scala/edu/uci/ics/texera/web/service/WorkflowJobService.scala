package edu.uci.ics.texera.web.service

import com.twitter.util.{Await, Duration, Future}
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.ModifyLogicHandler.ModifyLogic
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.StartWorkflowHandler.StartWorkflow
import edu.uci.ics.amber.engine.architecture.controller.{ControllerConfig, Workflow}
import edu.uci.ics.amber.engine.common.AmberUtils
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.amber.engine.common.virtualidentity.WorkflowIdentity
import edu.uci.ics.texera.web.model.websocket.request.{ModifyLogicRequest, WorkflowExecuteRequest}
import edu.uci.ics.texera.web.resource.dashboard.workflow.WorkflowVersionResource.getWorkflowByVersion
import edu.uci.ics.texera.web.service.WorkflowJobService.{DELTA_SEMANTIC_REASONING_FLAG, computeVersionsDiff}
import edu.uci.ics.texera.web.storage.WorkflowStateStore
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.{READY, RUNNING}
import edu.uci.ics.texera.web.{SubscriptionManager, TexeraWebApplication, WebsocketInput}
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.workflow.WorkflowCompiler.ConstraintViolationException
import edu.uci.ics.texera.workflow.common.workflow.WorkflowInfo.toJgraphtDAG
import edu.uci.ics.texera.workflow.common.workflow.{DBWorkflowToLogicalPlan, WorkflowCompiler, WorkflowInfo, WorkflowRewriter}
import org.jooq.types.UInteger

object WorkflowJobService {
  private final val DELTA_SEMANTIC_REASONING_FLAG: Boolean =
    AmberUtils.amberConfig.getBoolean("workflow-executions.delta-semantic-reasoning-flag")

  /**
   * This method takes in two workflow DAGs and returns the difference between them modeled as XXXX
   * @param previousWorkflow the previously executed workflow
   * @param currentWorkflow the current workflow to be executed
   * @return semantic difference between the two versions
   * */
  def computeVersionsDiff(previousWorkflow: WorkflowInfo, currentWorkflow: WorkflowInfo) : Unit = {
    // starting from source compare the operators until reach two operators are not equals
    // that operator will be the starting point of the difference
    // model the change from that operator
    val previousDAG: previousWorkflow.WorkflowDAG = previousWorkflow.toDAG
    val currentDAG: currentWorkflow.WorkflowDAG = currentWorkflow.toDAG
    System.out.println("PREVIOUS " + previousDAG)
    System.out.println("CURRENT " + currentDAG)
    val currentSources: List[String] = currentDAG.sourceOperators
    currentSources.foreach(source => {
      // first compare that all sources are the same then recursively move to downstream operators
      currentDAG.getDownstream(source)
    })
  }

}
class WorkflowJobService(
    workflowContext: WorkflowContext,
    stateStore: WorkflowStateStore,
    wsInput: WebsocketInput,
    operatorCache: WorkflowCacheService,
    resultService: JobResultService,
    request: WorkflowExecuteRequest,
    errorHandler: Throwable => Unit
) extends SubscriptionManager
    with LazyLogging {

  val workflowInfo: WorkflowInfo = createWorkflowInfo()
  val workflowCompiler: WorkflowCompiler = createWorkflowCompiler(workflowInfo)
  val workflow: Workflow = workflowCompiler.amberWorkflow(
    WorkflowIdentity(workflowContext.jobId),
    resultService.opResultStorage
  )

  // Runtime starts from here:
  var client: AmberClient =
    TexeraWebApplication.createAmberRuntime(
      workflow,
      ControllerConfig.default,
      errorHandler
    )
  val jobBreakpointService = new JobBreakpointService(client, stateStore)
  val jobStatsService = new JobStatsService(client, stateStore)
  val jobRuntimeService =
    new JobRuntimeService(client, stateStore, wsInput, jobBreakpointService)
  val jobPythonService =
    new JobPythonService(client, stateStore, wsInput, jobBreakpointService)

  addSubscription(wsInput.subscribe((req: ModifyLogicRequest, uidOpt) => {
    workflowCompiler.initOperator(req.operator)
    client.sendAsync(ModifyLogic(req.operator))
  }))

  workflowContext.executionID = -1 // for every new execution,
  // reset it so that the value doesn't carry over across executions
  def startWorkflow(): Unit = {
    for (pair <- workflowInfo.breakpoints) {
      Await.result(
        jobBreakpointService.addBreakpoint(pair.operatorID, pair.breakpoint),
        Duration.fromSeconds(10)
      )
    }
    resultService.attachToJob(workflowInfo, client)
    if (WorkflowService.userSystemEnabled) {
      // get previous executed versions of the same workflow to reason if the results are going to be the same
      val previousExecutedVersionsIDs:List[UInteger] = ExecutionsMetadataPersistService.getLatestExecutedVersionsOfWorkflow(workflowContext.wId)
      workflowContext.executionID =
        ExecutionsMetadataPersistService.insertNewExecution(workflowContext.wId)
      if(DELTA_SEMANTIC_REASONING_FLAG) {
      Future {
        // iterate through all the version ID until we find a version that is semantically equivalent to the current workflow
        for (versionID <- previousExecutedVersionsIDs) {
          val previousWorkflow = getWorkflowByVersion(UInteger.valueOf(workflowContext.wId), versionID)
          // TODO pass context and executionID to set it for each operator
          // convert workflow format from DB format to engine Logical DAG
          val workflowCasting: DBWorkflowToLogicalPlan = new DBWorkflowToLogicalPlan(previousWorkflow.getContent)
          workflowCasting.createLogicalPlan()
          // model the change (datastructure)
          val diff = computeVersionsDiff(workflowCasting.getWorkflowLogicalPlan(), workflowInfo)
        }
      }
        // call function(DAG v1, DAG v2) to get answer they are same or not
        // accordingly decide to store new result or just use previous result
          // save mapping of sink and mongoCollec
      }
    }
    stateStore.jobStateStore.updateState(jobInfo =>
      jobInfo.withState(READY).withEid(workflowContext.executionID).withError(null)
    )
    client.sendAsyncWithCallback[Unit](
      StartWorkflow(),
      _ => stateStore.jobStateStore.updateState(jobInfo => jobInfo.withState(RUNNING))
    )
  }

  private[this] def createWorkflowInfo(): WorkflowInfo = {
    var workflowInfo = WorkflowInfo(request.operators, request.links, request.breakpoints)
    if (WorkflowCacheService.isAvailable) {
      workflowInfo.cachedOperatorIds = request.cachedOperatorIds
      logger.debug(
        s"Cached operators: ${operatorCache.cachedOperators} with ${request.cachedOperatorIds}"
      )
      val workflowRewriter = new WorkflowRewriter(
        workflowInfo,
        operatorCache.cachedOperators,
        operatorCache.cacheSourceOperators,
        operatorCache.cacheSinkOperators,
        operatorCache.operatorRecord,
        resultService.opResultStorage
      )
      val newWorkflowInfo = workflowRewriter.rewrite
      val oldWorkflowInfo = workflowInfo
      workflowInfo = newWorkflowInfo
      workflowInfo.cachedOperatorIds = oldWorkflowInfo.cachedOperatorIds
      logger.info(
        s"Rewrite the original workflow: ${toJgraphtDAG(oldWorkflowInfo)} to be: ${toJgraphtDAG(workflowInfo)}"
      )
    }
    workflowInfo
  }

  private[this] def createWorkflowCompiler(
      workflowInfo: WorkflowInfo
  ): WorkflowCompiler = {
    val compiler = new WorkflowCompiler(workflowInfo, workflowContext)
    val violations = compiler.validate
    if (violations.nonEmpty) {
      throw new ConstraintViolationException(violations)
    }
    compiler
  }

  override def unsubscribeAll(): Unit = {
    super.unsubscribeAll()
    jobBreakpointService.unsubscribeAll()
    jobRuntimeService.unsubscribeAll()
    jobPythonService.unsubscribeAll()
    jobStatsService.unsubscribeAll()
  }

}
