package stu.Client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;

public class Client {
    public static final Client INSTANCE = new Client();
    private void Client(){};
    public static String filePath;
    public static String opencvPyPath;
    public static int port;
    public static String host;

    public static void main(String[] args) {
        if (args != null && args.length >= 4) {
            host = args[0];
            port = Integer.valueOf(args[1]);
            filePath = args[2];
            opencvPyPath = args[3];
            if(!(new File(opencvPyPath)).exists()) System.out.println("要调用的Py文件找不到");
        }else{
            System.out.println("Usage: java -jar client.jar <host> <port> <fileSendPath> <opencvPyPath>");
            System.exit(1);
        }

        new Client().connect(host, port);
    }

    public void connect(String host, int port) {
        EventLoopGroup group = new NioEventLoopGroup();  //只需要一个线程组，和服务端有所不同

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)//TODO: 为什么这里可以用tcp
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(new ObjectEncoder());//TODO: 搞清楚这个是啥
                        channel.pipeline().addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
                        channel.pipeline().addLast(new ClientHandler());  //自定义的handler
                    }
                });
        ChannelFuture future = null;
        try {
            future = bootstrap.connect(host, port);
            future.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        System.out.println("客户端未能连上服务器");
                    } else {
                        System.out.println("客户端成功连上服务器");
                    }
                }});
            future.sync();   //使得链接保持
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }

}


