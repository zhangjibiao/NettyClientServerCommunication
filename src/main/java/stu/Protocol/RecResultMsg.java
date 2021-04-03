package stu.Protocol;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

public class RecResultMsg extends Msg implements Serializable {
    private boolean recPass;
    private String name;

    public boolean isRecPass() {
        return recPass;
    }

    public void setRecPass(boolean recPass) {
        this.recPass = recPass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RecResultMsg(boolean recPass, String name) {
        this.recPass = recPass;
        this.name = name;
    }

    public RecResultMsg() {
    }

    @Override
    public void handle(ChannelHandlerContext ctx) {
        System.out.println("虹膜识别结果：");
        if(recPass){
            System.out.println("<验证通过>");
            System.out.println("<识别结果："+ name +">");
            System.out.println("本次识别结束，等待下一次识别...");
            System.out.println("-----------------------------------------------------------------\n\n");
            System.out.println("-----------------------------------------------------------------");

        }else{
            System.out.println("<验证不通过>");
        }
        //ctx.close();
    }
}
