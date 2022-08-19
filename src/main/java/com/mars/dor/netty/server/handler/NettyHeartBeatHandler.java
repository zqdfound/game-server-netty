package com.mars.dor.netty.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于检测channel的心跳handler，超时直接关闭
 * @author zhuangqingdian
 * @date 2022/8/17
 */
@Slf4j
public class NettyHeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent) {
            // 强制类型转换
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.info("进入读空闲...");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                log.info("进入写空闲...");
            } else if (event.state() == IdleState.ALL_IDLE) {
                log.info("所有的空闲...");
                Channel channel = ctx.channel();
                // 关闭无用的channel，以防资源浪费
                //todo 根据用户当前状态做不同处理 例如对战中的判定逃跑
                channel.writeAndFlush(new TextWebSocketFrame("长时间未操作，会话关闭"));
                channel.close();
            }
        }

    }
}
