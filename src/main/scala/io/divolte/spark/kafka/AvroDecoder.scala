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

package io.divolte.spark.kafka

import java.io.{IOException, ObjectInputStream, ObjectOutputStream}

import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumReader, IndexedRecord}
import org.apache.avro.io.DecoderFactory

@SerialVersionUID(1L)
class AvroDecoder[+T >: Null <: IndexedRecord] private (var schema: Schema) extends Serializable {
  import io.divolte.spark.kafka.AvroDecoder._

  @transient
  private[this] var reader = new GenericDatumReader[T](schema)

  def decode(buffer: Array[Byte]): T = {
    val decoder = decoderFactory.binaryDecoder(buffer, null)
    reader.read(null, decoder)
  }

  @throws(classOf[IOException])
  private def writeObject(out: ObjectOutputStream): Unit = {
    out.writeUTF(schema.toString)
  }

  @throws(classOf[IOException])
  private def readObject(in: ObjectInputStream): Unit = {
    val schemaString = in.readUTF()
    schema = new Schema.Parser().parse(schemaString)
    reader = new GenericDatumReader[T](schema)
  }
}

object AvroDecoder {

  private val decoderFactory = DecoderFactory.get()

  def apply[T >: Null <: IndexedRecord](schema: Schema): AvroDecoder[T] = new AvroDecoder[T](schema)
}
