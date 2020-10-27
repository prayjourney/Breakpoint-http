package win.pangniu.learn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * Created by wenwen on 2017/4/11.
 * version 1.0
 */
@SpringBootApplication
@MapperScan("win.pangniu.learn.mapper")
public class BreakpointHttpUploadApp {

    public static void main(String[] args) {
        SpringApplication.run(BreakpointHttpUploadApp.class, args);
    }

}
