package com.raven.route.message.netty;


import com.raven.common.loadbalance.GatewayServerInfo;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message;
import com.raven.common.utils.Constants;
import com.raven.common.utils.JsonHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@DependsOn("internalServerChannelManager")
public class GatewayTcpClient {

    @Autowired
    private ServerChannelManager internalServerChannelManager;

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private CuratorFramework curator;

    @Value("${spring.cloud.zookeeper.gateway-server-path}")
    private String gatewayServerPath;

    private EventLoopGroup workGroup = new NioEventLoopGroup();

    @PostConstruct
    public void startClient() throws Exception {
        startZkWatcher();
    }

    @PreDestroy
    public void destroy() {
        workGroup.shutdownGracefully().syncUninterruptibly();
        log.info("close raven-single client success");
    }

    private Channel connectServer(GatewayServerInfo server) {
        if (null != internalServerChannelManager.getChannelByServer(server)) {
            return null;
        }
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new IdleStateHandler(10, 10, 15));
                    p.addLast(new ProtobufVarint32FrameDecoder());
                    p.addLast(new ProtobufDecoder(Message.RavenMessage.getDefaultInstance()));
                    // 对protobuf协议的消息头上加上一个长度为32的整形字段
                    p.addLast(new ProtobufVarint32LengthFieldPrepender());
                    p.addLast(new ProtobufEncoder());
                    p.addLast("MessageHandler", messageHandler);
                }
            });
        ChannelFuture future = bootstrap.connect(server.getIp(), server.getInternalPort())
            .syncUninterruptibly();
        if (future.isSuccess()) {
            log.info("connect server success ip:{},port:{}", server.getIp(),
                server.getInternalPort());
            return future.channel();
        } else {
            log.error("connect server failed ip:{},port:{}", server.getIp(),
                server.getInternalPort());
        }
        return null;
    }

    private void startZkWatcher() throws Exception {
        PathChildrenCache gatewayWatcher = new PathChildrenCache(curator, gatewayServerPath, true);
        gatewayWatcher.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curator,
                PathChildrenCacheEvent event) throws Exception {
                if (event.getType().equals(Type.CHILD_ADDED)) {
                    log.info("zookeeper watched add gateway server node:{}",
                        new String(event.getData().getData()));
                    Map<String, Object> data = JsonHelper
                        .strToMap(new String(event.getData().getData()));
                    String address = (String) data.get("address");
                    Map<String, Object> payload = (Map<String, Object>) data.get("payload");
                    Map<String, Object> metadata = (Map<String, Object>) payload.get("metadata");
                    int tcpPort = Integer
                        .valueOf(metadata.get(Constants.CONFIG_TCP_PORT).toString());
                    int wsPort = Integer
                        .valueOf(metadata.get(Constants.CONFIG_WEBSOCKET_PORT).toString());
                    int internalPort = Integer
                        .valueOf(metadata.get(Constants.CONFIG_INTERNAL_PORT).toString());
                    GatewayServerInfo server = new GatewayServerInfo(address, tcpPort, wsPort,
                        internalPort);
                    Channel channel = connectServer(server);
                    if (null != channel) {
                        internalServerChannelManager.addServer2Channel(server, channel);
                    }
                }
            }
        });
        gatewayWatcher.start();
        log.info("start gateway server zk watcher");
    }


}
