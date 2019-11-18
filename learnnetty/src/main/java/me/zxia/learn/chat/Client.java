package me.zxia.learn.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.zxia.learn.chat.protocol.Decoder;
import me.zxia.learn.chat.protocol.Encoder;
import me.zxia.learn.chat.protocol.IMessage;

import java.util.Scanner;

public class Client {

    private static String NAME;

    public static void main(String[] args) throws InterruptedException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("输入个名字:");
        NAME = scanner.nextLine();

        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(new Decoder());
                            ch.pipeline().addLast(new Encoder());
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    System.out.println((IMessage)msg);
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    ctx.close();
                                }
                            });
                        }
                    });

            ChannelFuture sync = bootstrap.connect("127.0.0.1", 8000).sync();
            Channel channel = sync.channel();
            //完成注册
            IMessage reg = new IMessage(NAME,NAME,null);
            channel.writeAndFlush(reg);

            String message;
            while (!"exit".equalsIgnoreCase(message =scanner.nextLine())) {
                IMessage iMessage = new IMessage(NAME,message.split("\\|")[0],message.split("\\|")[1]);
                channel.writeAndFlush(iMessage);
            }

            channel.close();
        } finally {
            group.shutdownGracefully();
        }
    }
}
