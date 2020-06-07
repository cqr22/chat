package com.codeman22.chat.service.impl;

import com.codeman22.chat.dao.ChatMsgMapper;
import com.codeman22.chat.dao.FriendRequestMapper;
import com.codeman22.chat.dao.MyFriendMapper;
import com.codeman22.chat.dao.UserMapper;
import com.codeman22.chat.dto.FriendRequestMsg;
import com.codeman22.chat.dto.MyFriendsMsg;
import com.codeman22.chat.dto.UserFace;
import com.codeman22.chat.dto.UserMsg;
import com.codeman22.chat.entity.FriendRequest;
import com.codeman22.chat.entity.MyFriend;
import com.codeman22.chat.entity.User;
import com.codeman22.chat.netty.ChatMsg;
import com.codeman22.chat.netty.DataContent;
import com.codeman22.chat.netty.UserChannelRelation;
import com.codeman22.chat.service.UserService;
import com.codeman22.chat.utils.*;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 *
 * @author Kuzma
 * @date 2020/5/19
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MyFriendMapper myFriendMapper;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Autowired
    private QrCodeUtils qrCodeUtils;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private FastDFSClient fastDfsClient;


    @Override
    public Result registerOrLogin(User user) throws Exception {
        User userTemp = userMapper.getUserByName(user.getUserName());
        //进行注册
        if (userTemp==null){
            userTemp = new User();
            // 使用雪花算法生成唯一Id
            String id = String.valueOf(SnowflakeIdUtil.getSnowflakeId());
            // 为每个用户生成一个唯一的二维码
            String qrCodePath =  id + "qrcode.png";
            qrCodeUtils.createQrCode(qrCodePath,"codeMan_qrcode:"+user.getUserName());
            MultipartFile qrCodeFile = FileUtils.fileToMultipart(qrCodePath);
            String qrCodeUrl = "";
            try {
                qrCodeUrl = fastDFSClient.uploadQRCode(qrCodeFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            userTemp.setUserName(user.getUserName());
            userTemp.setId(String.valueOf(id));
            userTemp.setNickName(user.getUserName());
            userTemp.setFaceImage(Constant.DEFAULT_AVATAR);
            userTemp.setFaceImageBig(Constant.DEFAULT_AVATAR_BIG);
            userTemp.setPassword(MD5Utils.getMd5Str(user.getPassword()));
            userTemp.setQrCode(qrCodeUrl);
            userTemp.setCid(user.getCid());
            userTemp = saveUser(userTemp);
        }else {
            if (!userTemp.getPassword().equals(MD5Utils.getMd5Str(user.getPassword()))) {
                return Result.errorMsg("用户名或密码不正确");
            }
        }
        UserMsg userMsg = new UserMsg();
        BeanUtils.copyProperties(userTemp,userMsg);
        return Result.ok(userMsg);
    }

    @Override
    public Result uploadFaceBase64(UserFace userFace) throws Exception {
        // 获取前端传过来的base64字符串, 然后转换为文件对象再上传
        String base64Data = userFace.getFaceData();
        String userFacePath = userFace.getUserId() + "userface64.png";
        FileUtils.base64ToFile(userFacePath, base64Data);

        // 上传文件到fastdfs
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDfsClient.uploadBase64(faceFile);
        System.out.println(url);

        // 获取缩略图的url
        String thump = "_80x80.";
        String[] arr = url.split("\\.");
        String thumpImgUrl = arr[0] + thump + arr[1];

        //更新用户头像
        User user = userMapper.selectByPrimaryKey(userFace.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);
        return Result.ok(updateUserInfo(user));
    }

    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    @Override
    public User updateUserInfo(User user) {
        userMapper.updateByPrimaryKeySelective(user);
        user = userMapper.selectByPrimaryKey(user.getId());
        user.setPassword("");
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    @Override
    public User saveUser(User user) {
        userMapper.insert(user);
        return user;
    }

    @Override
    public Result setNickName(UserFace userFace) {
        User user = userMapper.selectByPrimaryKey(userFace.getUserId());
        user.setNickName(userFace.getNickName());
        return Result.ok(updateUserInfo(user));
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public Result searchUser(String myUserId, String friendUserName) {

        // 参数不能为空
        if (Objects.isNull(myUserId)||Objects.isNull(friendUserName)){
            return Result.errorMsg("参数不能为空");
        }

        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer status = searchFriends(myUserId, friendUserName);

        if (status.equals(SearchFriendsStatusEnum.SUCCESS.status)) {
            User user = userMapper.getUserByName(friendUserName);
            UserMsg userMsg = new UserMsg();
            BeanUtils.copyProperties(user, userMsg);
            return Result.ok(userMsg);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return Result.errorMsg(errorMsg);
        }

    }


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer searchFriends(String myUserId, String friendUserName) {

        User user = userMapper.getUserByName(friendUserName);

        // 1. 搜索的用户如果不存在，返回[无此用户]
        if (user == null) {
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }

        // 2. 搜索账号是你自己，返回[不能添加自己]
        if (user.getId().equals(myUserId)) {
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }

        // 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        MyFriend myFriend = myFriendMapper.selectByRelation(myUserId,user.getId());
        if (myFriend != null){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }

        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    @Override
    public Result addFriendRequest(String myUserId, String friendUserName) {

        // 参数不能为空
        if (Objects.isNull(myUserId)||Objects.isNull(friendUserName)){
            return Result.errorMsg("参数不能为空");
        }

        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer status = searchFriends(myUserId, friendUserName);

        if (status.equals(SearchFriendsStatusEnum.SUCCESS.status)) {
            sendFriendRequest(myUserId,friendUserName);
            return Result.ok();
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return Result.errorMsg(errorMsg);
        }

    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void sendFriendRequest(String myUserId, String friendUserName) {
        User friend = userMapper.getUserByName(friendUserName);
        FriendRequest friendRequest = friendRequestMapper.selectByUserIdAndFriendId(myUserId,friend.getId());
        //避免重复插入同一通知
        if (friendRequest == null){

            // 使用雪花算法生成唯一Id
            String id = String.valueOf(SnowflakeIdUtil.getSnowflakeId());

            friendRequest = new FriendRequest();
            friendRequest.setId(id);

            // 接收者
            friendRequest.setAcceptUserId(friend.getId());
            //发送者
            friendRequest.setSendUserId(myUserId);
            friendRequest.setRequestDateTime(new Date());

            friendRequestMapper.insert(friendRequest);
        }

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendRequestMsg> listFriendRequest(String acceptUserId) {
        return userMapper.listFriendRequest(acceptUserId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result operateFriendRequest(String acceptUserId, String sendUserId, Integer operateType) {

         if (Objects.isNull(acceptUserId)||Objects.isNull(sendUserId)||Objects.isNull(operateType)){
             return Result.errorMsg("缺少参数");
         }

         if (Objects.isNull(OperatorFriendRequestTypeEnum.getMsgByType(operateType))){
             return Result.errorMsg("参数错误");
         }

        if (operateType.equals(OperatorFriendRequestTypeEnum.PASS.type)){
             //同意 互增好友记录
            saveFriend(sendUserId,acceptUserId);
            saveFriend(acceptUserId,sendUserId);

            Channel sendChannel = UserChannelRelation.get(sendUserId);
            if (sendChannel != null) {
                // 使用webSocket主动推送消息到请求发起者，更新他的通讯录列表为最新
                DataContent dataContent = new DataContent();
                dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);

                sendChannel.writeAndFlush(
                        new TextWebSocketFrame(
                                JsonUtils.objectToJson(dataContent)));
            }
         }
         // 无论同意与否最后都会删除记录
        friendRequestMapper.deleteFriendRequest(sendUserId,acceptUserId);

        return Result.ok(myFriendMapper.listMyFriends(acceptUserId));
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void saveFriend(String sendUserId, String acceptUserId){
        MyFriend myFriend1 = new MyFriend();
        // 使用雪花算法生成唯一Id
        myFriend1.setId(String.valueOf(SnowflakeIdUtil.getSnowflakeId()));
        myFriend1.setMyFriendId(acceptUserId);
        myFriend1.setMyId(sendUserId);
        myFriendMapper.insert(myFriend1);
    }

    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    @Override
    public List<MyFriendsMsg> listMyFriends(String userId) {
        return myFriendMapper.listMyFriends(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public String saveMsg(ChatMsg chatMsg) {
        com.codeman22.chat.entity.ChatMsg msg = new com.codeman22.chat.entity.ChatMsg();

        // 使用雪花算法生成唯一Id
        msg.setId(String.valueOf(SnowflakeIdUtil.getSnowflakeId()));
        msg.setAcceptUserId(chatMsg.getReceiverId());
        msg.setSendUserId(chatMsg.getSenderId());
        msg.setCreateTime(new Date());
        msg.setIsRead(false);
        msg.setMsg(chatMsg.getMsg());

        chatMsgMapper.insert(msg);
        return msg.getId();
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        chatMsgMapper.batchUpdateMsgSigned(msgIdList);
    }

    @Override
    public Result getUnReadMsgList(@NotNull String userId) {
        return Result.ok(chatMsgMapper.getUnReadMsgList(userId));
    }

}
