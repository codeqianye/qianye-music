package cn.qiany.basic.module.search.flow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 单曲流程资源和默认场景配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "search.song.flow")
public class SongSearchFlowProperties {
    private String defaultAppId = "s900";
    private String configPath = "search/abtest_strategy.json";
}