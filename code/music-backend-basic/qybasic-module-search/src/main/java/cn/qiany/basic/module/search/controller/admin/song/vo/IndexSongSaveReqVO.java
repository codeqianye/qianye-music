package cn.qiany.basic.module.search.controller.admin.song.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 歌曲新增/修改 Request VO")
@Data
public class IndexSongSaveReqVO {

    @Schema(description = "自增主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10744")
    private Long id;

    @Schema(description = "外部歌曲业务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31597")
    @NotEmpty(message = "外部歌曲业务ID不能为空")
    private String orgId;

    @Schema(description = "歌曲名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "歌曲名称不能为空")
    private String name;

    @Schema(description = "歌手业务ID，第一期单值")
    private String singerIds;

    @Schema(description = "歌手名称，第一期单值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "歌手名称，第一期单值不能为空")
    private String singerNames;

    @Schema(description = "专辑名称，第一期单值")
    private String albumNames;

    @Schema(description = "歌曲热度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "歌曲热度不能为空")
    private Long hot;

    @Schema(description = "版权状态：1有版权，0无版权", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版权状态：1有版权，0无版权不能为空")
    private Integer isCopyright;

    @Schema(description = "版权到期日")
    private LocalDate invalidate;

    @Schema(description = "搜索可用状态：1可搜索，0不可搜索", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "搜索可用状态：1可搜索，0不可搜索不能为空")
    private Integer isEnabled;

    @Schema(description = "首次发布状态：1有效，0无效", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "首次发布状态：1有效，0无效不能为空")
    private Integer firstStartState;

    @Schema(description = "发行日期")
    private LocalDate releaseDate;

    @Schema(description = "更新者")
    private String updator;

    @Schema(description = "备注", example = "随便")
    private String remark;

}