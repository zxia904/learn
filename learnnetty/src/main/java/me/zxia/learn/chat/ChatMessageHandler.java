package me.zxia.learn.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import me.zxia.learn.chat.protocol.IMessage;

import java.util.concurrent.ConcurrentHashMap;

public class ChatMessageHandler extends ChannelInboundHandlerAdapter {

    private static ConcurrentHashMap<String, Channel> linkedMan = new ConcurrentHashMap<>();

    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        IMessage message = new IMessage("欢迎加入");
        channels.add(ctx.channel());
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if ( msg instanceof IMessage) {
            IMessage message = (IMessage)msg;
            if (!linkedMan.contains(ctx.channel()) && message.getFrom().equalsIgnoreCase(message.getTo())){
                System.out.println(message.getFrom()+"完成注册！");
                linkedMan.put(message.getFrom(), ctx.channel());
                return;
            }

            if ("all".equalsIgnoreCase(message.getTo())) {
                channels.writeAndFlush(message);
            }else {
                Channel channel = linkedMan.get(message.getTo());
                if (channel == null) {
                    IMessage ret = new IMessage(message.getTo() + "已下线");
                    ctx.channel().writeAndFlush(ret);
                    return;
                }

                channel.writeAndFlush(message);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        channels.remove(ctx.channel());
        linkedMan.entrySet().stream()
                .filter(entry -> {
                    if (entry.getValue() == ctx.channel()) {
                        return true;
                    }
                    return false;
                })
                .findFirst()
                .ifPresent(entry -> {
                    linkedMan.remove(entry.getKey());
                    System.out.println(entry.getKey()+"注销退出！");
                });
    }
}
