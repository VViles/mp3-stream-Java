package com.kanmenzhu.ipaudio.ipaudioaclient.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负责TCP通信
 */
public class TcpClient {
    private static final Logger logger = LoggerFactory.getLogger(TcpClient.class);
    private final static String HOST = "MediaCore的IP地址";
    private final static int PORT = 6002;
    private static Bootstrap bootstrap = getBootstrap();
    private static Channel channel;
    //= getChannel(HOST, PORT);
    private static Socket client;
    private static DataOutputStream output;
    
    private static final Bootstrap getBootstrap() {
		try {
			client = new Socket("MediaCoreIP", 6002);
			// 读取服务器端数据
    	     output = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  if(true) return null;  
    	
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class);
        b.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                //pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                pipeline.addLast("encoder", new ByteArrayEncoder());
                pipeline.addLast("handler", new ClientHandler());
            }
        });
        b.option(ChannelOption.SO_KEEPALIVE, true);
        return b;
    }

    private static final Channel getChannel(String host, int port) {
        Channel channel = null;
        try {
            channel = bootstrap.connect(host, port).sync().channel();

        } catch (Exception e) {
            logger.error("connect to host:{},port:{} failed", host, port, e);
            return null;
        }
        return channel;
    }

    public static boolean sendStringMsg(String msg) {
        logger.info("send string msg:{}", msg);
        if (channel != null) {
            try {
                channel.writeAndFlush(msg).sync();
                return true;
            } catch (InterruptedException e) {
                logger.error("send data failed.", e);
                return false;
            }
        } else {
            logger.warn("socket connect failed!");
            return false;
        }
    }

    public static void sendByteArray(byte[] data) {
        logger.info("send {} bytes data", data.length);
        try {
			output.write(data);
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        if (channel != null) {
//            try {
//                channel.writeAndFlush(data).sync();
//            } catch (InterruptedException e) {
//                logger.error("send data failed.", e);
//            }
//        } else {
//            logger.warn("socket connect failed!");
//        }
    }

}
