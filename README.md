# chat
Netty+SpringBoot+Mybatis实现一个聊天服务器
# Netty服务搭建
## 搭建WebSocketServere

@Component
public class WsServer {
    /**
     *  采用单例模式创建WebSocketServer
     *  将构造函数为private 这样该类就不会被实例化
     *  使用volatile关键字 禁止指令重排 防止多线程下getInstance发生错误
     */

    private volatile static WsServer wsServer;

    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap server;
    private ChannelFuture future;

    private WsServer(){
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        //netty服务器的创建 ServerBootstrap为启动类
        server = new ServerBootstrap();
        //设置主从线程组 nio的双向通道channel 从线程组处理器
        server.group(mainGroup,subGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WsServerInitializer());
    }

    public static WsServer getInstance(){
        if (wsServer == null){
            synchronized (WsServer.class){
                if (wsServer == null){
                    wsServer = new WsServer();
                }
            }
        }
        return wsServer;
    }

    public void start(){
        this.future = server.bind(8085);
    }
}
public class WsServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        // 用于支持http协议
        ChannelPipeline channelPipeline = socketChannel.pipeline();
        //webSocket基于http协议，所以要有http编解码器
        channelPipeline.addLast(new HttpServerCodec());
        //对写大数据流的支持
        channelPipeline.addLast(new ChunkedWriteHandler());
        //1024*64消息的最大长度 httpMessage聚合器  在netty编程中，几乎都会用到此handler
        channelPipeline.addLast(new HttpObjectAggregator(1024*64));

        // 用于增加心跳机制
        // 客户端如果没有在1分钟内向服务端发送读写心跳（ALL）则主动断开 如果是读空闲或者写空闲，不处理
        channelPipeline.addLast(new IdleStateHandler(60, 60, 120));
        // 自定义的空闲状态检测
        channelPipeline.addLast(new HeartBeatHandler());

        /*
         * webSocket 服务器处理的协议，用于指定给客户端连接访问的路由 : /ws
         * 本handler会帮你处理一些繁重的复杂的事
         * 会帮你处理握手动作： handshaking（close, ping, pong） ping + pong = 心跳
         * 对于webSocket来讲，都是以frames进行传输的，不同的数据类型对应的frames也不同
         */
        channelPipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        channelPipeline.addLast(new ChatHandler());
    }
}
## 搭建聊天channel拦截器
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
     * 客户端连接服务端后，获取客户端的channel，放到channelGroup中管理
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
## 心跳检测
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;

            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("进入读空闲...");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                System.out.println("进入写空闲...");
            } else if (event.state() == IdleState.ALL_IDLE) {

                System.out.println("channel关闭前，users的数量为：" + ChatHandler.users.size());

                Channel channel = ctx.channel();
                // 关闭无用的channel，以防资源浪费
                channel.close();

                System.out.println("channel关闭后，users的数量为：" + ChatHandler.users.size());
            }
        }
    }
}
## 启动netty服务
@Component
public class NettyBooter implements ApplicationListener<ContextRefreshedEvent>{

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (contextRefreshedEvent.getApplicationContext().getParent() == null){
            try {
                WsServer.getInstance().start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
