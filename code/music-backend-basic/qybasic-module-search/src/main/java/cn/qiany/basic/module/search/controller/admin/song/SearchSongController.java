package cn.qiany.basic.module.search.controller.admin.song;

import cn.qiany.basic.framework.common.pojo.CommonResult;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchResponse;
import cn.qiany.basic.module.search.service.song.SearchSongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.qiany.basic.framework.common.pojo.CommonResult.success;

@Tag(name = "音乐搜索 - 歌曲")
@RestController
@RequestMapping("/api/search")
@Validated
public class SearchSongController {

    @Resource
    private SearchSongService searchSongService;

    @PostMapping("/song")
    @Operation(summary = "获得歌曲列表")
    public CommonResult<AbstractGeneralSearchResponse> listSearchSong(@Valid @RequestBody AbstractGeneralSearchRequest request) {
        AbstractGeneralSearchResponse response = searchSongService.search(request);
        return success(response);
    }

}
