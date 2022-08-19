package com.mars.dor.netty.server.config;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhuangqingdian
 * @date 2022/8/16
 */
public class NettyConfig {
    /**
     * 存储不同用户的channel
     */
    public static ConcurrentHashMap<String, Channel> userChannelMap = new ConcurrentHashMap();

    /** websocket端口 **/
    public static final int WEBSOCKET_PORT = 3032;

    /** 心跳配置 */
    public static final int SOCKET_READER_IDLE_TIME = 100;      // 空闲读
    public static final int SOCKET_WRITER_IDLE_TIME = 120;      // 空闲写
    public static final int SOCKET_ALL_IDLE_TIME = 300;         // 空闲（超过300.客户端无发包在，则为空闲）

}
