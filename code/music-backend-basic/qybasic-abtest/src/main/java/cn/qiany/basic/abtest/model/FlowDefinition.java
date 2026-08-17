package cn.qiany.basic.abtest.model;

import cn.qiany.basic.abtest.context.RecommendJobFlowContext;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 已解析并通过校验的流程。
 *
 * <p>一个 FlowDefinition 对应一个 scene，例如 s900。它保存该场景全部
 * TaskDefinition，并按 execOrder 稳定排序。解析完成后会被缓存并由多个请求共享，
 * 因此 tasks 必须不可修改。</p>
 */
@Getter
public class FlowDefinition<C extends RecommendJobFlowContext<R>, R> {

    // 场景标识，与 abtest_strategy.json 的根节点 key 对应
    private final String scene;
    // 已完成校验和排序的任务列表
    private final List<TaskDefinition<C, R>> tasks;

    @Builder
    private FlowDefinition(String scene, List<TaskDefinition<C, R>> tasks) {
        // 复制后再包装为只读 List，防止调用方继续修改原始 tasks
        this.scene = Objects.requireNonNull(scene, "scene");
        this.tasks = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNull(tasks, "tasks")));
    }
}