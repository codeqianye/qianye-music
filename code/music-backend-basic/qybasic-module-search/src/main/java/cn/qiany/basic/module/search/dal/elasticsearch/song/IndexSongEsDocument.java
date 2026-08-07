package cn.qiany.basic.module.search.dal.elasticsearch.song;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * index_song文档对象
 */
@Getter
@Setter
public class IndexSongEsDocument {
    //歌曲业务id
    private String id;
    //数据库主键id
    private Long dbId;
    private String name;
    private String singerIds;
    private String singerNames;
    private String albumNames;
    private Long hot;
    private Integer isCopyright;
    private LocalDate invalidate;
    private Integer isEnabled;
    private Integer firstStartState;
    private LocalDate releaseDate;
    private LocalDateTime updateTime;
}
