package me.zxia.learn.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

    public static void start(Integer port) {

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup work = new NioEventLoopGroup();

        try {
            ChannelFuture future = new ServerBootstrap()
                    .group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChatChannelInitializer())
                    .bind(port).sync();
            future.channel().closeFuture().sync();
        } catch(InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }

    }


    public static void main(String[] args) {
        Server.start(8000);
    }
}
