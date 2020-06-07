package com.codeman22.chat.entity;

public class MyFriend {
    private String id;

    private String myId;

    private String myFriendId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId == null ? null : myId.trim();
    }

    public String getMyFriendId() {
        return myFriendId;
    }

    public void setMyFriendId(String myFriendId) {
        this.myFriendId = myFriendId == null ? null : myFriendId.trim();
    }

    @Override
    public String toString() {
        return "MyFriend{" +
                "id='" + id + '\'' +
                ", myId='" + myId + '\'' +
                ", myFriendId='" + myFriendId + '\'' +
                '}';
    }
}