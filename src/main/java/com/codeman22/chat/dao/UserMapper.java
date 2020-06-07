package com.codeman22.chat.dao;

import com.codeman22.chat.dto.FriendRequestMsg;
import com.codeman22.chat.entity.User;

import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(String id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User getUserByName(String userName);

    /**
     * @param acceptUserId
     * @return 好友申请记录
     */
    List<FriendRequestMsg> listFriendRequest(String acceptUserId);


}