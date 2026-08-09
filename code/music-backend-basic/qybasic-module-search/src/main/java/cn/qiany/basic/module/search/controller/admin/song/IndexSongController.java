package cn.qiany.basic.module.search.controller.admin.song;

import cn.qiany.basic.module.search.controller.admin.song.vo.mag.IndexSongPageReqVO;
import cn.qiany.basic.module.search.controller.admin.song.vo.mag.IndexSongRespVO;
import cn.qiany.basic.module.search.controller.admin.song.vo.mag.IndexSongSaveReqVO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.qiany.basic.framework.common.pojo.PageParam;
import cn.qiany.basic.framework.common.pojo.PageResult;
import cn.qiany.basic.framework.common.pojo.CommonResult;
import cn.qiany.basic.framework.common.util.object.BeanUtils;
import static cn.qiany.basic.framework.common.pojo.CommonResult.success;

import cn.qiany.basic.framework.excel.core.util.ExcelUtils;

import cn.qiany.basic.framework.apilog.core.annotation.ApiAccessLog;
import static cn.qiany.basic.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.qiany.basic.module.search.dal.dataobject.song.IndexSongDO;
import cn.qiany.basic.module.search.service.song.IndexSongService;

@Tag(name = "管理后台 - 歌曲")
@RestController
@RequestMapping("/search/index-song")
@Validated
public class IndexSongController {

    @Resource
    private IndexSongService indexSongService;

    @PostMapping("/create")
    @Operation(summary = "创建歌曲")
    @PreAuthorize("@ss.hasPermission('search:index-song:create')")
    public CommonResult<Long> createIndexSong(@Valid @RequestBody IndexSongSaveReqVO createReqVO) {
        return success(indexSongService.createIndexSong(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新歌曲")
    @PreAuthorize("@ss.hasPermission('search:index-song:update')")
    public CommonResult<Boolean> updateIndexSong(@Valid @RequestBody IndexSongSaveReqVO updateReqVO) {
        indexSongService.updateIndexSong(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除歌曲")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('search:index-song:delete')")
    public CommonResult<Boolean> deleteIndexSong(@RequestParam("id") Long id) {
        indexSongService.deleteIndexSong(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除歌曲")
                @PreAuthorize("@ss.hasPermission('search:index-song:delete')")
    public CommonResult<Boolean> deleteIndexSongList(@RequestParam("ids") List<Long> ids) {
        indexSongService.deleteIndexSongListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得歌曲")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('search:index-song:query')")
    public CommonResult<IndexSongRespVO> getIndexSong(@RequestParam("id") Long id) {
        IndexSongDO indexSong = indexSongService.getIndexSong(id);
        return success(BeanUtils.toBean(indexSong, IndexSongRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得歌曲分页")
    @PreAuthorize("@ss.hasPermission('search:index-song:query')")
    public CommonResult<PageResult<IndexSongRespVO>> getIndexSongPage(@Valid IndexSongPageReqVO pageReqVO) {
        PageResult<IndexSongDO> pageResult = indexSongService.getIndexSongPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IndexSongRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出歌曲 Excel")
    @PreAuthorize("@ss.hasPermission('search:index-song:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportIndexSongExcel(@Valid IndexSongPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IndexSongDO> list = indexSongService.getIndexSongPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "歌曲.xls", "数据", IndexSongRespVO.class,
                        BeanUtils.toBean(list, IndexSongRespVO.class));
    }

}
