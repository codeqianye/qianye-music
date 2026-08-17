package cn.qiany.basic.abtest.source;

import java.util.Map;

/**
 * 提供指定场景的原始节点配置。
 */
public interface JobFlowSource {
    Map<String, Map<String, Object>> getRequired(String scene);
}