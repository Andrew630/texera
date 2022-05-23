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
import java.io.{File, FileInputStream, FileReader, InputStreamReader, Reader}
import com.univocity.parsers.csv.{CsvParser, CsvParserSettings}
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord

object InferSchemaTest extends Serializable {

  val filePath = "/Users/zuozhiw/Downloads/Loopnet-Webpage-SimpleSink-operator-93aef5b0-1aaf-4ac9-b9ea-7392fb908474.csv"

  def main(args: Array[String]): Unit = {
    test()
  }


  def test(): Unit = {

    val hasHeader = true
    var reader: CSVReader = CSVReader.open(filePath)
    val firstRow: Array[String] = reader.iterator.next().toArray
    reader.close()

    val data = readData(1000)

    inferSchema(firstRow, data)

    println("done")
  }


  def readData(readLimit: Int): Iterator[Array[String]] = {
    var start = LocalDateTime.now()

//    val data = readDataCurrent(readLimit)
//    val data = readDataApache(readLimit)
    val data = readDataVocity(readLimit)

    var end = LocalDateTime.now()
    var duration = Duration.between(start, end).toMillis
    System.out.println("read data duration: " + duration + " ms")

    data
  }

  def readDataCurrent(readLimit: Int): Iterator[Array[String]] = {

    val startOffset = 1
    val endOffset = startOffset + readLimit

    var reader: CSVReader = CSVReader.open(filePath)
    val data = reader.iterator
      .slice(startOffset, endOffset)
      .map(seq => seq.toArray).toArray.iterator

    reader.close()
    data
  }

  def readDataApache(readLimit: Int): Iterator[Array[String]] = {

    val in = new FileReader(filePath)
    val records = CSVFormat.DEFAULT.parse(in)
    val startOffset = 1
    val endOffset = startOffset + readLimit

    val data = records.iterator.asScala
      .slice(startOffset, endOffset)
      .map(seq => seq.asScala.toArray).toArray.iterator

    in.close()
    data
  }

  def readDataVocity(readLimit: Int): Iterator[Array[String]] = {

    val inputReader = new InputStreamReader(new FileInputStream(
      new File(filePath)))

    val setting = new CsvParserSettings()
    setting.setMaxCharsPerColumn(-1)
    val parser = new CsvParser(setting)
    parser.beginParsing(inputReader)

    var data: Array[Array[String]] = Array()
    for (x <- 0 until readLimit) {
      val row = parser.parseNext()
      data = data :+ row
    }

    parser.stopParsing()
    inputReader.close()

    data.iterator
  }


  def inferSchema(header: Array[String], data: Iterator[Array[String]]): Unit = {
    var start = LocalDateTime.now()

//        val cols = InferSchema.apply(data, header)
//    System.out.println(schema1)

    val cols = AttributeTypeUtils.inferSchemaFromRows(data.asInstanceOf[Iterator[Array[Object]]])
//    System.out.println(schema2)

    var end = LocalDateTime.now()
    var duration = Duration.between(start, end).toMillis
    System.out.println("infer schema duration: " + duration + " ms")

    val schema = Schema.newBuilder
      .add(
        header.indices
          .map((i: Int) =>
            new Attribute(
              header.apply(i),
              cols.apply(i)
            )
          )
          .asJava
      )
      .build
    System.out.println(schema)
  }

}
