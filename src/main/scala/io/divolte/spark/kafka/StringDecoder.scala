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
