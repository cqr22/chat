package com.codeman22.chat.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

/**
 *
 * @author Kuzma
 * @date 2020/5/11
 */
@Component
public class WsServer {
    /**
     *  采用单例模式创建WebSocketServer
     *  将构造函数为private 这样该类就不会被实例化
     *  使用volatile关键字 禁止指令重排 防止多线程下getInstance发生错误
     */

    private volatile static WsServer wsServer;

    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap server;
    private ChannelFuture future;

    private WsServer(){
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        //netty服务器的创建 ServerBootstrap为启动类
        server = new ServerBootstrap();
        //设置主从线程组 nio的双向通道channel 从线程组处理器
        server.group(mainGroup,subGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WsServerInitializer());
    }

    public static WsServer getInstance(){
        if (wsServer == null){
            synchronized (WsServer.class){
                if (wsServer == null){
                    wsServer = new WsServer();
                }
            }
        }
        return wsServer;
    }

    public void start(){
        this.future = server.bind(8085);
        System.err.println("服务器启动完毕");
    }
}
