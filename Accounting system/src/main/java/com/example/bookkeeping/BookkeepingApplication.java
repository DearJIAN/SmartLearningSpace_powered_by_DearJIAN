package com.example.bookkeeping;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.bookkeeping.mapper")
public class BookkeepingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookkeepingApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  个人记账系统启动成功   ლ(´ڡ`ლ)ﾞ");
        System.out.println("请访问地址: http://localhost:8080");
    }

}
 