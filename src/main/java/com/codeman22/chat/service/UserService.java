package com.codeman22.chat.service;

import com.codeman22.chat.netty.ChatMsg;
import com.codeman22.chat.dto.FriendRequestMsg;
import com.codeman22.chat.dto.MyFriendsMsg;
import com.codeman22.chat.dto.UserFace;
import com.codeman22.chat.entity.User;
import com.codeman22.chat.utils.Result;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 *
 * @author Kuzma
 * @date 2020/5/19
 */
public interface UserService {

    Result registerOrLogin(User user) throws Exception;

    Result uploadFaceBase64(UserFace userFace) throws Exception;

    User updateUserInfo(User user);

    User saveUser(User user);

    Result setNickName(UserFace user);

    Result searchUser(String myUserId, String friendUserName);

    Integer searchFriends(String myUserId, String friendUsername);

    Result addFriendRequest(String myUserId, String friendUserName);

    void sendFriendRequest(String myUserId, String friendUserName);

    List<FriendRequestMsg> listFriendRequest(String acceptUserId);

    Result operateFriendRequest(String acceptUserId, String sendUserId, Integer operateType);

    void saveFriend(String sendUserId, String acceptUserId);

    List<MyFriendsMsg> listMyFriends(String userId);

    String saveMsg(ChatMsg chatMsg);

    void updateMsgSigned(List<String> msgIdList);

    Result getUnReadMsgList(@NotNull String userId);
}
