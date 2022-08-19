package com.mars.dor.netty.pojo.sandTable.enums;

/**
 * @author zhuangqingdian
 * @date 2022/8/17
 */
public enum EnumRedisKey {
    /**
     * userOnline 在线状态
     */
    SAND_USER_STATUS,
    /**
     * userOnline 对局信息
     */
    SAND_USER_IN_PLAY,
    /**
     * userOnline 匹配信息
     */
    SAND_USER_MATCH_INFO,
    /**
     * 匹配房间
     */
    SAND_BATTLE,
    /**
     * 自定义房间
     */
    SAND_ROOM_INFO
    ;

    public String getKey() {
        return this.name();
    }
}
