package cn.timmy.message.handler;

import cn.timmy.message.channel.NettyChannelManager;
import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Author zxx
 * Description  心跳
 * Date Created on 2018/6/30
 */
@Component
@Sharable
public class HeartBeatHandler extends SimpleChannelInboundHandler<MessageLite> {

    private static final Logger logger = LogManager.getLogger(
        HeartBeatHandler.class);

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite){
        channelHandlerContext.fireChannelRead(messageLite);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                String uid = nettyChannelManager.getUidByChannel(ctx.channel());
                logger.info("uid:{} read idle", uid);
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}