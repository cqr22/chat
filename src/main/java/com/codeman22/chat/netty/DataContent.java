package com.codeman22.chat.netty;

import java.io.Serializable;

public class DataContent implements Serializable {

	private static final long serialVersionUID = 8021381444738260454L;

	// 动作类型
	private Integer action;

    // 用户的聊天内容entity
	private ChatMsg chatMsg;

    // 扩展字段
	private String extend;		
	
	public Integer getAction() {
		return action;
	}

	public void setAction(Integer action) {
		this.action = action;
	}

	public ChatMsg getChatMsg() {
		return chatMsg;
	}

	public void setChatMsg(ChatMsg chatMsg) {
		this.chatMsg = chatMsg;
	}

	public String getExtand() {
		return extend;
	}

	public void setExtand(String extand) {
		this.extend = extand;
	}
}
