package com.mars.dor.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static com.mars.dor.netty.server.config.NettyConfig.WEBSOCKET_PORT;

/**
 * @author zhuangqingdian
 * @date 2022/8/16
 */
@Component
@Slf4j
public class NettyServer {
    private EventLoopGroup bossGroup;

    private EventLoopGroup workGroup;

    private void run(){
        log.info("开始启动服务器");
        bossGroup=new NioEventLoopGroup(4);

        workGroup=new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap =new ServerBootstrap();

            serverBootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer());
            //启动服务器
            ChannelFuture channelFuture=serverBootstrap.bind(WEBSOCKET_PORT).sync();
            log.info("开始启动服务器完毕....");
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            bossGroup.shutdownGracefully();

            workGroup.shutdownGracefully();
        }

    }


    /*
     * 初始化服务器
     * */
    @PostConstruct
    @SneakyThrows
    public void init(){
        new Thread(this::run).start();
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().sync();
        }
        if (workGroup != null) {
            workGroup.shutdownGracefully().sync();
        }
    }
}
