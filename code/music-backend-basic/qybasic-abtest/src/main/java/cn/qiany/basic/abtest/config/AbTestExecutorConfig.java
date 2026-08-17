package cn.qiany.basic.abtest.config;

import cn.qiany.basic.abtest.exception.AbFlowException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 创建 ABTest 专用有界线程池。
 */
@Configuration(proxyBeanMethods = false)
public class AbTestExecutorConfig {

    @Bean(name = "abTestExecutor")
    public ThreadPoolTaskExecutor abTestExecutor(
            AbTestExecutorProperties properties) {
        AbTestExecutorProperties.Executor config = properties.getExecutor();
        if (config.getCorePoolSize() < 1
                || config.getMaxPoolSize() < config.getCorePoolSize()
                || config.getQueueCapacity() < 0) {
            throw new AbFlowException("ABTest线程池配置非法");
        }
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());
        executor.setThreadNamePrefix("abtest-task-");
        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(
                config.getAwaitTerminationSeconds());
        executor.initialize();
        return executor;
    }
}