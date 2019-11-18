package me.zxia.learn;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServer {

    public static void main(String[] args) throws InterruptedException {

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup work = new NioEventLoopGroup();

        try {
            ChannelFuture channelFuture = new ServerBootstrap().group(boss, work)
                    .channel(NioServerSocketChannel.class)
//                    .option(ChannelOption.SO_BACKLOG, 515)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(1024*1024));
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    if (msg instanceof FullHttpRequest) {
                                        FullHttpRequest request = (FullHttpRequest) msg;
                                        System.out.println(request.toString());
                                        System.out.println(request.content().toString(CharsetUtil.UTF_8));

                                        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(request.toString().getBytes()));
                                        response.headers().set(CONTENT_TYPE, "text/plain");
                                        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

                                        boolean keepAlive = HttpUtil.isKeepAlive(request);
                                        if (!keepAlive) {
                                            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                                        } else {
                                            response.headers().set(CONNECTION, KEEP_ALIVE);
                                            ctx.write(response);
                                        }
                                        ctx.channel().writeAndFlush(msg.toString());
                                    }
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    cause.printStackTrace();
                                }
                            });
                        }
                    })
                    .bind(8080)
                    .sync();

            channelFuture.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }

}
