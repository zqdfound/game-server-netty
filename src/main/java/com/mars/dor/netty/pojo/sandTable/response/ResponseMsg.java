package com.mars.dor.netty.pojo.sandTable.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息返回体
 * @author zhuangqingdian
 * @date 2022/8/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMsg {
    private Integer code;
    private String data;
    private String desc;
}
