package io.divolte.spark

import org.apache.avro.generic.IndexedRecord
import org.apache.avro.mapred.{AvroInputFormat, AvroKey, AvroWrapper}
import org.apache.avro.mapreduce.AvroKeyInputFormat
import org.apache.hadoop.io.NullWritable
import org.apache.spark.SparkContext

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
}
