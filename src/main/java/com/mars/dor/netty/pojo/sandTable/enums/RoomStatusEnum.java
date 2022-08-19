package com.mars.dor.netty.pojo.sandTable.enums;

/**
 * 用户状态
 * @author zhuangqingdian
 * @date 2022/8/17
 */
public enum RoomStatusEnum {
    /**
     * 待匹配
     */
    IDLE,
    /**
     * 游戏中
     */
    IN_GAME,
    /**
     * 游戏结束
     */
    GAME_OVER,
    ;

    public static RoomStatusEnum getStatusEnum(String status) {
        switch (status) {
            case "IDLE":
                return IDLE;
            case "IN_GAME":
                return IN_GAME;
            case "GAME_OVER":
                return GAME_OVER;
            default:
                throw new RuntimeException("系统异常");
        }
    }

    public String getValue() {
        return this.name();
    }
}
