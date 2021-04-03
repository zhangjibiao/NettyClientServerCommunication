package stu.Protocol;

import io.netty.channel.ChannelHandlerContext;
import stu.Util.PropertyMgr;

import java.io.IOException;
import java.io.Serializable;

public class RecFlieMsg extends Msg implements Serializable { //TODO: 子类和父类实现序列化接口的
    private boolean recSuccess;

    public RecFlieMsg() {
    }

    public RecFlieMsg(boolean success) {
        this.recSuccess = success;
    }

    public boolean isRecSuccess() {
        return recSuccess;
    }

    public void setRecSuccess(boolean recSuccess) {
        this.recSuccess = recSuccess;
    }

    @Override
    public void handle(ChannelHandlerContext ctx) {
        if (recSuccess){ //客户端要做的事情
            try {
                PropertyMgr.I.printSucInf();
                PropertyMgr.I.send(ctx);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("<对方接收文件出现错误>");
            System.exit(0);
        }
    }
}
