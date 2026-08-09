package cn.qiany.basic.module.search.dal.mysql.song;

import java.util.*;

import cn.qiany.basic.framework.common.pojo.PageResult;
import cn.qiany.basic.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.qiany.basic.framework.mybatis.core.mapper.BaseMapperX;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.controller.admin.song.vo.mag.IndexSongPageReqVO;
import cn.qiany.basic.module.search.dal.dataobject.song.IndexSongDO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 歌曲 Mapper
 *
 * @author fengpeng
 */
@Mapper
public interface IndexSongMapper extends BaseMapperX<IndexSongDO> {

    default PageResult<IndexSongDO> selectPage(IndexSongPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IndexSongDO>()
                .likeIfPresent(IndexSongDO::getName, reqVO.getName())
                .eqIfPresent(IndexSongDO::getSingerIds, reqVO.getSingerIds())
                .eqIfPresent(IndexSongDO::getSingerNames, reqVO.getSingerNames())
                .likeIfPresent(IndexSongDO::getAlbumNames, reqVO.getAlbumNames())
                .eqIfPresent(IndexSongDO::getHot, reqVO.getHot())
                .eqIfPresent(IndexSongDO::getIsCopyright, reqVO.getIsCopyright())
                .orderByDesc(IndexSongDO::getId));
    }

    default List<IndexSongDO> selectList(AbstractGeneralSearchRequest request) {
        String keyword = StringUtils.trim(request.getText());
        if (StringUtils.isEmpty(keyword)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IndexSongDO>()
                .and( wrapper -> wrapper
                        .like(IndexSongDO::getName, keyword)
                        .or()
                        .like(IndexSongDO::getSingerNames, keyword)
                        .or()
                        .like(IndexSongDO::getAlbumNames, keyword))
                .orderByDesc(IndexSongDO::getHot)
                .orderByDesc(IndexSongDO::getId));
    }

    List<IndexSongDO> selectSyncList(@Param("lastId") Long lastId, @Param("batchSize") Integer batchSize);

    Long selectSyncCount();

}