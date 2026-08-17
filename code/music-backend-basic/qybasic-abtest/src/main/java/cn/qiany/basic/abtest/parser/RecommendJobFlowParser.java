package cn.qiany.basic.abtest.parser;

import cn.qiany.basic.abtest.context.RecommendJobFlowContext;
import cn.qiany.basic.abtest.exception.AbFlowException;
import cn.qiany.basic.abtest.model.FlowDefinition;
import cn.qiany.basic.abtest.model.TaskDefinition;
import cn.qiany.basic.abtest.processor.AbstractGeneralProcessor;
import cn.qiany.basic.abtest.registry.TaskProcessorCache;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 将节点配置解析为可执行流程定义。
 *
 * <p>该类连接“原始配置”和“执行器”：它负责校验公共字段、根据 fmap 找到
 * Processor Bean、构建 TaskDefinition、按 order 稳定排序并缓存最终流程。
 * 它不读取 JSON，也不执行 Processor。</p>
 *
 * <p>泛型 C 是业务流程上下文，R 是单节点业务结果。解析器本身不依赖歌曲请求、
 * ES 客户端或 SongSearchItem，因此可以被后续其他搜索业务复用。</p>
 */
public class RecommendJobFlowParser<
        C extends RecommendJobFlowContext<R>, R> {

    // fmap 注册表，负责把配置中的 msc_10 等标识转换为 Processor Bean
    private final TaskProcessorCache<C, R> taskProcessorCache;
    // 整个阶段允许的最大耗时，节点 timeoutMs 不能超过该值
    private final long stageTimeoutMs;
    // 流程配置启动后不再变化，按 scene 缓存解析结果供所有请求共享
    private final ConcurrentMap<String, FlowDefinition<C, R>> flowCache =
            new ConcurrentHashMap<>();

    public RecommendJobFlowParser(
            TaskProcessorCache<C, R> taskProcessorCache,
            long stageTimeoutMs) {
        // 构造阶段提前拒绝非法依赖和配置，避免到首个请求时才暴露错误
        if (taskProcessorCache == null) {
            throw new IllegalArgumentException("taskProcessorCache不能为空");
        }
        if (stageTimeoutMs <= 0) {
            throw new IllegalArgumentException("stageTimeoutMs必须大于0");
        }
        this.taskProcessorCache = taskProcessorCache;
        this.stageTimeoutMs = stageTimeoutMs;
    }

    /**
     * 解析指定场景的节点配置。
     *
     * <p>同一个 scene 只在首次调用时真正解析。并发请求同时首次进入时，
     * putIfAbsent 保证最终只有一个 FlowDefinition 作为共享缓存结果。</p>
     *
     * @param scene 场景标识
     * @param nodes 节点名称与节点参数映射
     * @return 不可变流程定义
     */
    public FlowDefinition<C, R> parseJobFlow(
            String scene,
            Map<String, Map<String, Object>> nodes) {
        String normalizedScene = StringUtils.trim(scene);
        if (StringUtils.isBlank(normalizedScene)) {
            throw new AbFlowException("流程scene不能为空");
        }

        // 热路径优先读取缓存，正常请求不会重复遍历和校验节点配置
        FlowDefinition<C, R> cached = flowCache.get(normalizedScene);
        if (cached != null) {
            return cached;
        }

        // 两个线程可能同时解析，但 putIfAbsent 会统一返回先写入缓存的结果
        FlowDefinition<C, R> parsed = doParse(normalizedScene, nodes);
        FlowDefinition<C, R> previous =
                flowCache.putIfAbsent(normalizedScene, parsed);
        return previous == null ? parsed : previous;
    }

    /**
     * 校验一个场景的所有节点，并转成按执行阶段排序的任务定义。
     *
     * @param scene 已标准化的场景标识
     * @param nodes 保留 JSON 声明顺序的节点配置
     * @return 可供执行器复用的不可变流程定义
     */
    private FlowDefinition<C, R> doParse(
            String scene,
            Map<String, Map<String, Object>> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            throw new AbFlowException("流程节点不能为空, scene=" + scene);
        }

        List<TaskDefinition<C, R>> tasks = new ArrayList<>();
        boolean enabled = false;
        // LinkedHashMap 的遍历顺序就是 JSON 声明顺序，不能转换为 HashMap
        for (Map.Entry<String, Map<String, Object>> entry : nodes.entrySet()) {
            String nodeName = StringUtils.trim(entry.getKey());
            if (StringUtils.isBlank(nodeName)) {
                throw new AbFlowException("流程nodeName不能为空, scene=" + scene);
            }

            Map<String, Object> params = entry.getValue();
            if (params == null || params.isEmpty()) {
                throw configError(scene, nodeName, "节点参数不能为空");
            }

            int execOrder = parseOrder(scene, nodeName, params);
            String fmap = requiredString(scene, nodeName, params, "fmap");
            boolean toggle = parseFlag(scene, nodeName, params, "toggle");
            boolean required = parseFlag(scene, nodeName, params, "required");
            long timeoutMs = parseTimeout(scene, nodeName, params);
            AbstractGeneralProcessor<C, R> processor =
                    taskProcessorCache.getData(fmap);

            // TaskDefinition 保存完整节点参数，业务 Processor 可以读取自己的扩展字段
            tasks.add(TaskDefinition.<C, R>builder()
                    .scene(scene)
                    .type(nodeName)
                    .execOrder(execOrder)
                    .fmap(fmap)
                    .toggle(toggle)
                    .required(required)
                    .timeoutMs(timeoutMs)
                    .params(params)
                    .processor(processor)
                    .build());
            enabled = enabled || toggle;
        }

        if (!enabled) {
            throw new AbFlowException("流程至少需要一个启用节点, scene=" + scene);
        }

        // List.sort 是稳定排序；order 相同时继续保持 JSON 中的声明顺序
        tasks.sort(Comparator.comparingInt(
                (TaskDefinition<C, R> task) -> task.getExecOrder()));
        return FlowDefinition.<C, R>builder()
                .scene(scene)
                .tasks(tasks)
                .build();
    }

    /**
     * 解析节点所属阶段；阶段值决定串并行关系。
     */
    private int parseOrder(
            String scene,
            String nodeName,
            Map<String, Object> params) {
        String value = requiredString(scene, nodeName, params, "order");
        try {
            // 现网配置中的 order 是字符串，这里统一转成整数供执行器分组
            int order = Integer.parseInt(value);
            if (order < 0) {
                throw configError(scene, nodeName, "order不能小于0");
            }
            return order;
        } catch (NumberFormatException ex) {
            throw configError(scene, nodeName, "order必须是整数", ex);
        }
    }

    /**
     * 校验单节点超时，避免节点超时突破阶段屏障。
     */
    private long parseTimeout(
            String scene,
            String nodeName,
            Map<String, Object> params) {
        String value = requiredString(
                scene, nodeName, params, "timeoutMs");
        try {
            // 节点超时必须受阶段总超时约束，否则阶段屏障无法按时结束
            long timeoutMs = Long.parseLong(value);
            if (timeoutMs <= 0 || timeoutMs > stageTimeoutMs) {
                throw configError(scene, nodeName,
                        "timeoutMs必须大于0且不超过阶段超时");
            }
            return timeoutMs;
        } catch (NumberFormatException ex) {
            throw configError(scene, nodeName, "timeoutMs必须是整数", ex);
        }
    }

    /**
     * 按现网约定读取字符串形式的开关字段。
     */
    private boolean parseFlag(
            String scene,
            String nodeName,
            Map<String, Object> params,
            String fieldName) {
        String value = requiredString(
                scene, nodeName, params, fieldName);
        // 沿用现网字符串 0/1，拒绝 true、false 等不同配置写法
        if (!"0".equals(value) && !"1".equals(value)) {
            throw configError(scene, nodeName,
                    fieldName + "只能配置为字符串0或1");
        }
        return "1".equals(value);
    }

    /**
     * 获取必填公共字段，不接受 JSON 数字或布尔值的隐式转换。
     */
    private String requiredString(
            String scene,
            String nodeName,
            Map<String, Object> params,
            String fieldName) {
        Object value = params.get(fieldName);
        // 公共字段必须显式配置为字符串，避免 Jackson 数字类型造成隐式兼容
        if (!(value instanceof String)
                || StringUtils.isBlank((String) value)) {
            throw configError(scene, nodeName,
                    fieldName + "必须是非空字符串");
        }
        return StringUtils.trim((String) value);
    }

    private AbFlowException configError(
            String scene,
            String nodeName,
            String message) {
        return new AbFlowException(message
                + ", scene=" + scene
                + ", node=" + nodeName);
    }

    private AbFlowException configError(
            String scene,
            String nodeName,
            String message,
            Throwable cause) {
        return new AbFlowException(message
                + ", scene=" + scene
                + ", node=" + nodeName, cause);
    }
}