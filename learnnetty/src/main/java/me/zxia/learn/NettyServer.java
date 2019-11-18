package me.zxia.learn;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.nio.channels.Channel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NettyServer  {

    private static ConcurrentHashMap<String, ChannelGroup> group = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Channel> pp = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            ChannelFuture future = serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            //                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            //                            @Override
                            //                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            //                                ByteBuf in = (ByteBuf) msg;
                            //                                System.out.println(in.toString(CharsetUtil.US_ASCII));
                            //                                //消息向后面的handler传递
                            //                                ctx.fireChannelRead(msg);
                            //                            }
                            //                        });

//                            ch.pipeline().addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    ctx.writeAndFlush("欢迎加入\r\n");
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) {

                                    System.out.println(msg);
                                }

                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

                                    if (evt instanceof IdleStateEvent) {
                                        IdleStateEvent i = (IdleStateEvent) evt;
                                        if (i.state() == IdleState.READER_IDLE) {
                                            System.out.println("5秒没收到消息了");
                                            ctx.writeAndFlush("ao lei ao lei");
                                        }
                                    } else {
                                        super.userEventTriggered(ctx, evt);
                                    }
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    ctx.close();
                                }
                            });
                        }
                    })
                    .bind(8000).sync();
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

}
