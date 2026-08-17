package cn.qiany.basic.abtest.model;

import cn.qiany.basic.abtest.context.RecommendJobFlowContext;
import cn.qiany.basic.abtest.processor.AbstractGeneralProcessor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 单个可执行流程节点。
 *
 * <p>该类不是 Processor 本身，而是解析配置后得到的一条执行说明。
 * 例如 s900 下的 song 节点会被转换成一个 TaskDefinition，执行器随后根据
 * execOrder 决定执行阶段，并调用 processor 完成节点逻辑。</p>
 *
 * <p>泛型 C 表示本次请求使用的流程上下文，R 表示单个 Processor 返回的结果。
 * 歌曲搜索中分别对应 GeneralRecFlowContext 和 SongProcessorResult。</p>
 */
@Getter
public class TaskDefinition<C extends RecommendJobFlowContext<R>, R> {

    // 节点所属场景，例如 s900；用于日志、异常定位和结果 flowId
    private final String scene;
    // JSON 中的节点名称，例如 song；同一个 fmap 可以被多个节点复用
    private final String type;
    // 执行阶段；不同 order 串行，相同 order 归入同一并发阶段
    private final int execOrder;
    // 配置中的 Processor 映射标识，例如 msc_10
    private final String fmap;
    // toggle=1 时执行；toggle=0 时跳过并记录 SKIPPED 轨迹
    private final boolean toggle;
    // 必选节点失败会终止流程，可选节点失败只记录轨迹并继续
    private final boolean required;
    // 单节点允许的最大执行时间，单位毫秒
    private final long timeoutMs;
    // 节点完整参数的深度只读副本，包含公共字段和 chain 等业务字段
    private final Map<String, Object> params;
    // 解析阶段已经按 fmap 找到的 Spring Processor Bean，请求执行时无需再次查找
    private final AbstractGeneralProcessor<C, R> processor;

    @Builder
    private TaskDefinition(String scene,
                           String type,
                           int execOrder,
                           String fmap,
                           boolean toggle,
                           boolean required,
                           long timeoutMs,
                           Map<String, Object> params,
                           AbstractGeneralProcessor<C, R> processor) {
        // 构造时完成防御性复制，避免共享配置在请求执行期间被其他代码修改
        this.scene = Objects.requireNonNull(scene, "scene");
        this.type = Objects.requireNonNull(type, "type");
        this.execOrder = execOrder;
        this.fmap = Objects.requireNonNull(fmap, "fmap");
        this.toggle = toggle;
        this.required = required;
        this.timeoutMs = timeoutMs;
        this.params = immutableParams(
                Objects.requireNonNull(params, "params"));
        this.processor = Objects.requireNonNull(processor, "processor");
    }

    private static Map<String, Object> immutableParams(
            Map<String, Object> params) {
        // 仅包装最外层 Map 不够，chain 等嵌套集合仍可能被修改，因此需要递归复制
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            copy.put(entry.getKey(), immutableValue(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    private static Object immutableValue(Object value) {
        // JSON 对象由 Jackson 解析为 Map，需要继续递归处理其内部值
        if (value instanceof Map) {
            Map<Object, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                copy.put(entry.getKey(), immutableValue(entry.getValue()));
            }
            return Collections.unmodifiableMap(copy);
        }
        if (value instanceof List) {
            // JSON 数组由 Jackson 解析为 List，同样转换成只读副本
            List<Object> copy = new ArrayList<>();
            for (Object item : (List<?>) value) {
                copy.add(immutableValue(item));
            }
            return Collections.unmodifiableList(copy);
        }
        return value;
    }
}