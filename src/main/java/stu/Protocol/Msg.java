package stu.Protocol;


import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

public abstract class Msg implements Serializable {
    public abstract void handle(ChannelHandlerContext ctx);
}
