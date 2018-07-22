package com.kanmenzhu.ipaudio.ipaudioaclient.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 接收服务端信息
 */
public class ClientHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger=LoggerFactory.getLogger(ClientHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object data) throws Exception {
        logger.info("receive data from server:{}", data);
    }
}
