package com.codeman22.chat.controller;

import com.codeman22.chat.dto.UserFace;
import com.codeman22.chat.entity.User;
import com.codeman22.chat.service.UserService;
import com.codeman22.chat.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Kuzma
 * @date 2020/5/19
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 登录注册统一接口 有则登录 无则注册
     * @param user
     * @return
     * @throws Exception
     */
    @PostMapping("/registerOrLogin")
    public Result registerOrLogin(@NotBlank(message = "用户名或密码不能为空")@RequestBody User user) throws Exception {
        return userService.registerOrLogin(user);
    }

    /**
     * 上传用户头像
     * @param userFace
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadFaceBase64")
    public Result uploadFaceBase64(@RequestBody UserFace userFace) throws Exception {
        return userService.uploadFaceBase64(userFace);
    }

    /**
     * 用户修改昵称
     * @param user
     * @return
     */
    @PostMapping("/setNickName")
    public Result setNickName(@RequestBody UserFace user){
        return userService.setNickName(user);
    }

    /**
     * 好友搜索
     * @param myUserId
     * @param friendUserName
     * @return
     */
    @PostMapping("/search")
    public Result searchUser(String myUserId,  String friendUserName){
        return userService.searchUser(myUserId,friendUserName);
    }

    /**
     * 发送好友请求
     * @param myUserId
     * @param friendUserName
     * @return
     */
    @PostMapping("/addFriendRequest")
    public Result addFriendRequest(String myUserId,  String friendUserName){
        return userService.addFriendRequest(myUserId,friendUserName);
    }

    /**
     * 请求好友申请列表
     * @param userId
     * @return
     */
    @PostMapping("/queryFriendRequests")
    public Result listFriendQuest(@NotNull String userId){
        return Result.ok(userService.listFriendRequest(userId));
    }

    /**
     * 通过或忽略好友申请
     * @param acceptUserId
     * @param sendUserId
     * @param operateType
     * @return
     */
    @PostMapping("/operateFriendRequest")
    public Result operateFriendRequest(String acceptUserId, String sendUserId, Integer operateType){
        return userService.operateFriendRequest(acceptUserId,sendUserId,operateType);
    }

    /**
     * 请求好友列表
     * @param userId
     * @return
     */
    @PostMapping("/myFriends")
    public Result listMyFriends(@NotNull String userId){
        return Result.ok(userService.listMyFriends(userId));
    }

    /**
     * 获取未读消息
     * @param userId
     * @return
     */
    @PostMapping("/getUnReadMsgList")
    public Result getUnReadMsgList(@NotNull String userId){
        return userService.getUnReadMsgList(userId);
    }
}
