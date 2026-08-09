package cn.qiany.basic.module.search.controller.admin.song.vo.mag;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 歌曲 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IndexSongRespVO {
    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED, example = "31597")
    @ExcelProperty("")
    private Long id;

    @Schema(description = "外部歌曲业务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31597")
    @ExcelProperty("外部歌曲业务ID")
    private String orgId;

    @Schema(description = "歌曲名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("歌曲名称")
    private String name;

    @Schema(description = "歌手业务ID，第一期单值")
    @ExcelProperty("歌手业务ID，第一期单值")
    private String singerIds;

    @Schema(description = "歌手名称，第一期单值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("歌手名称，第一期单值")
    private String singerNames;

    @Schema(description = "专辑名称，第一期单值")
    @ExcelProperty("专辑名称，第一期单值")
    private String albumNames;

    @Schema(description = "歌曲热度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("歌曲热度")
    private Long hot;

    @Schema(description = "版权状态：1有版权，0无版权", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("版权状态：1有版权，0无版权")
    private Integer isCopyright;

    @Schema(description = "版权到期日")
    @ExcelProperty("版权到期日")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate invalidate;

    @Schema(description = "搜索可用状态：1可搜索，0不可搜索", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isEnabled;

    @Schema(description = "首次发布状态：1有效，0无效", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer firstStartState;

    @Schema(description = "发行日期")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @Schema(description = "备注", example = "随便")
    private String remark;

}
