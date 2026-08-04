package cn.qiany.basic.module.search.service.song;

import cn.qiany.basic.framework.common.pojo.PageResult;
import cn.qiany.basic.framework.common.util.object.BeanUtils;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchResponse;
import cn.qiany.basic.module.search.controller.admin.song.vo.IndexSongPageReqVO;
import cn.qiany.basic.module.search.controller.admin.song.vo.IndexSongSaveReqVO;
import cn.qiany.basic.module.search.dal.dataobject.song.IndexSongDO;
import cn.qiany.basic.module.search.dal.mysql.song.IndexSongMapper;
import cn.qiany.basic.module.search.templet.AbstractSearchTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.qiany.basic.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.qiany.basic.module.search.enums.ErrorCodeConstants.INDEX_SONG_NOT_EXISTS;

/**
 * 歌曲 Service 实现类
 *
 * @author fengpeng
 */
@Service
public class SearchSongServiceImpl extends AbstractSearchTemplate implements SearchSongService {
    @Resource
    private IndexSongMapper indexSongMapper;

    @Override
    public AbstractGeneralSearchResponse search(AbstractGeneralSearchRequest request) {
        preHandle(request);
        List<IndexSongDO> indexSongDOS = indexSongMapper.selectList();
        AbstractGeneralSearchResponse response = new AbstractGeneralSearchResponse();
        return afterHandle(request,response,indexSongDOS);
    }
}
