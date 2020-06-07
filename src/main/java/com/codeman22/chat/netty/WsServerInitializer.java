package com.codeman22.chat.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 *
 * @author Kuzma
 * @date 2020/5/10
 */
public class WsServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        // 用于支持http协议
        ChannelPipeline channelPipeline = socketChannel.pipeline();
        //webSocket基于http协议，所以要有http编解码器
        channelPipeline.addLast(new HttpServerCodec());
        //对写大数据流的支持
        channelPipeline.addLast(new ChunkedWriteHandler());
        //1024*64消息的最大长度 httpMessage聚合器  在netty编程中，几乎都会用到此handler
        channelPipeline.addLast(new HttpObjectAggregator(1024*64));

        // 用于增加心跳机制
        // 客户端如果没有在1分钟内向服务端发送读写心跳（ALL）则主动断开 如果是读空闲或者写空闲，不处理
        channelPipeline.addLast(new IdleStateHandler(60, 60, 120));
        // 自定义的空闲状态检测
        channelPipeline.addLast(new HeartBeatHandler());

        /*
         * webSocket 服务器处理的协议，用于指定给客户端连接访问的路由 : /ws
         * 本handler会帮你处理一些繁重的复杂的事
         * 会帮你处理握手动作： handshaking（close, ping, pong） ping + pong = 心跳
         * 对于webSocket来讲，都是以frames进行传输的，不同的数据类型对应的frames也不同
         */
        channelPipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        channelPipeline.addLast(new ChatHandler());
    }
}
