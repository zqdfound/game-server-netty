package com.mars.dor.netty.util;

import com.mars.dor.netty.pojo.sandTable.enums.EnumRedisKey;
import com.mars.dor.netty.pojo.sandTable.enums.RoomStatusEnum;
import com.mars.dor.netty.pojo.sandTable.enums.UserStatusEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

/**
 * 匹配操作工具类
 * @author zhuangqingdian
 * @date 2022/8/17
 */
@Component
public class MatchCacheUtil {

    /**
     * key - EnumRedisKey在线状态
     * value - MAP<userId,StatusEnum用户在线状态>
     */
    @Resource
    private RedisTemplate<String, Map<String, String>> redisTemplate;

    /**
     * 移除用户在线状态
     */
    public void removeUserOnlineStatus(String userId) {
        redisTemplate.opsForHash().delete(EnumRedisKey.SAND_USER_STATUS.getKey(), userId);
    }

    /**
     * 获取用户在线状态
     */
    public UserStatusEnum getUserOnlineStatus(String userId) {
        Object status = redisTemplate.opsForHash().get(EnumRedisKey.SAND_USER_STATUS.getKey(), userId);
        if (status == null) {
            return null;
        }
        return UserStatusEnum.getStatusEnum(status.toString());
    }

    /**
     * 设置用户为 空闲 IDLE 状态
     */
    public void setUserStatusIDLE(String userId) {
        removeUserOnlineStatus(userId);
        redisTemplate.opsForHash().put(EnumRedisKey.SAND_USER_STATUS.getKey(), userId, UserStatusEnum.IDLE.getValue());
    }

    /**
     * 设置用户为 IN_MATCH 状态
     */
    public void setUserStatusInMatch(String userId) {
        removeUserOnlineStatus(userId);
        redisTemplate.opsForHash().put(EnumRedisKey.SAND_USER_STATUS.getKey(), userId, UserStatusEnum.IN_MATCH.getValue());
    }

    /**
     * 设置用户为 IN_ROOM 状态
     */
    public void setUserStatusInRoom(String userId) {
        removeUserOnlineStatus(userId);
        redisTemplate.opsForHash().put(EnumRedisKey.SAND_USER_STATUS.getKey(), userId, UserStatusEnum.IN_ROOM.getValue());
    }
    /**
     * 设置用户为 IN_GAME 状态
     */
    public void setUserStatusInGame(String userId) {
        removeUserOnlineStatus(userId);
        redisTemplate.opsForHash().put(EnumRedisKey.SAND_USER_STATUS.getKey(), userId, UserStatusEnum.IN_GAME.getValue());
    }
    /**
     * 随机获取处于匹配状态的用户（除了指定用户外）
     */
    public String getUserInMatchRandom(String userId) {
        Optional<Map.Entry<Object, Object>> any = redisTemplate.opsForHash().entries(EnumRedisKey.SAND_USER_STATUS.getKey())
                .entrySet().stream().filter(entry -> entry.getValue().equals(UserStatusEnum.IN_MATCH.getValue()) && !entry.getKey().equals(userId))
                .findAny();
        return any.map(entry -> entry.getKey().toString()).orElse(null);
    }


    /**
     * 设置处于游戏中的用户在同一对战局
     */
    public void setUserInBattle(String userId1, String userId2) {
        redisTemplate.opsForHash().put(EnumRedisKey.SAND_BATTLE.getKey(), userId1, userId2);
        redisTemplate.opsForHash().put(EnumRedisKey.SAND_BATTLE.getKey(), userId2, userId1);
    }

    /**
     * 从对战局中移除用户
     */
    public void removeUserFromBattle(String userId) {
        redisTemplate.opsForHash().delete(EnumRedisKey.SAND_BATTLE.getKey(), userId);
    }

    /**
     * 从对战局中获取用户
     */
    public String getUserFromBattle(String userId) {
        return redisTemplate.opsForHash().get(EnumRedisKey.SAND_BATTLE.getKey(), userId).toString();
    }

    /**
     * 设置处于游戏中的用户的对战信息
     */
    public void setUserMatchInfo(String userId, String userMatchInfo) {
        redisTemplate.opsForHash().put(EnumRedisKey.SAND_USER_MATCH_INFO.getKey(), userId, userMatchInfo);
    }

    /**
     * 移除处于游戏中的用户的对战信息
     */
    public void removeUserMatchInfo(String userId) {
        redisTemplate.opsForHash().delete(EnumRedisKey.SAND_USER_MATCH_INFO.getKey(), userId);
    }

    /**
     * 设置处于游戏中的用户的对战信息
     */
    public String getUserMatchInfo(String userId) {
        return redisTemplate.opsForHash().get(EnumRedisKey.SAND_USER_MATCH_INFO.getKey(), userId).toString();
    }



    public void cleanAll(){
        redisTemplate.delete(EnumRedisKey.SAND_USER_STATUS.getKey());
        redisTemplate.delete(EnumRedisKey.SAND_USER_IN_PLAY.getKey());
        redisTemplate.delete(EnumRedisKey.SAND_USER_MATCH_INFO.getKey());
        redisTemplate.delete(EnumRedisKey.SAND_BATTLE.getKey());
    }

    /**
     * 创建新房间
     */
    public void createIdleRoom(String userId, String roomNo) {
        String key = EnumRedisKey.SAND_ROOM_INFO+":"+roomNo;
        String status = RoomStatusEnum.IDLE.getValue();
        redisTemplate.opsForHash().put(key,"roomNo",roomNo);
        redisTemplate.opsForHash().put(key,"owner",userId);
        redisTemplate.opsForHash().put(key,"status",status);
    }
    public String getRoom(String roomNo) {
        String key = EnumRedisKey.SAND_ROOM_INFO+":"+roomNo;
        Object room = redisTemplate.opsForHash().get(key,"roomNo");
        if(room == null){
            return null;
        }
        return (String) room;
    }

    /**
     * 获取房间状态
     */
    public RoomStatusEnum getRoomStatus(String roomNo) {
        String key = EnumRedisKey.SAND_ROOM_INFO+":"+roomNo;
        Object status = redisTemplate.opsForHash().get(key, "status");
        if (status == null) {
            return null;
        }
        return RoomStatusEnum.getStatusEnum(status.toString());
    }

    /**
     * 设置房间状态
     */
    public void setRoomStatus(String roomNo,String value) {
        String key = EnumRedisKey.SAND_ROOM_INFO+":"+roomNo;
        redisTemplate.opsForHash().put(key,"status",value);
    }

    /**
     * 设置对手
     */
    public void setRoomReceiver(String roomNo, String receiver) {
        String key = EnumRedisKey.SAND_ROOM_INFO+":"+roomNo;
        redisTemplate.opsForHash().put(key,"receiver",receiver);
    }

    public String getRoomOwner(String roomNo) {
        String key = EnumRedisKey.SAND_ROOM_INFO+":"+roomNo;
        Object owner = redisTemplate.opsForHash().get(key, "owner");
        if (owner == null) {
            return null;
        }
        return (String) owner;
    }
    public String getRoomReceiver(String roomNo) {
        String key = EnumRedisKey.SAND_ROOM_INFO+":"+roomNo;
        Object owner = redisTemplate.opsForHash().get(key, "receiver");
        if (owner == null) {
            return null;
        }
        return (String) owner;
    }

    public void closeRoom(String roomNo) {
        String key = EnumRedisKey.SAND_ROOM_INFO+":"+roomNo;
        redisTemplate.delete(key);
    }

    //移除房客
    public void removeRoomReceiver(String roomNo) {
        String key = EnumRedisKey.SAND_ROOM_INFO+":"+roomNo;
        redisTemplate.opsForHash().delete(key,"receiver");
    }
}
