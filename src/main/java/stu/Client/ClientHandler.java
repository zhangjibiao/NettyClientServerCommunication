package stu.Client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import stu.Protocol.Msg;
import stu.Util.PropertyMgr;

@ChannelHandler.Sharable
//TODO: 搞清楚这个又是啥
public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    //当前channel激活的时候的时候
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        new Thread(){
            @Override
            public void run() {
                PropertyMgr.I.invokeOpencv(ctx);
            }
        }.start();
    }



    @Override  //当前channel从远端读取到数据时触发
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Msg) {
            ((Msg) msg).handle(ctx);
        }
    }

    @Override  //在当前ChannelHandler回调方法出现异常时被回调
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause.getMessage().equals("远程主机强迫关闭了一个现有的连接。")){
            System.out.println("服务端强行关闭了一个已有的连接");
            System.exit(1);
            System.out.println("-------------------------------\n");
        }else{
            cause.printStackTrace();
        }
        ctx.close();
    }
}


