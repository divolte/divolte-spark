package io.divolte.spark

import _root_.kafka.serializer.{Decoder, DefaultDecoder}
import io.divolte.record.DefaultEventRecord
import io.divolte.spark.kafka.StringDecoder
import org.apache.avro.Schema
import org.apache.avro.generic.IndexedRecord
import org.apache.avro.mapred.{AvroInputFormat, AvroKey, AvroWrapper}
import org.apache.avro.mapreduce.AvroKeyInputFormat
import org.apache.hadoop.io.NullWritable
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

    def newAvroFile[T <: IndexedRecord: ClassTag](path: String): AvroRDDMagnet[T] =
      AvroRDDMagnet[T, NullWritable, AvroKey[T]](self.newAPIHadoopFile[AvroKey[T], NullWritable, AvroKeyInputFormat[T]](path))
  }

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
