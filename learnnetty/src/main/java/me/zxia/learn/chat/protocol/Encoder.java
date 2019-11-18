package me.zxia.learn.chat.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encoder extends MessageToByteEncoder<IMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, IMessage msg, ByteBuf out) throws Exception {
        if(msg == null ){
            throw new Exception("the encode message is null");
        }
        int version = 1;
        int length = 0;
        String data = msg.getFrom() + "|" + msg.getTo() + "|" + msg.getContent();
        out.writeInt(version);
        out.writeInt(0);
        byte[] bytes = data.getBytes();
        length = bytes.length;
        out.writeBytes(bytes);
        //写入length;
        out.setInt(4, length);
    }
}
