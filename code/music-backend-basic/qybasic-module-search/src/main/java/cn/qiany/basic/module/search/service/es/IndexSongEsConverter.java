package cn.qiany.basic.module.search.service.es;

import cn.hutool.core.collection.CollUtil;
import cn.qiany.basic.module.search.dal.dataobject.song.IndexSongDO;
import cn.qiany.basic.module.search.dal.elasticsearch.song.IndexSongEsDocument;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据库对象转成ES对象
 */
@Component
public class IndexSongEsConverter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 单个对象数据转换
     * @param source
     * @return
     */
    public IndexSongEsDocument convert(IndexSongDO source) {
        if (source == null) {
            return null;
        }
        if (StringUtils.isBlank(source.getOrgId())) {
            throw new RuntimeException("orgId为空, dbId=" + source.getId());
        }
        IndexSongEsDocument target = new IndexSongEsDocument();
        target.setId(source.getOrgId());
        target.setDbId(source.getId());
        target.setName(source.getName());
        target.setSingerIds(source.getSingerIds());
        target.setSingerNames(source.getSingerNames());
        target.setAlbumNames(source.getAlbumNames());
        target.setHot(source.getHot());
        target.setIsCopyright(source.getIsCopyright());
        target.setInvalidate(source.getInvalidate());
        target.setIsEnabled(source.getIsEnabled());
        target.setFirstStartState(source.getFirstStartState());
        target.setReleaseDate(source.getReleaseDate());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }

    /**
     * 批量数据转换
     * @param sources
     * @return
     */
    public List<IndexSongEsDocument> convertList(List<IndexSongDO> sources) {
        if (CollUtil.isEmpty(sources)) {
            return Collections.emptyList();
        }
        return sources.stream().map(this::convert).collect(Collectors.toList());
    }

    public Map<String, Object> toSource(IndexSongEsDocument document) {
        Map<String, Object> source = new LinkedHashMap<>();
        putIfNotNull(source, "id", document.getId());
        putIfNotNull(source, "dbId", document.getDbId());
        putIfNotNull(source, "name", document.getName());
        putIfNotNull(source, "singerIds", document.getSingerIds());
        putIfNotNull(source, "singerNames", document.getSingerNames());
        putIfNotNull(source, "albumNames", document.getAlbumNames());
        putIfNotNull(source, "hot", document.getHot());
        putIfNotNull(source, "isCopyright", document.getIsCopyright());
        putIfNotNull(source, "invalidate", format(document.getInvalidate()));
        putIfNotNull(source, "isEnabled", document.getIsEnabled());
        putIfNotNull(source, "firstStartState", document.getFirstStartState());
        putIfNotNull(source, "releaseDate", format(document.getReleaseDate()));
        putIfNotNull(source, "updateTime",
                document.getUpdateTime() == null ? null : document.getUpdateTime().format(DATE_TIME_FORMATTER));
        return source;
    }

    private static void putIfNotNull(Map<String, Object> source,
                                     String field,
                                     Object value) {
        if (value != null) {
            source.put(field, value);
        }
    }

    private static String format(LocalDate date) {
        return date == null ? null : date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}