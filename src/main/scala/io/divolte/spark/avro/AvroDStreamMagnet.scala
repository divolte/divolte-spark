package io.divolte.spark.avro

import java.io.{Serializable => JSerializable}

import io.divolte.spark.kafka.AvroDecoder
import org.apache.avro.Schema
import org.apache.avro.generic.IndexedRecord
import org.apache.spark.streaming.dstream.DStream

import scala.reflect.ClassTag

/**
 * Magnet for operations on a DStream containing Avro records.
 *
 * This is motivated by the fact that Avro records (ironically) don't implement the
 * [[java.io.Serializable]] interface, which means that the only safe operations are
 * those which cannot result in Spark trying to serialize the records.
 *
 * For convenience, we provide two operations:
 *  1. Extract a set of fields, specified by name. These are extracted to a
 *     [[scala.Seq[Option[AnyRef]]]] collection.
 *  2. Convert to a [[io.divolte.spark.avro.Record]].
 *
 * @param stream the DStream being wrapped.
 * @tparam K     the type of the decoded Kafka keys for each message.
 * @tparam V     the type of the deserialized Avro record.
 */
class AvroDStreamMagnet[K, +V >: Null <: IndexedRecord : ClassTag] private[AvroDStreamMagnet] (stream: DStream[(K,V)]) {

  /**
   * Extract serializable values from a DStream of Avro records.
   *
   * @param f   A function to extract the serializable values from a record.
   * @tparam U  The (serializable) type of the value extracted from a record.
   * @return    A DStream containing the extracted values.
   */
  def mapValues[U <: JSerializable : ClassTag](f: V => U): DStream[(K,U)] = {
    stream.map { case(k,v) => k -> f(v) }
  }

  /**
   * View the DStream of Avro records as [[io.divolte.spark.avro.Record]] instances.
   *
   * This operation must perform a deep copy of the Avro record with conversions
   * to ensure that everything can be serialized. If you only wish to access a
   * small subset of the Avro record, it can be more efficient to extract the
   * fields you need using [[AvroRDDMagnet#fields]].
   *
   * @return a DStream of [[io.divolte.spark.avro.Record]] instances built from the Avro records.
   */
  def toRecords: DStream[(K,Record)] = mapValues(Record.apply)

  /**
   * Extract specific fields from an RDD of Avro records.
   *
   * This operation extracts specific fields from an RDD of Avro records. Field values
   * are wrapped in an [[Option]].
   *
   * @param fieldNames the names of the fields to extract
   * @return a DStream of sequences containing the field values requested.
   */
  def fields(fieldNames: String*): DStream[(K,Seq[Option[JSerializable]])] = {
    stream.map { case (k,v) => k -> AvroConverters.extractFields(v, fieldNames: _*) }
  }
}

object AvroDStreamMagnet {
  @inline
  private[spark] def apply[K,V >: Null <: IndexedRecord: ClassTag](schema: Schema, rawStream: DStream[(K,Array[Byte])]) = {
    val decoder = AvroDecoder[V](schema)
    val recordStream = rawStream.map { case(k,v) => k -> decoder.decode(v) }
    new AvroDStreamMagnet[K,V](recordStream)
  }
}
