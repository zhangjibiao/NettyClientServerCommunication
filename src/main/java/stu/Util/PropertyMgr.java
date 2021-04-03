package stu.Util;

import io.netty.channel.ChannelHandlerContext;
import stu.Client.Client;
import stu.Protocol.FileMsg;
import stu.Protocol.SendFinishMsg;

import java.io.*;

public class PropertyMgr {
    public static final PropertyMgr I = new PropertyMgr();

    private PropertyMgr() {
    }

    public String host = Client.host;
    public String irisPath =Client.filePath;
    String irisCapPyPath =Client.opencvPyPath;

    //TODO: volatile是为了防止它转换转换成int
    /*
    使用Long 当传输的文件大于2G时，Integer类型会不够表达文件的长度,long的写法
    2的10次方个字节 1Kbyte
    2的20次方个字节 1Mbyte
    2的30次方个字节 1Gbyte
    2的31次方个字节 2Gbyte
    int能表示的数的-2的31次方~2的31次方-1
     */
    public volatile Long startPos;
    public volatile Long endPos;
    public volatile int pieceLength = 100000; //每次发送的文件块数的长度,字节
    public RandomAccessFile raf;
    public byte[] content = new byte[pieceLength];
    public FileMsg filemsg = new FileMsg();

    //发送文件的第一片
    public void sendIris(ChannelHandlerContext ctx) throws IOException {
        //TODO:根据文件大小设置byteLength
        //初始化参数
        startPos = 0L;
        endPos = 0L;
        raf = new RandomAccessFile(new File(irisPath), "r");
        raf.seek(0);

        //发送文件
        send(ctx);
    }

    public void printSucInf() throws IOException {
        Long restLength = raf.length() - I.endPos;
        System.out.println("\t<文件片段发送成功\t长度:"+content.length+
                "\t内容区间" + startPos + "~" + endPos +
                "\t剩余长度：" + restLength + ">");
    }

    //发送文件的其它片
    public void send(ChannelHandlerContext ctx) throws IOException {
        Long restLength = raf.length() - endPos;

        if(restLength==0){//发送完成
            finishSend(ctx);
        }else{
            countinueSend(ctx, restLength);
        }
    }

    private void finishSend(ChannelHandlerContext ctx) throws IOException {
        SendFinishMsg sfm = new SendFinishMsg();
        ctx.writeAndFlush(sfm);
        raf.close();

        System.out.println("文件发送完成");

//        ctx.close(); //为什么这里有个close？

    }

    private void countinueSend(ChannelHandlerContext ctx, Long restLength) throws IOException {
        //判断需要读取的长度
        int sendLength;
        if(restLength < pieceLength){//剩下的长度不够一片的长度
            sendLength = restLength.intValue();//原来是这样子，解决大小不一致，rw
        }else{
            sendLength = pieceLength;
        }

        //求设置fliemsg需要的其它参数
        startPos = endPos;
        endPos = startPos + sendLength;

        byte[] content = new byte[sendLength];
        raf.seek(startPos);
        raf.read(content);

        //设置filemsg
        filemsg.setStartPos(startPos);
        filemsg.setEndPos(endPos);
        filemsg.setContent(content);

        //发送filemsg
        ctx.writeAndFlush(filemsg);
    }

    public void invokeOpencv(ChannelHandlerContext ctx){
        //检测python程序存在
        File file = new File(irisCapPyPath);
        if (!file.exists()){
            System.out.println("<irisCapPyPath未找到>");
            System.exit(1);
        }

        //调用python脚本
        //String[] arguments = new String[] {"python3", irisCapPyPath};
        String[] arguments = new String[] {"python3", irisCapPyPath};
        try {
            System.out.println("-----------------------------------\n");
            System.out.println("-----------------------------------");
            System.out.println("开始调用irisCapPy程序");
            Process process = Runtime.getRuntime().exec(arguments);//会等待它执行完成才行？？
            InputStream is = process.getInputStream();
            InputStream es = process.getErrorStream();
//TODO 解决导出jar包警告
            BufferedReader in = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            BufferedReader errorIn = new BufferedReader(new InputStreamReader(es, "UTF-8"));

//            String line = in.readLine();//TODO: 这是阻塞方法不？

            String line = null;
            /*while(process.isAlive()){
                try {
                    if ((line = in.readLine()) != null){
                        System.line.println("@预测程序:"+line);
                        if(line.equals("Iris captured successfully")){//做成配置文件
                            System.line.println("开始传输虹膜图片");//TODO:解决要flush的问题
                            sendIris(ctx);
                        }
                    }


                    br = new BufferedReader(new InputStreamReader(es, "UTF-8"));
                    while ((line = br.readLine()) != null) {
                        System.line.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
            while(process.isAlive()){
                try {
                    while ((line = in.readLine()) != null) {
                        System.out.println("@预测程序:"+line);
                        if(line.equals("Iris captured successfully")){//做成配置文件
                            System.out.println("开始传输虹膜图片");//TODO:解决要flush的问题
                            sendIris(ctx);
                        }
                    }
                    while ((line = errorIn.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //Python程序执行结束
            in.close();
            int re = process.waitFor();
            if(re==0){
                System.out.println("<虹膜捕获程序正常结束>");
            }else if(re==1) {
                System.out.println("<虹膜捕获程序执行失败>");
                System.exit(1);
            }
            //java代码中的process.waitFor()返回值为0表示我们调用python脚本成功，
            //返回值为1表示调用python脚本失败，这和我们通常意义上见到的0与1定义正好相反
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
