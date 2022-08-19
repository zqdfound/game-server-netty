package com.mars.dor.netty.pojo.sandTable;

import lombok.Data;

/**
 * 请求实体封装类
 * @author zhuangqingdian
 * @date 2022/8/17
 */
@Data
public class RequestMsg {
    /**
     * 消息类型 参考 MessageTypeEnum
     */
    private String msgType;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 房号
     */
    private String roomNo;
}
