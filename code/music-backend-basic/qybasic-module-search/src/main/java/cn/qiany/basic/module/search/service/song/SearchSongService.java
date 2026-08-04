package cn.qiany.basic.module.search.service.song;

import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchResponse;

/**
 * 搜索歌曲 Service 接口
 *
 * @author fengpeng
 */
public interface SearchSongService {

    /**
     * 歌曲搜索
     *
     * @param request
     */
    AbstractGeneralSearchResponse search(AbstractGeneralSearchRequest request);

}
