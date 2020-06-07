package com.codeman22;

import com.codeman22.chat.utils.SpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kuzma
 */
@SpringBootApplication
@Configuration
@MapperScan("com.codeman22.chat.dao")
public class ChatApplication {

	@Bean
	public SpringUtil getSpringUtil(){
		return new SpringUtil();
	}
	public static void main(String[] args) {
		SpringApplication.run(ChatApplication.class, args);
	}

}
