package stu.Protocol;

import io.netty.channel.ChannelHandlerContext;
import stu.Server.Server;

import java.io.*;

public class SendFinishMsg extends Msg implements Serializable { //TODO: 子类和父类实现序列化接口的
    public SendFinishMsg() {
    }

    @Override
    public void handle(ChannelHandlerContext ctx) {
        System.out.println("\n-----------------------------------");
        System.out.println("文件接收完成");
        System.out.println("调用预测程序对虹膜进行预测：");
        invokePrediction(ctx);
    }

    public void invokePrediction(ChannelHandlerContext ctx){
        //检测python程序存在
        String PredictPyPath = Server.predictPyPath;
        File file = new File(PredictPyPath);
        if (!file.exists()){
            System.out.println("<PredictPyPath未找到>");
            System.exit(1);//TODO 增加类型，服务器出错
        }

        //调用python脚本
        String[] arguments = new String[] {"python3", PredictPyPath};//后面再加一列就是参数
        String name;
        try {
            Process process = Runtime.getRuntime().exec(arguments);
          /*  1、destroy()杀死这个子进程

　　2、exitValue()得到进程运行结束后的返回状态，会杀死进程

　　3、waitFor()得到进程运行结束后的返回状态，如果进程未运行完毕则等待知道执行完毕

　　4、getInputStream()　　得到进程的标准输出信息流

　　5、getErrorStream()　　得到进程的错误输出信息流

　　6、getOutputStream()　得到进程的输入流
————————————————
           。*/



            InputStream is = process.getInputStream();
            InputStream es = process.getErrorStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            BufferedReader errorIn = new BufferedReader(new InputStreamReader(es, "UTF-8"));
            String line = null;

            while(process.isAlive()){
                try {
                    while ((line = in.readLine()) != null) {
                        if(line.equals("1")){//做成配置文件
                            name = in.readLine();
                            System.out.println("<验证通过>");
                            System.out.println("<识别结果："+ name +">");
                            RecResultMsg rrm = new RecResultMsg(true,name); //注意实现序列化接口
                            ctx.writeAndFlush(rrm);
                        }else if(line.equals("0")){
                            System.out.println("<验证不通过>");
                            RecResultMsg rrm = new RecResultMsg(false,"NotPass");
                            ctx.writeAndFlush(rrm);
                        }else{
                            System.out.println("@预测程序:"+line);
                        }
                    }
                    while ((line = errorIn.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            in.close();

            //输出python程序执行结果
//            java代码中的process.waitFor()返回值为0表示我们调用python脚本成功，
//            返回值为1表示调用python脚本失败，这和我们通常意义上见到的0与1定义正好相反
            int re = process.waitFor();
            if(re==0){
                System.out.println("本次预测程序正常结束，等待下一次请求...");
                System.out.println("-----------------------------------");
            }else if(re==1) {
                System.out.println("预测程序执行失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
