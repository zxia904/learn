package me.zxia.learn;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class TimeServer {

    public static void main(String[] args) {

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        serverBootstrap
                .group(boss,worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(final ChannelHandlerContext ctx) { // (1)
                                final ByteBuf time = ctx.alloc().buffer(4); // (2)
                                time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

                                final ChannelFuture f = ctx.writeAndFlush(time); // (3)

                                f.addListener(ChannelFutureListener.CLOSE);

//                                f.addListener(new ChannelFutureListener() {
//                                    @Override
//                                    public void operationComplete(ChannelFuture future) {
//                                        assert f == future;
//                                        ctx.close();
//                                    }
//                                }); // (4)
                            }
                        });
                    }
                })
                .bind(8000);

    }

}
