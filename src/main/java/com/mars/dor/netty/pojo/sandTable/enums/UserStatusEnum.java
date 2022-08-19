package com.mars.dor.netty.pojo.sandTable.enums;

/**
 * 用户状态
 * @author zhuangqingdian
 * @date 2022/8/17
 */
public enum UserStatusEnum {
    /**
     * 待匹配
     */
    IDLE,
    /**
     * 匹配中
     */
    IN_MATCH,
    /**
     * 在自定义房间中
     */
    IN_ROOM,
    /**
     * 游戏中
     */
    IN_GAME,

    ;

    public static UserStatusEnum getStatusEnum(String status) {
        switch (status) {
            case "IDLE":
                return IDLE;
            case "IN_MATCH":
                return IN_MATCH;
            case "IN_GAME":
                return IN_GAME;
            default:
                throw new RuntimeException("系统异常");
        }
    }

    public String getValue() {
        return this.name();
    }
}
