package com.codeman22.chat.dao;

import com.codeman22.chat.entity.FriendRequest;
import org.apache.ibatis.annotations.Param;

public interface FriendRequestMapper {
    int deleteByPrimaryKey(String id);

    int insert(FriendRequest record);

    int insertSelective(FriendRequest record);

    FriendRequest selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FriendRequest record);

    int updateByPrimaryKey(FriendRequest record);

    /**
     * @param sendUserId
     * @param acceptUserId
     * @return 发送者跟接收者的好友申请
     */
    FriendRequest selectByUserIdAndFriendId(@Param("sendUserId") String sendUserId, @Param("acceptUserId") String acceptUserId);

    /**
     * @param sendUserId
     * @param acceptUserId
     * @return 删除好友申请记录
     */
    int deleteFriendRequest(@Param("sendUserId")String sendUserId,@Param("acceptUserId") String acceptUserId);
}