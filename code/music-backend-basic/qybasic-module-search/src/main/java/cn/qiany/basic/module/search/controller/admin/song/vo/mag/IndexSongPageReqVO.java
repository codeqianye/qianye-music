package cn.qiany.basic.module.search.controller.admin.song.vo.mag;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.qiany.basic.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 歌曲分页 Request VO")
@Data
public class IndexSongPageReqVO extends PageParam {

    @Schema(description = "歌曲名称", example = "王五")
    private String name;

    @Schema(description = "歌手业务ID，第一期单值")
    private String singerIds;

    @Schema(description = "歌手名称，第一期单值")
    private String singerNames;

    @Schema(description = "专辑名称，第一期单值")
    private String albumNames;

    @Schema(description = "歌曲热度")
    private Long hot;

    @Schema(description = "版权状态：1有版权，0无版权")
    private Integer isCopyright;

}