/*
* Copyright 2011 P.Budzik
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
*
* User: przemek
* Date: 12/11/12
* Time: 9:12 PM
*/

package com.github.brisk

import org.bson._

class Message(val underlying: BSONObject = new BasicBSONObject) {

  def get(key: String) = underlying.get(key)

  def getAs[T](key: String) = underlying.get(key).asInstanceOf[T]

  def getString(key: String) = underlying.get(key).toString

  def +[A <: String, B <: Any](entry: (A, B)) = {
    underlying.put(entry._1, entry._2)
    this
  }

  def -[A <: String, B <: Any](key: String) = {
    underlying.removeField(key)
    this
  }

  override def toString = underlying.toString

}

object Message {
  val Service = "_service"
  lazy val encoder = new BasicBSONEncoder
  lazy val decoder = new BasicBSONDecoder

  def apply[A <: String, B <: Any](elems: (A, B)*): Message = {
    val bso = new BasicBSONObject
    for ((k, v) <- elems) {
      bso.put(k, v)
    }
    new Message(bso)
  }

  def encode(doc: Message) = encoder.encode(doc.underlying)

  def decode(bytes: Array[Byte]) = {
    val callback = new BasicBSONCallback
    decoder.decode(bytes, callback)
    callback.get() match {
      case bso: BSONObject => new Message(bso)
      case _ => throw new Exception("cant deserialize")
    }
  }
}
