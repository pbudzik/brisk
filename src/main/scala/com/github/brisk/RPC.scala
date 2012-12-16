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
* Date: 12/15/12
* Time: 6:37 PM
*/


package com.github.brisk

import cluster.Server
import java.util.concurrent.atomic.AtomicInteger

trait RPC {

  val clients: collection.mutable.Map[Server, BriskClient]

  def invokeSync(service: String, in: Message = Message(), selector: Selector = RoundRobin) =
    selector.selectOne(clients).invokeSync(service, in)

  def invoke(service: String, in: Message = Message(), selector: Selector = RoundRobin) =
    selector.selectOne(clients).invoke(service, in)

  def invokeAll(service: String, in: Message = Message()) =
    clients.values.map(_.invoke(service, in))

  def foo(service: String, in: Message = Message(), stopPredicate: Message => Boolean) = {
    val futures = clients.values.par.map {
      client =>
        val f = client.invoke(service, in)
        f
    }
    //futures.head.
  }

  def destroy()

  import collection.mutable.Map

  trait Selector {
    def selectOne(client: Map[Server, BriskClient]): BriskClient
  }

  object RoundRobin extends Selector {
    val counter = new AtomicInteger

    def selectOne(clients: Map[Server, BriskClient]) =
      if (clients.isEmpty) throw new Exception("No Brisk servers connected to cluster")
      else
        clients.values.toList(counter.incrementAndGet() % clients.size)
  }

}