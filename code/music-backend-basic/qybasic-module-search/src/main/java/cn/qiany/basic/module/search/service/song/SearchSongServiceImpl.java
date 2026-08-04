package cn.qiany.basic.module.search.service.song;

import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchResponse;
import cn.qiany.basic.module.search.dal.dataobject.song.IndexSongDO;
import cn.qiany.basic.module.search.dal.mysql.song.IndexSongMapper;
import cn.qiany.basic.module.search.templet.AbstractSearchTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
        List<IndexSongDO> indexSongDOS = indexSongMapper.selectList(request);
        AbstractGeneralSearchResponse response = new AbstractGeneralSearchResponse();
        return afterHandle(request,response,indexSongDOS);
    }
}
