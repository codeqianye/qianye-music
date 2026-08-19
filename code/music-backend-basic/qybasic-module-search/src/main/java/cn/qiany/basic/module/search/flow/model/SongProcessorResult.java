package cn.qiany.basic.module.search.flow.model;

import cn.qiany.basic.module.search.controller.admin.song.vo.es.SongSearchItem;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 歌曲 Processor 返回的独立业务结果。
 */
@Getter
@Builder
public class SongProcessorResult {
    private final String normalizedKeyword;
    private final RecallType recallType;
    //@Builder.Default: 当你使用 @Builder 创建对象时，如果没有给这个字段赋值，就使用这里写的默认值。
    @Builder.Default
    private final List<SongSearchItem> rows = Collections.emptyList();
    private final long total;
    private final boolean finalResult;
}