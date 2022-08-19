package com.mars.dor.netty.server;

import com.mars.dor.netty.server.config.NettyConfig;
import com.mars.dor.netty.server.handler.NettyHeartBeatHandler;
import com.mars.dor.netty.server.handler.SandTableWarHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * pipeline配置
 * @author zhuangqingdian
 * @date 2022/8/16
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline=socketChannel.pipeline();
        //使用http的编码器和解码器
        pipeline.addLast(new HttpServerCodec());

        //添加块处理器  主要作用是支持异步发送的码流（大文件传输），但不专用过多的内存，防止java内存溢出
        pipeline.addLast(new ChunkedWriteHandler());

        //加入ObjectAggregator解码器，作用是他会把多个消息转换为单一的FullHttpRequest或者FullHttpResponse
        pipeline.addLast(new HttpObjectAggregator(65536));

        // 增加心跳支持
        // 针对客户端，如果在300s时没有向服务端发送读写心跳(ALL)，则主动断开
        // 如果是读空闲或者写空闲，不处理
        pipeline.addLast(new IdleStateHandler(NettyConfig.SOCKET_READER_IDLE_TIME, NettyConfig.SOCKET_WRITER_IDLE_TIME, NettyConfig.SOCKET_ALL_IDLE_TIME));

        // 自定义的空闲状态检测 处理消息的handler
        pipeline.addLast(new NettyHeartBeatHandler());

        // 加入webSocket的hanlder
        pipeline.addLast(new WebSocketServerProtocolHandler("/netty"));

        //自定义handler,处理业务逻辑
        pipeline.addLast(new SandTableWarHandler());


    }
}