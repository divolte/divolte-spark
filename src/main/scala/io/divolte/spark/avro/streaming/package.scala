/*
 * Copyright 2015 GoDataDriven B.V.
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

import io.divolte.record.DefaultEventRecord
import io.divolte.spark.kafka.StringDecoder
import kafka.serializer.{DefaultDecoder, Decoder}
import org.apache.avro.Schema
import org.apache.avro.generic.IndexedRecord
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.kafka.KafkaUtils

import scala.reflect.ClassTag

package object streaming {
  implicit class RichStreamingContext(val self: StreamingContext) extends AnyVal {

    def kafkaAvroStream[K : ClassTag,
                        V >: Null <: IndexedRecord : ClassTag,
                        U <: Decoder[K] : ClassTag](schema: Schema,
                                                    kafkaParams: Map[String, String],
                                                    topics: Map[String, Int],
                                                    storageLevel: StorageLevel): AvroDStreamMagnet[K,V] = {
      val stream = KafkaUtils.createStream[K, Array[Byte], U, DefaultDecoder](self, kafkaParams, topics, storageLevel)
      AvroDStreamMagnet[K,V](schema, stream)
    }

    def divolteStream[T >: Null <: IndexedRecord : ClassTag](kafkaParams: Map[String, String],
                                                             topics: Map[String, Int],
                                                             storageLevel: StorageLevel,
                                                             schema: Schema = DefaultEventRecord.getClassSchema): AvroDStreamMagnet[String,T] = {
      kafkaAvroStream[String, T, StringDecoder](schema, kafkaParams, topics, storageLevel)
    }
  }
}
