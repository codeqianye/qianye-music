package cn.qiany.basic.module.search.dal.dataobject.song;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.qiany.basic.framework.mybatis.core.dataobject.BaseDO;

/**
 * 歌曲 DO
 *
 * @author fengpeng
 */
@TableName("index_song")
@KeySequence("index_song_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexSongDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;
    /**
     * 外部歌曲业务ID
     */
    private String orgId;
    /**
     * 歌曲名称
     */
    private String name;
    /**
     * 歌手业务ID，第一期单值
     */
    private String singerIds;
    /**
     * 歌手名称，第一期单值
     */
    private String singerNames;
    /**
     * 专辑名称，第一期单值
     */
    private String albumNames;
    /**
     * 歌曲热度
     */
    private Long hot;
    /**
     * 版权状态：1有版权，0无版权
     */
    private Integer isCopyright;
    /**
     * 版权到期日
     */
    private LocalDate invalidate;
    /**
     * 搜索可用状态：1可搜索，0不可搜索
     */
    private Integer isEnabled;
    /**
     * 首次发布状态：1有效，0无效
     */
    private Integer firstStartState;
    /**
     * 发行日期
     */
    private LocalDate releaseDate;
    /**
     * 备注
     */
    private String remark;


}
