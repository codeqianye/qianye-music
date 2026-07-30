package cn.qiany.basic.module.search.service.song;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.qiany.basic.module.search.controller.admin.song.vo.*;
import cn.qiany.basic.module.search.dal.dataobject.song.IndexSongDO;
import cn.qiany.basic.framework.common.pojo.PageResult;
import cn.qiany.basic.framework.common.pojo.PageParam;
import cn.qiany.basic.framework.common.util.object.BeanUtils;

import cn.qiany.basic.module.search.dal.mysql.song.IndexSongMapper;

import static cn.qiany.basic.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.qiany.basic.framework.common.util.collection.CollectionUtils.convertList;
import static cn.qiany.basic.framework.common.util.collection.CollectionUtils.diffList;
import static cn.qiany.basic.module.search.enums.ErrorCodeConstants.*;

/**
 * 歌曲 Service 实现类
 *
 * @author fengpeng
 */
@Service
@Validated
public class IndexSongServiceImpl implements IndexSongService {

    @Resource
    private IndexSongMapper indexSongMapper;

    @Override
    public Long createIndexSong(IndexSongSaveReqVO createReqVO) {
        // 插入
        IndexSongDO indexSong = BeanUtils.toBean(createReqVO, IndexSongDO.class);
        indexSongMapper.insert(indexSong);

        // 返回
        return indexSong.getId();
    }

    @Override
    public void updateIndexSong(IndexSongSaveReqVO updateReqVO) {
        // 校验存在
        validateIndexSongExists(updateReqVO.getId());
        // 更新
        IndexSongDO updateObj = BeanUtils.toBean(updateReqVO, IndexSongDO.class);
        indexSongMapper.updateById(updateObj);
    }

    @Override
    public void deleteIndexSong(Long id) {
        // 校验存在
        validateIndexSongExists(id);
        // 删除
        indexSongMapper.deleteById(id);
    }

    @Override
        public void deleteIndexSongListByIds(List<Long> ids) {
        // 删除
        indexSongMapper.deleteByIds(ids);
        }


    private void validateIndexSongExists(Long id) {
        if (indexSongMapper.selectById(id) == null) {
            throw exception(INDEX_SONG_NOT_EXISTS);
        }
    }

    @Override
    public IndexSongDO getIndexSong(Long id) {
        return indexSongMapper.selectById(id);
    }

    @Override
    public PageResult<IndexSongDO> getIndexSongPage(IndexSongPageReqVO pageReqVO) {
        return indexSongMapper.selectPage(pageReqVO);
    }

}