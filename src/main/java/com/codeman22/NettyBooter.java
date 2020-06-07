package com.codeman22;

import com.codeman22.chat.netty.WsServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 *
 * @author Kuzma
 * @date 2020/5/11
 */
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
