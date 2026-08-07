package cn.qiany.basic.server;

import cn.qiany.basic.module.search.dal.elasticsearch.song.IndexSongEsDocument;
import cn.qiany.basic.module.search.service.es.BulkWriteResult;
import cn.qiany.basic.module.search.service.es.IndexSongEsClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class EsTest {
    @Resource
    private IndexSongEsClient esClient;

    @Test
    public void esClientTest(){
        BulkWriteResult result = esClient.bulkIndex(buildValidDocuments());
        esClient.refresh();

        Assertions.assertFalse(result.hasFailure());
        Assertions.assertEquals(2, result.getSuccessCount());
        Assertions.assertEquals(0, result.getFailureCount());
        Assertions.assertEquals(2L, esClient.count());
    }

    @Test
    public void esClientTest1(){
        esClient.deleteIndex();
        BulkWriteResult result = esClient.bulkIndex(buildValidDocuments());
        esClient.refresh();

        Assertions.assertFalse(result.hasFailure());
        Assertions.assertEquals(2, result.getSuccessCount());
        Assertions.assertEquals(0, result.getFailureCount());
        Assertions.assertEquals(2L, esClient.count());
    }

    private List<IndexSongEsDocument> buildValidDocuments() {
        IndexSongEsDocument first = new IndexSongEsDocument();
        first.setId("ES2_T07_001");
        first.setDbId(9000001L);
        first.setName("T07测试歌曲一");
        first.setSingerIds("ES2_SINGER_001");
        first.setSingerNames("T07测试歌手");
        first.setAlbumNames("T07测试专辑");
        first.setHot(1000L);
        first.setIsCopyright(1);
        first.setInvalidate(null);
        first.setIsEnabled(1);
        first.setFirstStartState(1);
        first.setReleaseDate(LocalDate.of(2026, 1, 1));
        first.setUpdateTime(LocalDateTime.of(2026, 8, 7, 10, 0));

        IndexSongEsDocument second = new IndexSongEsDocument();
        second.setId("ES2_T07_002");
        second.setDbId(9000002L);
        second.setName("T07测试歌曲二");
        second.setSingerIds("ES2_SINGER_002");
        second.setSingerNames("T07测试歌手");
        second.setAlbumNames(null);
        second.setHot(900L);
        second.setIsCopyright(1);
        second.setInvalidate(LocalDate.of(2099, 12, 31));
        second.setIsEnabled(1);
        second.setFirstStartState(1);
        second.setReleaseDate(LocalDate.of(2026, 2, 1));
        second.setUpdateTime(LocalDateTime.of(2026, 8, 7, 10, 5));

        return Arrays.asList(first, second);
    }
}
