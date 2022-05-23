package edu.uci.ics.texera.workflow.common.tuple.schema

import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
import com.google.common.base.Preconditions
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.AttributeType._
import edu.uci.ics.texera.workflow.common.tuple.schema.AttributeTypeUtils.inferSchemaFromRows
import scala.collection.JavaConverters._

import scala.jdk.CollectionConverters.asJavaIterableConverter
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.{Duration, Instant, LocalDateTime}
import scala.util.Try
import scala.util.control.Exception.allCatch

object InferSchemaTest extends Serializable {

  def main(args: Array[String]): Unit = {
    test3()
//    test2()
  }


  def test1(): Unit = {

    val hasHeader = true
    val filePath = "C:\\Users\\zuozhiw\\Downloads\\Loopnet-Webpage-SimpleSink-operator-93aef5b0-1aaf-4ac9-b9ea-7392fb908474.csv"


    var reader: CSVReader = CSVReader.open(filePath)
    val firstRow: Array[String] = reader.iterator.next().toArray
    reader.close()

    // reopen the file to read from the beginning
    reader = CSVReader.open(filePath)

    val readLimit = 5

    val startOffset = (if (hasHeader) 1 else 0)
    val endOffset =
      startOffset + readLimit

    var start = LocalDateTime.now()

    val data =       reader.iterator
      .slice(startOffset, endOffset)
      .map(seq => seq.toArray).toArray.asInstanceOf[Array[Array[Object]]]

    var end = LocalDateTime.now()
    var duration = Duration.between(start, end).toMillis
    System.out.println("read data duration: " + duration + " ms")

    start = LocalDateTime.now()

    val attributeTypeList: Array[AttributeType] = inferSchemaFromRows(
      data.iterator
    )

    end = LocalDateTime.now()
    duration = Duration.between(start, end).toMillis
    System.out.println("infer schema duration: " + duration + " ms")



    reader.close()

    // build schema based on inferred AttributeTypes
    val schema = Schema.newBuilder
      .add(
        firstRow.indices
          .map((i: Int) =>
            new Attribute(
              if (hasHeader) firstRow.apply(i) else "column-" + (i + 1),
              attributeTypeList.apply(i)
            )
          )
          .asJava
      )
      .build

    System.out.println(schema)
  }

  def test3(): Unit = {

    val hasHeader = true
    val filePath = "C:\\Users\\zuozhiw\\Downloads\\Loopnet-Webpage-SimpleSink-operator-93aef5b0-1aaf-4ac9-b9ea-7392fb908474.csv"

    var reader: CSVReader = CSVReader.open(filePath)
    val firstRow: Array[String] = reader.iterator.next().toArray
    reader.close()



    import java.io.FileReader
    import java.io.Reader
    val in = new FileReader(filePath)

    import org.apache.commons.csv.CSVFormat
    import org.apache.commons.csv.CSVRecord
    val records = CSVFormat.DEFAULT.parse(in)

    val readLimit = 25

    val startOffset = (if (hasHeader) 1 else 0)
    val endOffset =
      startOffset + readLimit

    var start = LocalDateTime.now()

    val data =       records.iterator.asScala
      .slice(startOffset, endOffset)
      .map(seq => seq.asScala.toArray).toArray.asInstanceOf[Array[Array[Object]]]

    var end = LocalDateTime.now()
    var duration = Duration.between(start, end).toMillis
    System.out.println("read data duration: " + duration + " ms")

    start = LocalDateTime.now()

    val attributeTypeList: Array[AttributeType] = inferSchemaFromRows(
      data.iterator
    )

    end = LocalDateTime.now()
    duration = Duration.between(start, end).toMillis
    System.out.println("infer schema duration: " + duration + " ms")



    reader.close()

    // build schema based on inferred AttributeTypes
    val schema = Schema.newBuilder
      .add(
        firstRow.indices
          .map((i: Int) =>
            new Attribute(
              if (hasHeader) firstRow.apply(i) else "column-" + (i + 1),
              attributeTypeList.apply(i)
            )
          )
          .asJava
      )
      .build

    System.out.println(schema)
  }

  def test2(): Unit = {


    val hasHeader = true
    val filePath = "C:\\Users\\zuozhiw\\Downloads\\Loopnet-Webpage-SimpleSink-operator-93aef5b0-1aaf-4ac9-b9ea-7392fb908474.csv"


    var reader: CSVReader = CSVReader.open(filePath)
    val firstRow: Array[String] = reader.iterator.next().toArray
    reader.close()

    // reopen the file to read from the beginning
    reader = CSVReader.open(filePath)

    val readLimit = 5

    val startOffset = (if (hasHeader) 1 else 0)
    val endOffset =
      startOffset + readLimit

    var start = LocalDateTime.now()


    val data =       reader.iterator
      .slice(startOffset, endOffset)
      .map(seq => seq.toArray)


    val attributeTypeList: Array[AttributeType] = InferSchema.apply(data, firstRow)

    var end = LocalDateTime.now()
    var duration = Duration.between(start, end).toSeconds
    System.out.println("duration: " + duration + " s")

    reader.close()

    // build schema based on inferred AttributeTypes
    val schema = Schema.newBuilder
      .add(
        firstRow.indices
          .map((i: Int) =>
            new Attribute(
              if (hasHeader) firstRow.apply(i) else "column-" + (i + 1),
              attributeTypeList.apply(i)
            )
          )
          .asJava
      )
      .build

    System.out.println(schema)
  }

}
