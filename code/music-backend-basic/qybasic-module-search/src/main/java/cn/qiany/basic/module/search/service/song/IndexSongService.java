package cn.qiany.basic.module.search.service.song;

import java.util.*;
import javax.validation.*;
import cn.qiany.basic.module.search.controller.admin.song.vo.*;
import cn.qiany.basic.module.search.dal.dataobject.song.IndexSongDO;
import cn.qiany.basic.framework.common.pojo.PageResult;
import cn.qiany.basic.framework.common.pojo.PageParam;

/**
 * 歌曲 Service 接口
 *
 * @author fengpeng
 */
public interface IndexSongService {

    /**
     * 创建歌曲
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createIndexSong(@Valid IndexSongSaveReqVO createReqVO);

    /**
     * 更新歌曲
     *
     * @param updateReqVO 更新信息
     */
    void updateIndexSong(@Valid IndexSongSaveReqVO updateReqVO);

    /**
     * 删除歌曲
     *
     * @param id 编号
     */
    void deleteIndexSong(Long id);

    /**
    * 批量删除歌曲
    *
    * @param ids 编号
    */
    void deleteIndexSongListByIds(List<Long> ids);

    /**
     * 获得歌曲
     *
     * @param id 编号
     * @return 歌曲
     */
    IndexSongDO getIndexSong(Long id);

    /**
     * 获得歌曲分页
     *
     * @param pageReqVO 分页查询
     * @return 歌曲分页
     */
    PageResult<IndexSongDO> getIndexSongPage(IndexSongPageReqVO pageReqVO);

}