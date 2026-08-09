package cn.qiany.basic.module.search.controller.admin.song;

import cn.qiany.basic.framework.common.pojo.CommonResult;
import cn.qiany.basic.module.search.controller.admin.song.vo.sync.IndexSongEsSyncResult;
import cn.qiany.basic.module.search.service.sync.IndexSongEsSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.qiany.basic.framework.common.pojo.CommonResult.success;

/**
 * 提供歌曲 ES 索引管理接口。
 */
@Tag(name = "管理后台 - 歌曲ES索引")
@RestController
@RequestMapping("/api/sync")
public class SyncSongController {

    @Resource
    private IndexSongEsSyncService syncService;

    /**
     * 触发 MySQL 到 ES 的全量同步。
     *
     * @return 全量同步结果
     */
    @PostMapping("/full-sync")
    @Operation(summary = "全量同步歌曲到ES")
    public CommonResult<IndexSongEsSyncResult> fullSync() {
        return success(syncService.fullSync());
    }
}