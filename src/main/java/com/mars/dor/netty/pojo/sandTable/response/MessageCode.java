package com.mars.dor.netty.pojo.sandTable.response;

import lombok.Getter;

/**
 * 响应码
 *
 * @author yeeq
 * @date 2021/3/27
 */
@Getter
public enum MessageCode {

    /**
     * 响应码
     */
    FAIL(100,"操作失败"),
    SUCCESS(200, "操作成功"),
    USER_RE_LINK(201, "用户重连"),
    CURRENT_USER_IS_IN_GAME(202, "当前用户已在游戏中"),
    MESSAGE_ERROR(203, "消息错误"),
    CANCEL_MATCH_ERROR(204, "用户取消了匹配"),
    HEART_BEAT_RESP(205, "心跳回复");

    private final Integer code;
    private final String desc;

    MessageCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
