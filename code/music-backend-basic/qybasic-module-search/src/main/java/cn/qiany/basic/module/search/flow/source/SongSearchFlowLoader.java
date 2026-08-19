package cn.qiany.basic.module.search.flow.source;

import cn.qiany.basic.abtest.exception.AbFlowException;
import cn.qiany.basic.abtest.parser.JsonReaderContext;
import cn.qiany.basic.abtest.source.JobFlowSource;
import cn.qiany.basic.module.search.flow.config.SongSearchFlowProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 启动时读取并缓存单曲流程 JSON。
 */
@Component
public class SongSearchFlowLoader implements JobFlowSource {
    private final Map<String, Map<String, Map<String, Object>>> scenes;

    public SongSearchFlowLoader(
            SongSearchFlowProperties properties,
            JsonReaderContext jsonReaderContext) {
        String path = StringUtils.trim(properties.getConfigPath());
        if (StringUtils.isBlank(path)) {
            throw new AbFlowException("单曲流程配置路径不能为空");
        }
        ClassPathResource resource = new ClassPathResource(path);
        // 只在 Bean 创建时读取一次；请求线程始终访问内存中的只读配置。
        try (InputStream inputStream = resource.getInputStream()) {
            this.scenes = jsonReaderContext.toMap(inputStream);
        } catch (IOException ex) {
            throw new AbFlowException("单曲流程配置读取失败: " + path, ex);
        }
    }

    /**
     * 返回指定场景的原始节点配置，缺失场景应在执行前立即失败。
     *
     * @param scene 请求选择的场景
     * @return 保持 JSON 声明顺序的节点映射
     */
    @Override
    public Map<String, Map<String, Object>> getRequired(String scene) {
        Map<String, Map<String, Object>> nodes = scenes.get(scene);
        if (nodes == null) {
            throw new AbFlowException("单曲流程场景不存在: " + scene);
        }
        return nodes;
    }
}