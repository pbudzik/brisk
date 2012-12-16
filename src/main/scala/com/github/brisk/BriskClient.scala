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
import java.util.concurrent.{TimeUnit, CountDownLatch, Executors}
import org.jboss.netty.channel._
import group.{ChannelGroup, DefaultChannelGroup}
import java.net.InetSocketAddress
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}
import socket.nio.NioClientSocketChannelFactory
import org.xerial.snappy.Snappy
import scala.concurrent._
import duration.Duration
import ExecutionContext.Implicits.global

class BriskClient(host: String, port: Int) extends Logging {
  val bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
    Executors.newCachedThreadPool()))
  val channels = new DefaultChannelGroup
  bootstrap.setOption("tcpNoDelay", true)
  bootstrap.setOption("keepAlive", true)

  def invoke(service: String, in: Message) = future {
    val handler = new ClientHandler(channels, in + (Message.Service -> service))
    bootstrap.setPipelineFactory(new BriskClientPipelineFactory(handler))
    bootstrap.connect(new InetSocketAddress(host, port))
    handler.get
  }

  def invokeSync(service: String, in: Message) = {
    val future = invoke(service: String, in: Message)
    Await.result(future, Duration.create(5, TimeUnit.SECONDS))
  }

  def destroy() {
    channels.close().awaitUninterruptibly()
    bootstrap.releaseExternalResources()
    debug("Client " + host + ":" + port + " destroyed")
  }

  class BriskClientPipelineFactory(handler: ChannelHandler) extends ChannelPipelineFactory {

    def getPipeline = {
      val pipeline = org.jboss.netty.channel.Channels.pipeline()
      pipeline.addLast("handler", handler)
      pipeline
    }
  }

}

class ClientHandler(channels: ChannelGroup, in: Message) extends SimpleChannelUpstreamHandler with Logging {

  val latch = new CountDownLatch(1)

  var out: Message = _

  def get = {
    try {
      latch.await()
    } catch {
      case e: InterruptedException => throw new Exception(e)
    }
    out
  }

  override def messageReceived(ctx: ChannelHandlerContext, event: MessageEvent) {
    debug("Message received in client")
    val bytes = event.getMessage.asInstanceOf[ChannelBuffer].array()
    out = Message.decode(Snappy.uncompress(bytes))
    latch.countDown()
    debug("out=" + out)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    warn("Client: Unexpected exception from downstream: %s".format(e.getCause))
    e.getChannel.close()
  }

  override def channelConnected(ctx: ChannelHandlerContext, event: ChannelStateEvent) {
    debug("Writting....")
    val channel = event.getChannel
    channels.add(channel)
    channel.write(ChannelBuffers.copiedBuffer(Snappy.compress(Message.encode(in))))
    debug("Written")
  }

}

