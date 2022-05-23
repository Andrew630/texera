/*
 * Copyright 2014 Databricks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uci.ics.texera.workflow.common.tuple.schema

import java.sql.Timestamp
import java.text.SimpleDateFormat

import scala.util.control.Exception._

object InferSchema {

  /**
    * Similar to the JSON schema inference.
    *     1. Infer type of each row
    *     2. Merge row types to find common type
    *     3. Replace any null types with string type
    */
  def apply(
             tokenRdd: Iterator[Array[String]],
             header: Array[String],
             dropMalformed: Boolean = false,
             nullValue: String = "",
             dateFormatter: SimpleDateFormat = null): Array[AttributeType] = {
    val startType: Array[AttributeType] = Array.fill[AttributeType](header.length)(AttributeType.ANY)
    val rootTypes: Array[AttributeType] = tokenRdd.aggregate(startType)(
      inferRowType(nullValue, header, dateFormatter, dropMalformed),
      mergeRowTypes)

    val structFields =rootTypes.map {
      case AttributeType.ANY => AttributeType.STRING
      case other => other
    }
    structFields
  }

  private def inferRowType(
                            nullValue: String,
                            header: Array[String],
                            dateFormatter: SimpleDateFormat,
                            dropMalformed: Boolean = false)
                          (rowSoFar: Array[AttributeType], next: Array[String]): Array[AttributeType] = {
    var i = 0
    if (header.length != next.length && dropMalformed) {
      // Type inference should not be based on malformed lines in case of DROPMALFORMED parse mode
      rowSoFar
    } else {
      while (i < math.min(rowSoFar.length, next.length)) {  // May have columns on right missing.
        rowSoFar(i) = inferField(rowSoFar(i), next(i), nullValue, dateFormatter)
        i+=1
      }
      rowSoFar
    }
  }

  private def mergeRowTypes(
                                  first: Array[AttributeType],
                                  second: Array[AttributeType]): Array[AttributeType] = {
    first.zipAll(second, AttributeType.ANY, AttributeType.ANY).map { case ((a, b)) =>
      findTightestCommonType(a, b).getOrElse(AttributeType.ANY)
    }
  }

  /**
    * Infer type of string field. Given known type Double, and a string "1", there is no
    * point checking if it is an Int, as the final type must be Double or higher.
    */
  private def inferField(typeSoFar: AttributeType,
                              field: String,
                              nullValue: String = "",
                              dateFormatter: SimpleDateFormat = null): AttributeType = {
    def tryParseInteger(field: String): AttributeType =
      if ((allCatch opt field.toInt).isDefined) {
      AttributeType.INTEGER
    } else {
      tryParseLong(field)
    }

    def tryParseLong(field: String): AttributeType = if ((allCatch opt field.toLong).isDefined) {
      AttributeType.LONG
    } else {
      tryParseDouble(field)
    }

    def tryParseDouble(field: String): AttributeType = {
      if ((allCatch opt field.toDouble).isDefined) {
        AttributeType.DOUBLE
      } else {
        tryParseTimestamp(field)
      }
    }

    def tryParseTimestamp(field: String): AttributeType = {
      if (dateFormatter != null) {
        // This case infers a custom `dataFormat` is set.
        if ((allCatch opt dateFormatter.parse(field)).isDefined){
          AttributeType.TIMESTAMP
        } else {
          tryParseBoolean(field)
        }
      } else {
        // We keep this for backwords competibility.
        if ((allCatch opt Timestamp.valueOf(field)).isDefined) {
          AttributeType.TIMESTAMP
        } else {
          tryParseBoolean(field)
        }
      }
    }

    def tryParseBoolean(field: String): AttributeType = {
      if ((allCatch opt field.toBoolean).isDefined) {
        AttributeType.BOOLEAN
      } else {
        stringType()
      }
    }

    // Defining a function to return the StringType constant is necessary in order to work around
    // a Scala compiler issue which leads to runtime incompatibilities with certain Spark versions;
    // see issue #128 for more details.
    def stringType(): AttributeType = {
      AttributeType.STRING
    }

    if (field == null || field.isEmpty || field == nullValue) {
      typeSoFar
    } else {
      typeSoFar match {
        case AttributeType.ANY => tryParseInteger(field)
        case AttributeType.INTEGER => tryParseInteger(field)
        case AttributeType.LONG => tryParseLong(field)
        case AttributeType.DOUBLE => tryParseDouble(field)
        case AttributeType.TIMESTAMP => tryParseTimestamp(field)
        case AttributeType.BOOLEAN => tryParseBoolean(field)
        case AttributeType.STRING => AttributeType.STRING
        case other: AttributeType =>
          throw new UnsupportedOperationException(s"Unexpected data type $other")
      }
    }
  }

  private val numericPrecedence: IndexedSeq[AttributeType] =
    IndexedSeq[AttributeType](
      AttributeType.INTEGER,
      AttributeType.LONG,
      AttributeType.DOUBLE,
      AttributeType.TIMESTAMP)

  val findTightestCommonType: (AttributeType, AttributeType) => Option[AttributeType] = {
    case (t1, t2) if t1 == t2 => Some(t1)
    case (AttributeType.ANY, t1) => Some(t1)
    case (t1, AttributeType.ANY) => Some(t1)
    case (AttributeType.STRING, t2) => Some(AttributeType.STRING)
    case (t1, AttributeType.STRING) => Some(AttributeType.STRING)

    // Promote numeric types to the highest of the two and all numeric types to unlimited decimal
    case (t1, t2) if Seq(t1, t2).forall(numericPrecedence.contains) =>
      val index = numericPrecedence.lastIndexWhere(t => t == t1 || t == t2)
      Some(numericPrecedence(index))

    case _ => None
  }
}