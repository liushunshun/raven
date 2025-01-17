package com.raven.gateway.handler;

import com.raven.common.model.MsgContent;
import com.raven.common.netty.IdChannelManager;
import com.raven.common.netty.NettyAttrUtil;
import com.raven.common.protos.Message.ConverType;
import com.raven.common.protos.Message.HeartBeat;
import com.raven.common.protos.Message.HeartBeatType;
import com.raven.common.protos.Message.MessageContent;
import com.raven.common.protos.Message.MessageType;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.utils.SnowFlake;
import com.raven.storage.conver.ConverManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.txn.CreateSessionTxn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class HeartBeatHandler extends SimpleChannelInboundHandler<RavenMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private SnowFlake snowFlake;

    @Autowired
    private ConverManager converManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message) {
        if (message.getType() == Type.HeartBeat) {
            HeartBeat heartBeat = message.getHeartBeat();
            log.info("receive heartbeat :{}", heartBeat);
            if (heartBeat.getHeartBeatType() == HeartBeatType.PING) {
                HeartBeat heartBeatAck = HeartBeat.newBuilder()
                    .setId(heartBeat.getId())
                    .setHeartBeatType(HeartBeatType.PONG)
                    .build();
                RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.HeartBeat)
                    .setHeartBeat(heartBeatAck).build();
                ctx.writeAndFlush(ravenMessage);
            }
            processUserWaitAckMsg(ctx);
        } else {
            ctx.fireChannelRead(message);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                Long lastReadTime = NettyAttrUtil.getReaderTime(ctx.channel());
                String uid = uidChannelManager.getIdByChannel(ctx.channel());
                if (null == lastReadTime || null == uid) {
                    ctx.close();
                    return;
                }
                if (System.currentTimeMillis() - lastReadTime > 30000) {
                    log.info("uid:{} last read time more than 30 seconds", uid);
                    ctx.close();
                    return;
                }
                HeartBeat heartBeat = HeartBeat.newBuilder()
                    .setId(snowFlake.nextId())
                    .setHeartBeatType(HeartBeatType.PING)
                    .build();
                RavenMessage ravenMessage = RavenMessage.newBuilder().setHeartBeat(heartBeat)
                    .setType(Type.HeartBeat).build();
                ctx.writeAndFlush(ravenMessage).addListeners(future -> {
                    if (!future.isSuccess()) {
                        log.info("uid:{} off line", uid);
                        ctx.close();
                    }
                });
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void processUserWaitAckMsg(ChannelHandlerContext ctx) {
        String uid = uidChannelManager.getIdByChannel(ctx.channel());
        if (null != uid) {
            List<UpDownMessage> upDownMessageList = converManager.getWaitUserAckMsg(uid);
            for (UpDownMessage downMessage : upDownMessageList) {
                log.info("no ack message:{}", downMessage);
                RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.UpDownMessage)
                    .setUpDownMessage(downMessage).build();
                ctx.writeAndFlush(ravenMessage);
            }
        }
    }


}
