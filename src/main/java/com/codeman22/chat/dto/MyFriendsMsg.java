package com.codeman22.chat.dto;

/**
 *
 * @author Kuzma
 * @date 2020/5/31
 */
public class MyFriendsMsg {
    private String friendUserId;

    private String friendUseName;

    private String friendFaceImage;

    private String friendNickName;

    public String getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getFriendUseName() {
        return friendUseName;
    }

    public void setFriendUseName(String friendUseName) {
        this.friendUseName = friendUseName;
    }

    public String getFriendFaceImage() {
        return friendFaceImage;
    }

    public void setFriendFaceImage(String friendFaceImage) {
        this.friendFaceImage = friendFaceImage;
    }

    public String getFriendNickName() {
        return friendNickName;
    }

    public void setFriendNickName(String friendNickName) {
        this.friendNickName = friendNickName;
    }

    @Override
    public String toString() {
        return "MyFriendsMsg{" +
                "friendUserId='" + friendUserId + '\'' +
                ", friendUseName='" + friendUseName + '\'' +
                ", friendFaceImage='" + friendFaceImage + '\'' +
                ", friendNickName='" + friendNickName + '\'' +
                '}';
    }
}
