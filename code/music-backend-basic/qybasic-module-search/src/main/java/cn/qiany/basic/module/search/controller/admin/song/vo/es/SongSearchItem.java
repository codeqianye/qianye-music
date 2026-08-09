package cn.qiany.basic.module.search.controller.admin.song.vo.es;

import lombok.Data;

/**
 * 定义单曲搜索的当前页展示数据。
 */
@Data
public class SongSearchItem {
    // MySQL 自增主键
    private Long id;
    // 歌曲业务 ID
    private String orgId;
    // 歌曲原始名称
    private String name;
    // 歌手原始名称
    private String singerNames;
    // 专辑原始名称
    private String albumNames;
    // 排序热度
    private Long hot;
    // 歌名高亮结果
    private String highlightName;
    // 歌手名高亮结果
    private String highlightSingerNames;
    // 专辑名高亮结果
    private String highlightAlbumNames;
}