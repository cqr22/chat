package com.codeman22.chat.dto;


public class UserFace {
    private String userId;

    private String faceData;

    private String nickName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFaceData() {
        return faceData;
    }

    public void setFaceData(String faceData) {
        this.faceData = faceData;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickname(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        return "UserFace{" +
                "userId='" + userId + '\'' +
                ", faceData='" + faceData + '\'' +
                ", nickName='" + nickName + '\'' +
                '}';
    }
}
