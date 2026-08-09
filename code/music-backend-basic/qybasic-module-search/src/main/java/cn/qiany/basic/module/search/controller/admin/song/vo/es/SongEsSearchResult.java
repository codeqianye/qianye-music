package cn.qiany.basic.module.search.controller.admin.song.vo.es;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 封装 ES 单曲搜索结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongEsSearchResult {
    // ES 总命中数
    private long total;
    // 当前页歌曲
    private List<SongSearchItem> rows;
}