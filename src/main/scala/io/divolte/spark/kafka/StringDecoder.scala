package io.divolte.spark.kafka

import java.nio.charset.StandardCharsets

import kafka.serializer.Decoder
import kafka.utils.VerifiableProperties

/**
 * Kafka decoder for Strings, enforcing UTF-8 encoding.
 *
 * @param props Kafka properties. Ignored.
 */
class StringDecoder(props: VerifiableProperties = null) extends Decoder[String] {
  override def fromBytes(bytes: Array[Byte]): String = new String(bytes, StandardCharsets.UTF_8)
}
