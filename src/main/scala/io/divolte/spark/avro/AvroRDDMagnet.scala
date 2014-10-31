package io.divolte.spark.avro

import java.io.{Serializable => JSerializable}

import org.apache.avro.generic.IndexedRecord
import org.apache.avro.mapred.AvroWrapper
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

/**
 * Magnet for operations on an RDD containing Avro records.
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
 * @param rdd the RDD being wrapped.
 * @tparam T  the type of the deserialized Avro record.
 */
class AvroRDDMagnet[+T <: IndexedRecord : ClassTag] private[AvroRDDMagnet] (rdd: RDD[T]) {

  /**
   * Extract serializable values from a RDD of Avro records.
   *
   * @param f   A function to extract the serializable values from a record.
   * @tparam U  The (serializable) type of the value extracted from a record.
   * @return    A RDD containing the extracted values.
   */
  def map[U <: JSerializable : ClassTag](f: T => U): RDD[U] = rdd.map(f)

  /**
   * View the RDD of Avro records as [[io.divolte.spark.avro.Record]] instances.
   *
   * This operation must perform a deep copy of the Avro record with conversions
   * to ensure that everything can be serialized. If you only wish to access a
   * small subset of the Avro record, it can be more efficient to extract the
   * fields you need using [[AvroRDDMagnet#fields]].
   *
   * @return an RDD of [[io.divolte.spark.avro.Record]] instances built from the Avro records.
   */
  def toRecord: RDD[Record] = map(Record.apply)

  /**
   * Extract specific fields from an RDD of Avro records.
   *
   * This operation extracts specific fields from an RDD of Avro records. Field values
   * are wrapped in an [[Option]].
   *
   * @param fieldNames the names of the fields to extract
   * @return an RDD of sequences containing the field values requested.
   */
  def fields(fieldNames: String*): RDD[Seq[Option[JSerializable]]] = rdd.map { record =>
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

object AvroRDDMagnet {
  @inline
  private[spark] def apply[K <: IndexedRecord: ClassTag, V, W <: AvroWrapper[K]](rdd: RDD[(W,V)]) =
    new AvroRDDMagnet[K](rdd.map(_._1.datum()))
}
