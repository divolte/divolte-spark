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

package io.divolte.spark

import _root_.kafka.serializer.{Decoder, DefaultDecoder}
import io.divolte.record.DefaultEventRecord
import io.divolte.spark.avro.streaming.AvroDStreamMagnet
import io.divolte.spark.kafka.StringDecoder
import org.apache.avro.Schema
import org.apache.avro.generic.IndexedRecord
import org.apache.avro.mapred.{AvroInputFormat, AvroJob, AvroKey, AvroWrapper}
import org.apache.avro.mapreduce.AvroKeyInputFormat
import org.apache.hadoop.io.NullWritable
import org.apache.hadoop.mapred.JobConf
import org.apache.spark.SparkContext
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.kafka.KafkaUtils

import scala.reflect.ClassTag

package object avro {
  implicit class RichSparkContext(val self: SparkContext) extends AnyVal {

    // Due to https://issues.apache.org/jira/browse/AVRO-1170 we use the old API.

    def avroFile[T <: IndexedRecord: ClassTag](path: String): AvroRDDMagnet[T] =
      AvroRDDMagnet[T, NullWritable, AvroWrapper[T]](self.hadoopFile[AvroWrapper[T], NullWritable, AvroInputFormat[T]](path))

    def avroFile[T <: IndexedRecord: ClassTag](path: String, minPartitions: Int): AvroRDDMagnet[T] =
      AvroRDDMagnet[T, NullWritable, AvroWrapper[T]](self.hadoopFile[AvroWrapper[T], NullWritable, AvroInputFormat[T]](path, minPartitions))

    def newAvroFile[T <: IndexedRecord: ClassTag](path: String, inputSchema: Option[Schema] = None): AvroRDDMagnet[T] = {
      val conf = self.hadoopConfiguration
      AvroRDDMagnet[T, NullWritable, AvroKey[T]](self.newAPIHadoopFile(path,
                                                                       classOf[AvroKeyInputFormat[T]],
                                                                       classOf[AvroKey[T]],
                                                                       classOf[NullWritable],
                                                                       conf))
    }
  }
}
