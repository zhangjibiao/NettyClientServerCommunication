package stu.Protocol;

import io.netty.channel.ChannelHandlerContext;
import stu.Server.Server;

import java.io.*;

public class FileMsg extends Msg implements Serializable {
    //TODO:带上文件姓名、文件md5

    private byte[] content;
    private Long startPos;
    private Long endPos;
    private String filePath;//TODO: 加入发送文件的路径，

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Long getStartPos() {
        return startPos;
    }

    public void setStartPos(Long startPos) {
        this.startPos = startPos;
    }

    public Long getEndPos() {
        return endPos;
    }

    public void setEndPos(Long endPos) {
        this.endPos = endPos;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    public FileMsg(byte[] content, String fileName, Long startPos, Long endPos) {
        this.content = content;
        this.filePath = fileName;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public FileMsg() {
    }

    @Override
    public void handle(ChannelHandlerContext ctx) {
        RecFlieMsg msgResp = new RecFlieMsg(true);
        File file = new File(Server.INSTANCE.filePath);

       if(!file.exists()){
           try {
               file.createNewFile();
           } catch (IOException e) {
               System.out.println("创建文件出错");
               e.printStackTrace();
               msgResp.setRecSuccess(false);
               ctx.writeAndFlush(msgResp);
           }
           System.out.println("创建"+file.getAbsolutePath()+"成功");
        }

        RandomAccessFile raf = null;  //使用RandomAccessFile读取文件  rw权限读写
        try {
            raf = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            System.out.println("创建randomAccesFile出错");
            e.printStackTrace();
            msgResp.setRecSuccess(false);
            ctx.writeAndFlush(msgResp);
        }
        try {
            raf.seek(startPos);  //设置此次读写操作，文件的起始偏移量？？？？
        } catch (IOException e) {
            System.out.println("设置读写指针出错");
            e.printStackTrace();
            msgResp.setRecSuccess(false);
            ctx.writeAndFlush(msgResp);
        }
        try {
//            raf.write(content);  //将接收到的字节写入到文件
            raf.write(content, 0, (int) (endPos-startPos));
        } catch (IOException e) {
            System.out.println("文件写入出错");
            e.printStackTrace();
            msgResp.setRecSuccess(false);
            ctx.writeAndFlush(msgResp);
        }

        //TODO: 返回接收成功的消息，要英文：
        try {
            raf.close();   //接收完毕 关闭文件  （存在问题，可能会存在文件一直未关的情况）
        } catch (IOException e) {
            e.printStackTrace();
            msgResp.setRecSuccess(false);
            ctx.writeAndFlush(msgResp);
        }

        ctx.writeAndFlush(msgResp);
    }
}
