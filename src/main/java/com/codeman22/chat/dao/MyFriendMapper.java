package com.codeman22.chat.dao;

import com.codeman22.chat.dto.MyFriendsMsg;
import com.codeman22.chat.entity.MyFriend;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MyFriendMapper {
    int deleteByPrimaryKey(String id);

    int insert(MyFriend record);

    int insertSelective(MyFriend record);

    MyFriend selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(MyFriend record);

    int updateByPrimaryKey(MyFriend record);

    /**
     * @param myId
     * @param myFriendId
     * @return 用户与另一用户的好友关系
     */
    MyFriend selectByRelation(@Param("myId") String myId, @Param("myFriendId") String myFriendId);

    /**
     * @param userId
     * @return 返回用户朋友列表
     */
    List<MyFriendsMsg> listMyFriends(@Param("myId") String userId);
}