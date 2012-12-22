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
import scala.language.dynamics

class Message(val underlying: BSONObject = new BasicBSONObject) extends Dynamic {

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

  override def equals(obj: Any): Boolean = obj match {
    case msg: Message => msg.underlying == underlying
    case _ => false
  }

  def selectDynamic(key: String) = {
    if (underlying.containsField(key))
      get(key)
    else throw new NoSuchElementException("No value for: " + key)
  }

  def applyDynamic(key: String)(args: Any*) = {
    if (underlying.containsField(key))
      get(key)
    else throw new NoSuchElementException("No value for: " + key)
  }
}

object Message {
  val Service = "_service"

  def apply[A <: String, B <: Any](elems: (A, B)*): Message = {
    val bso = new BasicBSONObject
    for ((k, v) <- elems) {
      bso.put(k, v)
    }
    new Message(bso)
  }

  def encode(doc: Message) = {
    val encoder = new BasicBSONEncoder
    encoder.encode(doc.underlying)
  }

  def decode(bytes: Array[Byte]) = {
    val decoder = new BasicBSONDecoder
    val callback = new BasicBSONCallback
    decoder.decode(bytes, callback)
    callback.get() match {
      case bso: BSONObject => new Message(bso)
      case _ => throw new Exception("cant deserialize")
    }
  }

}

