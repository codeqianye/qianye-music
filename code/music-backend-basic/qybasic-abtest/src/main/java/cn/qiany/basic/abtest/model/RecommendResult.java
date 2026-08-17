package cn.qiany.basic.abtest.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 本地流程引擎对业务层的统一输出。
 *
 * <p>Writer 将业务 Context 转换成该对象，搜索 Service 再把它交给现有
 * afterHandle 生成 HTTP 响应。泛型 T 是列表元素类型，W 是可选附加信息类型。</p>
 */
@Getter
@Builder
public class RecommendResult<T, W> {
    // 最终返回的业务数据；歌曲搜索中为 SongSearchItem 列表
    @Builder.Default
    private final List<T> recData = Collections.emptyList();
    // 符合条件的总数量，用于分页
    private final long total;
    // 本次请求实际执行的流程标识
    private final String flowId;
    // 预留的策略附加信息；第三期使用 Void 并保持为空
    private final W attached;
}