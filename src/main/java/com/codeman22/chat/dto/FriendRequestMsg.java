package com.codeman22.chat.dto;

/**
 * 好友请求消息
 * @author Kuzma
 * @date 2020/5/31
 */
public class FriendRequestMsg {

    private String sendUserId;

    private String sendUserName;

    private String sendFaceImage;

    private String sendNickName;

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getSendUserName() {
        return sendUserName;
    }

    public void setSendUserName(String sendUserName) {
        this.sendUserName = sendUserName;
    }

    public String getSendFaceImage() {
        return sendFaceImage;
    }

    public void setSendFaceImage(String sendFaceImage) {
        this.sendFaceImage = sendFaceImage;
    }

    public String getSendNickName() {
        return sendNickName;
    }

    public void setSendNickName(String sendNickName) {
        this.sendNickName = sendNickName;
    }

    @Override
    public String toString() {
        return "FriendRequestMsg{" +
                "sendUserId='" + sendUserId + '\'' +
                ", sendUserName='" + sendUserName + '\'' +
                ", sendFaceImage='" + sendFaceImage + '\'' +
                ", sendNickName='" + sendNickName + '\'' +
                '}';
    }
}
