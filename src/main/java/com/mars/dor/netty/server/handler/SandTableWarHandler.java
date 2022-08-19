package com.mars.dor.netty.server.handler;

import com.alibaba.fastjson.JSON;
import com.mars.dor.netty.pojo.sandTable.RequestMsg;
import com.mars.dor.netty.pojo.sandTable.enums.MessageTypeEnum;
import com.mars.dor.netty.pojo.sandTable.enums.RoomStatusEnum;
import com.mars.dor.netty.pojo.sandTable.enums.UserStatusEnum;
import com.mars.dor.netty.pojo.sandTable.response.MessageCode;
import com.mars.dor.netty.pojo.sandTable.response.ResponseMsg;
import com.mars.dor.netty.server.config.NettyConfig;
import com.mars.dor.netty.util.SpringUtils;
import com.mars.dor.netty.util.MatchCacheUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 沙盘游戏handler
 *
 * @author zhuangqingdian
 * @date 2022/8/16
 */
@Slf4j
public class SandTableWarHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    static Lock lock = new ReentrantLock();
    static Condition matchCond = lock.newCondition();


    /**
     * 一旦客户端连接上来，该方法被执行
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("触发新的连接，channelId:" + ctx.channel().id().asLongText());
    }

    /**
     * 断开连接，需要移除用户,清除redis
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        removeUserId(ctx);
    }


    /**
     * 异常，关闭通道
     *
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        if (cause.getMessage().startsWith("远程主机强迫关闭了一个现有的连接") == false) {
            InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
            log.error("【链接异常断开】, ip = {}, exception = ", inSocket.getAddress().getHostAddress(), cause);
        }
    }

    /**
     * 消息读取
     *
     * @param channelHandlerContext
     * @param textWebSocketFrame
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        //传过来的是json字符串
        String text = textWebSocketFrame.text();
        log.info("接收消息.." + text);
        RequestMsg requestMsg = JSON.parseObject(text, RequestMsg.class);
        Channel channel = channelHandlerContext.channel();
        delWithMsg(requestMsg, channel);
    }
//------------------------------------------------------------------------------------------------------------//

    /**
     * 消息处理
     *
     * @param requestMsg
     * @param channel
     */
    private void delWithMsg(RequestMsg requestMsg, Channel channel) {
        String msgType = requestMsg.getMsgType();
        MatchCacheUtil matchCacheUtil = SpringUtils.getBean(MatchCacheUtil.class);
        if (msgType.equals(MessageTypeEnum.HEART_BEAT.getValue())) {
            //心跳检测
            answerHeartBeat(channel);
        } else if (msgType.equals(MessageTypeEnum.ADD_USER.getValue())) {
            //新加入
            addUser(matchCacheUtil, channel, requestMsg.getUserId());
        } else if (msgType.equals(MessageTypeEnum.MATCH_USER.getValue())) {
            //匹配玩家
            matchUser(matchCacheUtil, channel, requestMsg.getUserId());
        } else if (msgType.equals(MessageTypeEnum.CANCEL_MATCH.getValue())) {
            //取消匹配
            cancelMatch(matchCacheUtil, channel, requestMsg.getUserId());
        } else if (msgType.equals(MessageTypeEnum.PLAY_GAME.getValue())) {
            //进行游戏
            toPlay(matchCacheUtil, channel, requestMsg.getUserId());
        } else if (msgType.equals(MessageTypeEnum.GAME_OVER.getValue())) {
            //结束游戏
            gameOver(matchCacheUtil, channel, requestMsg.getUserId());
        } else if (msgType.equals(MessageTypeEnum.CREATE_ROOM.getValue())) {
            //创建房间
            createRoom(matchCacheUtil, channel, requestMsg);
        } else if (msgType.equals(MessageTypeEnum.JOIN_ROOM.getValue())) {
            //加入房间
            joinRoom(matchCacheUtil, channel, requestMsg);
        } else if(msgType.equals(MessageTypeEnum.QUIT_ROOM.getValue())){
            //退出房间
            quitRoom(matchCacheUtil,channel,requestMsg);
        }else if(msgType.equals(MessageTypeEnum.KICK_OUT.getValue())){
            //房主踢人
            kickOut(matchCacheUtil,channel,requestMsg);
        }else {
            log.error("无法识别的消息");
            ResponseMsg resp = new ResponseMsg();
            resp.setCode(MessageCode.MESSAGE_ERROR.getCode());
            resp.setDesc("错误的消息类型：" + msgType);
            writeMsg(JSON.toJSONString(resp), channel);
        }
    }

    /**
     * 房主踢人
     * @param matchCacheUtil
     * @param channel
     * @param requestMsg
     */
    private void kickOut(MatchCacheUtil matchCacheUtil, Channel channel, RequestMsg requestMsg) {
        String userId = requestMsg.getUserId();
        String roomNo = requestMsg.getRoomNo();
        if(userId.equals(matchCacheUtil.getRoomOwner(roomNo))){
            String receiver = matchCacheUtil.getRoomReceiver(roomNo);
            //重置房客为空闲，房主为在房中，房间删除房客，房间状态重置为空闲 通知双方
            matchCacheUtil.setUserStatusIDLE(receiver);
            matchCacheUtil.setUserStatusInRoom(userId);
            matchCacheUtil.removeRoomReceiver(roomNo);
            matchCacheUtil.setRoomStatus(roomNo,RoomStatusEnum.IDLE.getValue());
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.SUCCESS.getCode())
                    .desc("已将"+receiver+"移出房间")
                    .build()), channel);
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.FAIL.getCode())
                    .desc("你已被房主移出房间")
                    .build()), NettyConfig.userChannelMap.get(receiver));
        }else{
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.FAIL.getCode())
                    .desc("你不是房主，不能踢人")
                    .build()), channel);
        }
    }

    /**
     * 退出房间
     * @param matchCacheUtil
     * @param channel
     * @param requestMsg
     */
    private void quitRoom(MatchCacheUtil matchCacheUtil, Channel channel, RequestMsg requestMsg) {
        String userId = requestMsg.getUserId();
        String roomNo = requestMsg.getRoomNo();
        if(StringUtil.isNullOrEmpty(userId)|| StringUtil.isNullOrEmpty(roomNo)){
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.FAIL.getCode())
                    .desc("参数缺失")
                    .build()), channel);
            return;
        }
        String roomOwner = matchCacheUtil.getRoomOwner(roomNo);
        String receiver = matchCacheUtil.getRoomReceiver(roomNo);
        //房主退出，重制用户为空闲，解散房间，通知双方
        lock.lock();
        try{
            if(userId.equals(roomOwner)){
                matchCacheUtil.setUserStatusIDLE(userId);
                writeMsg(JSON.toJSONString(ResponseMsg.builder()
                        .code(MessageCode.SUCCESS.getCode())
                        .desc("房主退出房间，房间解散")
                        .build()), channel);
                //如果房间内有房客，房客也置为空闲
                if(receiver != null){
                    matchCacheUtil.setUserStatusIDLE(receiver);
                    writeMsg(JSON.toJSONString(ResponseMsg.builder()
                            .code(MessageCode.SUCCESS.getCode())
                            .desc("房主退出房间，房间解散")
                            .build()), NettyConfig.userChannelMap.get(receiver));
                }
                matchCacheUtil.closeRoom(roomNo);
            }
            //房客退出 重置房客 房间 为空闲状态，移除房间房客信息,通知双方
            if(userId.equals(receiver)){
                matchCacheUtil.setUserStatusIDLE(receiver);
                matchCacheUtil.setRoomStatus(roomNo,RoomStatusEnum.IDLE.getValue());
                matchCacheUtil.removeRoomReceiver(roomNo);
                writeMsg(JSON.toJSONString(ResponseMsg.builder()
                        .code(MessageCode.SUCCESS.getCode())
                        .desc("房客"+receiver+"退出房间")
                        .build()), NettyConfig.userChannelMap.get(roomOwner));

                matchCacheUtil.setUserStatusInRoom(roomOwner);
                writeMsg(JSON.toJSONString(ResponseMsg.builder()
                        .code(MessageCode.SUCCESS.getCode())
                        .desc("房客"+receiver+"退出房间")
                        .build()), NettyConfig.userChannelMap.get(receiver));
            }
        }finally {
            lock.unlock();
        }

    }


    /**
     * 加入房间
     * @param matchCacheUtil
     * @param channel
     * @param requestMsg
     */
    private void joinRoom(MatchCacheUtil matchCacheUtil, Channel channel, RequestMsg requestMsg) {
        String userId = requestMsg.getUserId();
        String roomNo = requestMsg.getRoomNo();
        if (matchCacheUtil.getUserOnlineStatus(userId).compareTo(UserStatusEnum.IN_GAME) == 0) {
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.FAIL.getCode())
                    .desc("你正在对局中，不能加入其他房间")
                    .build()), channel);
            return;
        }
        if (StringUtil.isNullOrEmpty(roomNo)) {
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.FAIL.getCode())
                    .desc("请输入房间号")
                    .build()), channel);
            return;
        }
        if (null == matchCacheUtil.getRoom(roomNo)) {
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.FAIL.getCode())
                    .desc("房间号不存在，请重新输入")
                    .build()), channel);
            return;
        }
        if (userId.equals(matchCacheUtil.getRoomOwner(roomNo))) {
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.FAIL.getCode())
                    .desc("不能加入自己创建的房间")
                    .build()), channel);
            return;
        }
        if (matchCacheUtil.getRoomStatus(roomNo).compareTo(RoomStatusEnum.IDLE) != 0) {
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.FAIL.getCode())
                    .desc("该房间已满，无法加入")
                    .build()), channel);
            return;
        }
        log.debug("用户{}加入房间{}", userId, roomNo);
        //设置房间对手，房间状态为对战中
        String owner = matchCacheUtil.getRoomOwner(roomNo);
        matchCacheUtil.setRoomStatus(roomNo, RoomStatusEnum.IN_GAME.getValue());
        matchCacheUtil.setRoomReceiver(roomNo, userId);
        matchCacheUtil.setUserStatusInGame(userId);
        matchCacheUtil.setUserStatusInGame(owner);

        writeMsg(JSON.toJSONString(ResponseMsg.builder()
                .code(MessageCode.SUCCESS.getCode())
                .desc("成功加入房间:" + roomNo)
                .build()), channel);

        writeMsg(JSON.toJSONString(ResponseMsg.builder()
                .code(MessageCode.SUCCESS.getCode())
                .desc("用户"+userId+"成功加入了房间:" + roomNo)
                .build()), NettyConfig.userChannelMap.get(owner));
    }

    /**
     * 创建房间
     *
     * @param matchCacheUtil
     * @param channel
     * @param requestMsg
     */
    private void createRoom(MatchCacheUtil matchCacheUtil, Channel channel, RequestMsg requestMsg) {
        String userId = requestMsg.getUserId();
        String roomNo = requestMsg.getRoomNo();

        if (StringUtil.isNullOrEmpty(roomNo)) {
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.FAIL.getCode())
                    .desc("请输入房间号")
                    .build()), channel);
            return;
        }
        if (null != matchCacheUtil.getRoom(roomNo)) {
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.FAIL.getCode())
                    .desc("房间号已被占用，请重新输入")
                    .build()), channel);
            return;
        }
        log.debug("用户{}开始创建房间{}", userId, roomNo);
        register(userId, channel);
        if (matchCacheUtil.getUserOnlineStatus(userId).compareTo(UserStatusEnum.IN_GAME) == 0) {
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.FAIL.getCode())
                    .desc("你正在对局中，不能创建房间")
                    .build()), channel);
            return;
        }
        //创建房间，设置房主为对战中
        matchCacheUtil.createIdleRoom(userId, roomNo);
        matchCacheUtil.setUserStatusInRoom(userId);

        writeMsg(JSON.toJSONString(ResponseMsg.builder()
                .code(MessageCode.SUCCESS.getCode())
                .desc("成功创建房间，房号：" + roomNo)
                .build()), channel);
    }

    /**
     * 心跳检测回复
     *
     * @param channel
     */
    private void answerHeartBeat(Channel channel) {
        ResponseMsg resp = new ResponseMsg();
        resp.setCode(MessageCode.HEART_BEAT_RESP.getCode());
        resp.setDesc(MessageCode.HEART_BEAT_RESP.getDesc());
        writeMsg(JSON.toJSONString(resp), channel);
    }

    /**
     * 结束游戏
     *
     * @param matchCacheUtil
     * @param channel
     * @param userId
     */
    private void gameOver(MatchCacheUtil matchCacheUtil, Channel channel, String userId) {
        log.info("ChatWebsocket gameover 用户对局结束 userId: {},");
        register(userId, channel);
        String receiver = matchCacheUtil.getUserFromBattle(userId);
        lock.lock();
        try {
            //重置对战双方为空闲，删除对战信息，并通知
            matchCacheUtil.setUserStatusIDLE(userId);
            matchCacheUtil.setUserStatusIDLE(receiver);
            matchCacheUtil.removeUserFromBattle(userId);
            matchCacheUtil.removeUserFromBattle(receiver);
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.SUCCESS.getCode())
                    .desc("用户"+userId+"离开了对战")
                    .build()), channel);

            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.SUCCESS.getCode())
                    .desc("用户"+userId+"离开了对战")
                    .build()), NettyConfig.userChannelMap.get(receiver));
        } finally {
            lock.unlock();
        }

        log.info("ChatWebsocket gameover 对局 [{} - {}] 结束", userId, receiver);
    }

    /**
     * 游戏中
     *
     * @param matchCacheUtil
     * @param channel
     * @param userId
     */
    private void toPlay(MatchCacheUtil matchCacheUtil, Channel channel, String userId) {
        log.info("ChatWebsocket toPlay 用户更新对局信息开始 userId: {}", userId);
        register(userId, channel);
        ResponseMsg responseMsg = new ResponseMsg();

        String receiver = matchCacheUtil.getUserFromBattle(userId);
        matchCacheUtil.setUserMatchInfo(userId, JSON.toJSONString("正在战斗冲啊！"));


        responseMsg.setCode(MessageCode.SUCCESS.getCode());
        responseMsg.setDesc("正在和" + receiver + "激烈战斗");

        writeMsg(JSON.toJSONString(responseMsg), channel);
        log.info("ChatWebsocket toPlay 用户更新对局信息结束 userId: {},");

    }

    /**
     * 取消匹配
     *
     * @param matchCacheUtil
     * @param channel
     * @param userId
     */
    private void cancelMatch(MatchCacheUtil matchCacheUtil, Channel channel, String userId) {
        log.info(" cancelMatch 用户取消匹配开始 userId: {}", userId);
        register(userId, channel);
        lock.lock();
        try {
            ResponseMsg responseMsg = new ResponseMsg();
            matchCacheUtil.setUserStatusIDLE(userId);
            responseMsg.setCode(MessageCode.SUCCESS.getCode());
            responseMsg.setDesc(MessageCode.SUCCESS.getDesc());
            writeMsg(JSON.toJSONString(responseMsg), channel);
        } finally {
            lock.unlock();
        }
        log.info(" cancelMatch 用户取消匹配结束 userId: {}", userId);
    }

    /**
     * 匹配用户
     *
     * @param matchCacheUtil
     * @param userId
     */
    private void matchUser(MatchCacheUtil matchCacheUtil, Channel channel, String userId) {
        log.info("用户{}开始匹配对手", userId);
        register(userId, channel);
        ResponseMsg responseMsg = new ResponseMsg();
        lock.lock();
        try {
            if (matchCacheUtil.getUserOnlineStatus(userId).compareTo(UserStatusEnum.IN_GAME) == 0) {
                writeMsg(JSON.toJSONString(ResponseMsg.builder()
                        .code(MessageCode.FAIL.getCode())
                        .desc("你正在对局中，不能进行匹配")
                        .build()), channel);
                return;
            }
            // 设置用户状态为匹配中
            matchCacheUtil.setUserStatusInMatch(userId);
            matchCond.signal();
        } finally {
            lock.unlock();
        }

        // 创建一个异步线程任务，负责匹配其他同样处于匹配状态的其他用户
        Thread matchThread = new Thread(() -> {
            boolean flag = true;
            String receiver = null;
            while (flag) {
                // 获取除自己以外的其他待匹配用户
                lock.lock();
                try {
                    // 当前用户不处于待匹配状态
                    if (matchCacheUtil.getUserOnlineStatus(userId).compareTo(UserStatusEnum.IN_MATCH) != 0) {
                        log.info("当前用户 {} 已退出匹配", userId);
                        return;
                    }
                    // 当前用户取消匹配状态
//                    if (matchCacheUtil.getUserOnlineStatus(userId).compareTo(UserStatusEnum.IDLE) == 0) {
//                        // 当前用户取消匹配
//                        responseMsg.setCode(MessageCode.CANCEL_MATCH_ERROR.getCode());
//                        responseMsg.setDesc(MessageCode.CANCEL_MATCH_ERROR.getDesc());
//                        log.info("当前用户 {} 已取消匹配", userId);
//                        return;
//                    }
                    receiver = matchCacheUtil.getUserInMatchRandom(userId);
                    if (receiver != null) {
                        // 对手不处于待匹配状态
                        if (matchCacheUtil.getUserOnlineStatus(receiver).compareTo(UserStatusEnum.IN_MATCH) != 0) {
                            log.info("当前用户 {}, 匹配对手 {} 已退出匹配状态", userId, receiver);
                        } else {
                            //匹配成功
                            matchCacheUtil.setUserStatusInGame(userId);
                            matchCacheUtil.setUserStatusInGame(receiver);
                            matchCacheUtil.setUserInBattle(userId, receiver);
                            flag = false;
                        }
                    } else {
                        // 如果当前没有待匹配用户，进入等待队列
                        try {
                            log.info("当前用户 {} 无对手可匹配", userId);
                            matchCond.await();
                        } catch (InterruptedException e) {
                            log.error(" 匹配线程 {} 发生异常: {}",
                                    Thread.currentThread().getName(), e.getMessage());
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }

            //向对战双方的channel推送匹配成功消息
            responseMsg.setCode(MessageCode.SUCCESS.getCode());
            responseMsg.setDesc("系统为你匹配到对手,对手userId：" + receiver);
            writeMsg(JSON.toJSONString(responseMsg), channel);
            responseMsg.setDesc("系统为你匹配到对手,对手userId：" + userId);
            writeMsg(JSON.toJSONString(responseMsg), NettyConfig.userChannelMap.get(receiver));

        }, "sand-game-match-thread:" + userId);
        matchThread.start();
    }

    /**
     * 用户加入游戏
     *
     * @param matchCacheUtil
     * @param userId
     */
    private void addUser(MatchCacheUtil matchCacheUtil, Channel channel, String userId) {
        register(userId, channel);
        /*
         * 获取用户的在线状态
         * 如果缓存中没有保存用户状态，表示用户新加入，则设置为在线状态
         * 否则直接返回
         */
        UserStatusEnum status = matchCacheUtil.getUserOnlineStatus(userId);
        if (status == null) {
            matchCacheUtil.setUserStatusIDLE(userId);
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.SUCCESS.getCode())
                    .desc(MessageCode.SUCCESS.getDesc())
                    .build()), channel);
        }else{
            writeMsg(JSON.toJSONString(ResponseMsg.builder()
                    .code(MessageCode.USER_RE_LINK.getCode())
                    .desc(MessageCode.USER_RE_LINK.getDesc())
                    .build()), channel);
        }
        log.info("用户{}加入平台", userId);
    }


    /**
     * 注册绑定userId和当前channel
     *
     * @param userId
     * @param channel
     */
    private void register(String userId, Channel channel) {
        if (!NettyConfig.userChannelMap.containsKey(userId)) { //没有指定的userId
            NettyConfig.userChannelMap.put(userId, channel);
            // 将用户ID作为自定义属性加入到channel中，方便随时channel中获取用户ID
            AttributeKey<String> key = AttributeKey.valueOf("userId");
            channel.attr(key).setIfAbsent(userId);
        }
    }

    /**
     * 移除用户
     *
     * @param ctx
     */
    private void removeUserId(ChannelHandlerContext ctx) {
        //获取当前channel对应的userid
        Channel channel = ctx.channel();
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        String userId = channel.attr(key).get();
        NettyConfig.userChannelMap.remove(userId);
        log.info("用户下线,userId：{}", userId);
        // 清除redis用户在线信息
//        SpringUtils.getBean(MatchCacheUtil.class).removeUserOnlineStatus(userId);
//        log.info("清除redis用户信息,userId：{}", userId);
    }

    /**
     * 回写文本
     *
     * @param msg     文本
     * @param channel
     */
    private void writeMsg(String msg, Channel channel) {
        if (channel != null && channel.isActive() && channel.isWritable()) {
            channel.writeAndFlush(new TextWebSocketFrame(msg));
        }
    }


}