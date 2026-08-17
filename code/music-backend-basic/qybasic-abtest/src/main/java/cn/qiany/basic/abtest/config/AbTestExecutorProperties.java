package cn.qiany.basic.abtest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 本地流程引擎线程池和阶段超时配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "abtest")
public class AbTestExecutorProperties {
    private long stageTimeoutMs = 2000L;
    private final Executor executor = new Executor();

    @Getter
    @Setter
    public static class Executor {
        private int corePoolSize = 4;
        private int maxPoolSize = 8;
        private int queueCapacity = 100;
        private int keepAliveSeconds = 60;
        private int awaitTerminationSeconds = 10;
    }
}