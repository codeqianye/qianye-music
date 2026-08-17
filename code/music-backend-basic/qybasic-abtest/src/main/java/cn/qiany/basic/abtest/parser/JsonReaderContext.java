package cn.qiany.basic.abtest.parser;

import cn.qiany.basic.abtest.exception.AbFlowException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 将流程 JSON 转换为保序、只读的配置 Map。
 *
 * <p>保序非常重要：相同 order 的节点虽然并发执行，但最终结果仍要按照
 * JSON 声明顺序合并。严格重复字段检测用于阻止重复 scene 或 nodeName
 * 被 Jackson 静默覆盖。</p>
 */
public class JsonReaderContext {

    // 固定反序列化为 LinkedHashMap，保留 scene 和节点在 JSON 中的声明顺序
    private static final TypeReference<LinkedHashMap<String,
            LinkedHashMap<String, LinkedHashMap<String, Object>>>> FLOW_TYPE =
            new TypeReference<LinkedHashMap<String,
                    LinkedHashMap<String, LinkedHashMap<String, Object>>>>() {
            };

    // 独立 ObjectMapper 只服务流程配置，不修改若依全局 Jackson 配置
    private final ObjectMapper objectMapper;

    public JsonReaderContext() {
        // 默认 Jackson 会让后出现的同名字段覆盖前值，这里改为直接解析失败
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        this.objectMapper = new ObjectMapper(jsonFactory);
    }

    /**
     * 读取流程配置，输入流由调用方负责关闭。
     *
     * <p>该类不知道资源从哪里打开，因此不能擅自关闭 InputStream。
     * SongSearchFlowLoader 应使用 try-with-resources 管理资源生命周期。</p>
     *
     * @param inputStream JSON 输入流
     * @return 场景、节点和节点参数组成的三级 Map
     */
    public Map<String, Map<String, Map<String, Object>>> toMap(
            InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream");
        try {
            // 一次性读取完整配置，后续请求直接使用缓存，不重复解析 JSON
            LinkedHashMap<String,
                    LinkedHashMap<String, LinkedHashMap<String, Object>>> source =
                    objectMapper.readValue(inputStream, FLOW_TYPE);
            if (source == null || source.isEmpty()) {
                throw new AbFlowException("流程JSON根节点不能为空");
            }
            return immutableCopy(source);
        } catch (IOException ex) {
            throw new AbFlowException("流程JSON读取失败", ex);
        }
    }

    private Map<String, Map<String, Map<String, Object>>> immutableCopy(
            LinkedHashMap<String,
                    LinkedHashMap<String, LinkedHashMap<String, Object>>> source) {
        Map<String, Map<String, Map<String, Object>>> scenes =
                new LinkedHashMap<>();
        // 逐层复制 Map，既保留顺序，也切断返回值和 Jackson 原始结果之间的引用
        for (Map.Entry<String,
                LinkedHashMap<String, LinkedHashMap<String, Object>>> sceneEntry
                : source.entrySet()) {
            if (sceneEntry.getValue() == null) {
                throw new AbFlowException(
                        "流程场景节点不能为空, scene=" + sceneEntry.getKey());
            }
            Map<String, Map<String, Object>> nodes = new LinkedHashMap<>();
            for (Map.Entry<String, LinkedHashMap<String, Object>> nodeEntry
                    : sceneEntry.getValue().entrySet()) {
                if (nodeEntry.getValue() == null) {
                    throw new AbFlowException("流程节点参数不能为空, scene="
                            + sceneEntry.getKey() + ", node="
                            + nodeEntry.getKey());
                }
                nodes.put(nodeEntry.getKey(), Collections.unmodifiableMap(
                        new LinkedHashMap<>(nodeEntry.getValue())));
            }
            // 节点集合只允许读取，不能在请求执行期间动态增删节点
            scenes.put(sceneEntry.getKey(), Collections.unmodifiableMap(nodes));
        }
        return Collections.unmodifiableMap(scenes);
    }
}