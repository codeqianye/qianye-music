package cn.qiany.basic.abtest.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 注册本地流程引擎的通用配置和专用线程池。
 */
@AutoConfiguration
@EnableConfigurationProperties(AbTestExecutorProperties.class)
@Import(AbTestExecutorConfig.class)
public class AbTestAutoConfiguration {
}