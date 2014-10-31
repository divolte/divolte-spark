package io.divolte.spark.avro

import java.io.{Serializable => JSerializable}

import org.apache.avro.generic.IndexedRecord

import scala.language.dynamics

/**
 * Encapsulate the content from an Avro record.
 *
 * This class encapsulates the content from an Avro record, with data converted such
 * that the record, and all nested values within, are:
 *  - Of scala types in preference to Java/Avro equivalents.
 *  - Serializable, so that Spark can transport the data when necessary.
 *  - Immutable (in general).
 *
 * This class uses Scala's Dynamics feature, which means that fields are accessed
 * using normal attribute syntax. Attempting to access an attribute that does not
 * exist will result in [[java.util.NoSuchElementException]] being thrown. Field
 * values are always encapsulated in an option.
 *
 * @param fields all fields from the Avro record, converted to be serializable.
 */
@SerialVersionUID(1L)
class Record private (fields: Map[String,JSerializable]) extends Dynamic with Serializable {

  def selectDynamic(fieldName: String): Option[JSerializable] = {
    fields.get(fieldName) match {
      case Some(nullableValue) => Option(nullableValue)
      case None                => throw new NoSuchElementException(s"Record does not contain field: $fieldName")
    }
  }

  override def hashCode(): Int = fields.hashCode()

  override def equals(obj: Any): Boolean =
    obj.isInstanceOf[Record] && equals(obj.asInstanceOf[Record])

  def equals(other: Record): Boolean = null != other && fields.equals(other.fields)

  override def toString: String = fields.toString()
}

private[spark] object Record {
  @inline
  def apply(avroRecord: IndexedRecord): Record = new Record(AvroConverters.avro2scala(avroRecord))
}
