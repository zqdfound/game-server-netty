package com.mars.dor.netty.pojo.sandTable.enums;

/**
 * 消息类型
 *
 * @author yeeq
 */
public enum MessageTypeEnum {
    /**
     * 心跳检测
     */
    HEART_BEAT,
    /**
     * 用户加入
     */
    ADD_USER,
    /**
     * 匹配对手
     */
    MATCH_USER,
    /**
     * 取消匹配
     */
    CANCEL_MATCH,
    /**
     * 游戏开始
     */
    PLAY_GAME,
    /**
     * 游戏结束
     */
    GAME_OVER,
    /**
     * 创建房间
     */
    CREATE_ROOM,
    /**
     * 加入房间
     */
    JOIN_ROOM,
    /**
     * 退出房间
     */
    QUIT_ROOM,
    /**
     * 房主踢人
     */
    KICK_OUT
    ;

    public String getValue() {
        return this.name();
    }
}