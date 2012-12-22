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
* Date: 12/16/12
* Time: 12:08 PM
*/


package com.github.brisk

import cluster.{ClusteredBrisk, Server}

object Utils {

  def localServers(ports: Int*) = ports.map(Server("localhost", _))

  def cloneBrisk(source: Brisk, port: Int) = {
    val brisk = new Brisk(port)
    source.services.foreach(e => brisk.services.put(e._1, e._2))
    brisk
  }

  def cloneClusteredBrisk(source: ClusteredBrisk, port: Int) = {
    val brisk = new ClusteredBrisk(port, source.cluster)
    source.services.foreach(e => brisk.services.put(e._1, e._2))
    brisk
  }

}