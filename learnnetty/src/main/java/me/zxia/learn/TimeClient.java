package me.zxia.learn;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Date;

public class TimeClient {

    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                    ByteBuf m = (ByteBuf) msg; // (1)
                                    try {
                                        long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
                                        System.out.println(new Date(currentTimeMillis));
                                        ctx.close();
                                    } finally {
                                        m.release();
                                    }
                                }
                            });
                        }
                    });

            ChannelFuture f = bootstrap.connect("127.0.0.1", 8000).sync();
            f.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully();
        }


    }
}
