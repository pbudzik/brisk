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
* Date: 1/28/12
* Time: 3:00 PM
*/

package com.github.brisk

import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import java.util.concurrent.Executors
import java.net.InetSocketAddress
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.group.DefaultChannelGroup

class BriskServer(port: Int) extends Logging {
  val bootstrap = new ServerBootstrap(
    new NioServerSocketChannelFactory(
      Executors.newCachedThreadPool(),
      Executors.newCachedThreadPool()))
  val channels = new DefaultChannelGroup()

  def configure(handler: Message => Message) {
    bootstrap.setPipelineFactory(new BriskPipelineFactory(handler))
  }

  def start() {
    bootstrap.bind(new InetSocketAddress(port))
  }

  def stop() {
    channels.close().awaitUninterruptibly()
    bootstrap.releaseExternalResources()
    debug("Server at " + port + " destroyed")
  }

  class BriskPipelineFactory(handler: Message => Message) extends ChannelPipelineFactory {

    def getPipeline = {
      val pipeline = org.jboss.netty.channel.Channels.pipeline()
      pipeline.addLast("handler", new BriskHandler(channels, handler))
      pipeline
    }

  }

}

