package me.zxia.learn.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import me.zxia.learn.chat.protocol.Decoder;
import me.zxia.learn.chat.protocol.Encoder;

public class ChatChannelInitializer extends ChannelInitializer {
    @Override
    protected void initChannel(Channel ch) throws Exception {

        ch.pipeline().addLast(new Decoder());
        ch.pipeline().addLast(new Encoder());
        ch.pipeline().addLast(new ChatMessageHandler());

    }
}
