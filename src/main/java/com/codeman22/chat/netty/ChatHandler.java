package com.codeman22.chat.netty;

import com.codeman22.chat.service.UserService;
import com.codeman22.chat.utils.JsonUtils;
import com.codeman22.chat.utils.MsgActionEnum;
import com.codeman22.chat.utils.SpringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kuzma
 * @date 2020/5/10
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /**
     * 用于记录和管理所有客户端的channel
     */
    public static ChannelGroup users = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame msg) throws Exception {
        // 客户端传输过来的消息
        String content = msg.text();

        // 获取Channel
        Channel currentChannel = channelHandlerContext.channel();

        // 获取客户端发来的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content,DataContent.class);
        assert dataContent != null;
        Integer action = dataContent.getAction();

        // 判断消息类型，根据不同的类型来处理不同的业务
        if (action.equals(MsgActionEnum.CONNECT.type)){

            // 当webSocket 第一次open时，初始化channel，把用的channel和userId关联
            UserChannelRelation.put(dataContent.getChatMsg().getSenderId(),currentChannel);

            // 测试
//            for (Channel c : users) {
//                System.out.println(c.id().asLongText());
//            }
//            UserChannelRelation.output();

        }else if (action.equals(MsgActionEnum.CHAT.type)){

            // 聊天类型的消息，把聊天记录保存到数据库，同时标记消息的签收状态[未签收]
            ChatMsg chatMsg = dataContent.getChatMsg();
            String receiverId = chatMsg.getReceiverId();

            // 这里无法注入UserSerevice，所以通过工具类，获取到UserService
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            // 保存消息到数据库，并且标记为 未签收
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);

            // 发送消息
            Channel receiverChannel = UserChannelRelation.get(receiverId);
            if (receiverChannel == null){
                // TODO channel为空代表用户离线，推送消息 例如极光
            }else {
                // 不为空 从ChannelGroup去查找对应的channel是否存在
                Channel findChannel = users.find(receiverChannel.id());
                if (findChannel != null) {
                    // 用户在线
                    receiverChannel.writeAndFlush(
                            new TextWebSocketFrame(
                                    JsonUtils.objectToJson(dataContent)));
                } else {
                    // 用户离线 TODO 推送消息
                }
            }

        }else if (action.equals(MsgActionEnum.SIGNED.type)){

            // 签收消息类型，针对具体的消息进行签收，修改数据库中对应消息的签收状态[已签收]
            UserService userService = (UserService)SpringUtil.getBean("userServiceImpl");
            // 扩展字段在signed类型的消息中，代表需要去签收的消息id，逗号间隔
            String msgIdsStr = dataContent.getExtand();
            String[] msgIds = msgIdsStr.split(",");

            List<String> msgIdList = new ArrayList<>();
            for (String mid : msgIds) {
                if (StringUtils.isNotBlank(mid)) {
                    msgIdList.add(mid);
                }
            }

            System.out.println(msgIdList.toString());

            if (!msgIdList.isEmpty()) {
                // 批量签收
                userService.updateMsgSigned(msgIdList);
            }

        }else if (action.equals(MsgActionEnum.KEEPALIVE.type)){
            // 心跳类型的消息
            System.out.println("收到来自channel为[" + currentChannel + "]的心跳包...");
        }

    }

    /**
     * 客户端连接服务端吼，获取客户端的channel，放到channelGroup中管理
     * @param handlerContext
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext handlerContext) throws Exception{
        users.add(handlerContext.channel());
    }

    /**
     *    移除对应客户端的channel
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        System.out.println("客户端被移除，channelId为：" + channelId);
        // 这一句其实会自动执行
        users.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) throws Exception{
        cause.printStackTrace();
        // 发生异常 关闭channel 从channelGroup移除channel
        channelHandlerContext.channel().close();
        users.remove(channelHandlerContext.channel());
    }

}
