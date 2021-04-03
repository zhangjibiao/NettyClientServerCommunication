package stu.Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class Server {
    public static final Server INSTANCE = new Server();
    private void Server(){};
    public static String filePath;
    public static String predictPyPath;
    public static int port;

    public static void main(String[] args) {
        //int port = 8888; // 服务端的默认端口
        if (args != null && args.length >= 3) {//空和null的区别
            port = Integer.valueOf(args[0]);
            filePath = args[1];
            predictPyPath = args[2];
        }else{
            System.out.println("Usage: java -jar server <port> <fileSavePath> <predictPyPath>");
            System.exit(1);
        }

        new Server().bind(port);
    }

    public void bind(int port) {
        EventLoopGroup boosGroup = new NioEventLoopGroup(); //服务端的管理线程
        EventLoopGroup workerGroup = new NioEventLoopGroup(); //服务端的工作线程

        //ServerBootstrap负责初始化netty服务器，并且开始监听端口的socket请求
        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(boosGroup, workerGroup)   //绑定管理线程和工作线程
                .channel(NioServerSocketChannel.class)   //ServerSocketChannelFactory 有两种选择，一种是NioServerSocketChannelFactory，一种是OioServerSocketChannelFactory。
                .option(ChannelOption.SO_BACKLOG, 124)  //BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(new ObjectEncoder());
                        channel.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null)));
                        channel.pipeline().addLast(new ServerHandler()); // 自定义Handler
                    }
                });

        try {
            System.out.println("服务启动成功");
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync(); //保证了服务一直启动，相当于一个死循环
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}

