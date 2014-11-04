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
