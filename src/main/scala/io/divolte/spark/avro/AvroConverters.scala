/*
 * Copyright 2014 GoDataDriven B.V.
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

package io.divolte.spark.avro

import java.io.{Serializable => JSerializable}
import java.nio.ByteBuffer
import java.util.{List => JList}

import akka.util.ByteString
import org.apache.avro.generic.IndexedRecord
import org.apache.avro.util.Utf8

/**
 * Converters for values contained in Avro records to Scala equivalents.
 * In general, the Scala equivalents are serializable and pattern-matchable.
 *
 * The conversions include:
 *
 *  - [[Utf8]] to [[String]]
 *  - [[ByteBuffer]] to [[Array[Byte]]]
 *  - [[IndexedRecord]] to [[Map[String,java.io.Serializable]]], with values converted recursively.
 *  - [[java.util.List]] to [[List[java.io.Serializable]], with values converted recursively.
 *
 * Null values and objects not mentioned above that implement [[java.io.Serializable]] are left as-is.
 */
object AvroConverters {

  def avro2scala(avroValue: AnyRef): JSerializable = {
    avroValue match {
      // Simple types
      case s: Utf8                 => avro2scala(s)
      case b: ByteBuffer           => avro2scala(b).asInstanceOf[JSerializable]
      // Complex types
      case r: IndexedRecord        => avro2scala(r).asInstanceOf[JSerializable]
      case a: JList[_]             => avro2scala(a.asInstanceOf[JList[AnyRef]]).asInstanceOf[JSerializable]
      // Anything else that's serializable, including null: pass-through
      case s: JSerializable        => s
      case null                    => null
      // Anything else can't be dealt with.
      case x =>
        throw new UnsupportedOperationException(s"Cannot convert Avro value to a serializable equivalent: $x")
    }
  }

  def avro2scala(avroString: Utf8): String = avroString.toString

  def avro2scala(avroBytes: ByteBuffer): ByteString = ByteString(avroBytes)

  def avro2scala(avroRecord: IndexedRecord): Map[String, JSerializable] = {
    import scala.collection.JavaConverters._
    avroRecord.getSchema.getFields.asScala.map { field =>
      field.name() -> avro2scala(avroRecord.get(field.pos()))
    } .toMap
  }

  def avro2scala(avroList: JList[AnyRef]): Seq[JSerializable] = {
    import scala.collection.JavaConverters._
    avroList.asScala.map(avro2scala)
  }

  private[spark] def extractFields(record: IndexedRecord,
                                   fieldNames: String*): Seq[Option[JSerializable]] = {
    val schema = record.getSchema
    fieldNames.map { fieldName =>
      val field = schema.getField(fieldName)
      if (null == field) {
        throw new NoSuchElementException(s"Record does not contain field: $fieldName")
      }
      Option.apply(record.get(field.pos())).map(AvroConverters.avro2scala)
    }
  }
}
