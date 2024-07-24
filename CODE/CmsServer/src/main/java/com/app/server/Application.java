package com.app.server;

import com.app.server.config.ConfigInfo;
import com.app.server.utils.AppUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.DateTimeUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Date;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
public class Application {
    public static void main(String[] args) throws IllegalArgumentException {
        try {
            new SpringApplicationBuilder(Application.class)
                    .properties("spring.config.location:" + ConfigInfo.SPRING_BOOT_CONFIG_PATH)
                    .build().run(args);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }
}