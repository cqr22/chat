package com.codeman22.chat.dao;

import com.codeman22.chat.entity.ChatMsg;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMsgMapper {
    int deleteByPrimaryKey(String id);

    int insert(ChatMsg record);

    int insertSelective(ChatMsg record);

    ChatMsg selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChatMsg record);

    int updateByPrimaryKey(ChatMsg record);

    /**
     * 批量处理消息为已签收
     * @param msgIdList
     */
    void batchUpdateMsgSigned(List<String> msgIdList);

    List<ChatMsg> getUnReadMsgList(@Param("acceptUserId") String acceptUserId);
}