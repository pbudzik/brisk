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
* Time: 10:23 PM
*/

package com.github.brisk

import org.jboss.netty.bootstrap.ClientBootstrap
import java.util.concurrent.Executors
import org.jboss.netty.channel._
import java.net.InetSocketAddress
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}
import socket.nio.NioClientSocketChannelFactory
import org.xerial.snappy.Snappy
import scala.actors.Futures._

class BriskClient(host: String, port: Int) {
  val bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
    Executors.newCachedThreadPool()))
  bootstrap.setOption("tcpNoDelay", true)
  bootstrap.setOption("keepAlive", true)

  def invokeSync(service: String, in: Message) = synchronized {
    val handler = new ClientHandler(in + (Message.Service -> service))
    bootstrap.setPipelineFactory(new BriskClientPipelineFactory(handler))
    val future = bootstrap.connect(new InetSocketAddress(host, port))
    future.getChannel.getCloseFuture.await()
    println("response=" + handler.out)
    handler.out
  }

  def invoke(service: String, in: Message) = future {
    invokeSync(service: String, in: Message)
  }

  def destroy() {
    bootstrap.releaseExternalResources()
  }
}

class ClientHandler(in: Message) extends SimpleChannelUpstreamHandler with Logging {

  var out: Message = _

  override def messageReceived(ctx: ChannelHandlerContext, event: MessageEvent) {
    println("Message received in client")
    val bytes = event.getMessage.asInstanceOf[ChannelBuffer].array()
    out = Message.decode(Snappy.uncompress(bytes))
    println("out=" + out)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    println("Client: Unexpected exception from downstream: %s".format(e.getCause))
    e.getChannel.close()
  }

  override def channelConnected(ctx: ChannelHandlerContext, event: ChannelStateEvent) {
    println("Writting....")
    event.getChannel.write(ChannelBuffers.copiedBuffer(Snappy.compress(Message.encode(in))))
    println("Written")
  }

}

class BriskClientPipelineFactory(handler: ChannelHandler) extends ChannelPipelineFactory {

  def getPipeline = {
    val pipeline = org.jboss.netty.channel.Channels.pipeline()
    pipeline.addLast("handler", handler)
    pipeline
  }

}