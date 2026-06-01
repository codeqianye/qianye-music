package cn.qiany.basic.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 * @author 芋道源码
 */
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${qybasic.info.base-package}
@SpringBootApplication(scanBasePackages = {"${qybasic.info.base-package}.server", "${qybasic.info.base-package}.module"})
public class QybasicServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(QybasicServerApplication.class, args);
//        new SpringApplicationBuilder(QybasicServerApplication.class)
//                .applicationStartup(new BufferingApplicationStartup(20480))
//                .run(args);

    }

}
