package cn.qiany.basic.module.search.service.es;

import cn.qiany.basic.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.qiany.basic.module.search.config.SongElasticsearchProperties;
import cn.qiany.basic.module.search.controller.admin.song.vo.es.BulkWriteResult;
import cn.qiany.basic.module.search.controller.admin.song.vo.es.IndexSongEsSyncResult;
import cn.qiany.basic.module.search.dal.dataobject.song.IndexSongDO;
import cn.qiany.basic.module.search.dal.elasticsearch.song.IndexSongEsDocument;
import cn.qiany.basic.module.search.dal.mysql.song.IndexSongMapper;
import cn.qiany.basic.module.search.enums.SearchEngineType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static cn.qiany.basic.framework.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * 负责将 MySQL 歌曲全量同步到 ES。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndexSongEsSyncService {

    private static final int MIN_BATCH_SIZE = 1;
    private static final int MAX_BATCH_SIZE = 2000;

    // 单实例内防止重复执行全量同步
    private final AtomicBoolean syncing = new AtomicBoolean(false);
    private final IndexSongMapper mapper;
    private final IndexSongEsConverter converter;
    private final IndexSongEsClient esClient;
    private final SongElasticsearchProperties properties;

    /**
     * 删除并重建索引，再按主键游标全量同步。
     *
     * @return 全量同步结果
     */
    public IndexSongEsSyncResult fullSync() {
        checkEngineIsMysql();
        if (!syncing.compareAndSet(false, true)) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"全量同步正在执行");
        }

        long start = System.currentTimeMillis();
        long lastId = 0L;
        long successCount = 0L;
        int batchCount = 0;
        try {
            int batchSize = checkBatchSize(
                    properties.getElasticsearch().getSyncBatchSize());
            Long mysqlCountValue = mapper.selectSyncCount();
            long mysqlCount = mysqlCountValue == null ? 0L : mysqlCountValue;

            // 先重建索引，避免残留已删除的 MySQL 数据
            esClient.deleteIndex();
            esClient.createIndex();

            while (true) {
                long batchStart = System.currentTimeMillis();
                List<IndexSongDO> rows = mapper.selectSyncList(lastId, batchSize);
                if (CollectionUtils.isEmpty(rows)) {
                    break;
                }

                List<IndexSongEsDocument> documents = converter.convertList(rows);
                BulkWriteResult writeResult = esClient.bulkIndex(documents);
                if (writeResult.hasFailure()) {
                    log.error("ES全量同步批次失败, batchNo={}, lastId={}, "
                                    + "successCount={}, failureCount={}, failureIds={}, costMs={}",
                            batchCount + 1, lastId,
                            writeResult.getSuccessCount(), writeResult.getFailureCount(),
                            writeResult.getFailureIds(),
                            System.currentTimeMillis() - batchStart);
                    throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"ES Bulk部分失败: {}",writeResult.getFailureMessage());

                }

                // 当前批次全部成功后才推进游标
                successCount += writeResult.getSuccessCount();
                lastId = rows.get(rows.size() - 1).getId();
                batchCount++;
                log.info("ES全量同步批次成功, batchNo={}, lastId={}, "
                                + "batchSize={}, successCount={}, costMs={}",
                        batchCount, lastId, rows.size(), writeResult.getSuccessCount(),
                        System.currentTimeMillis() - batchStart);
            }

            // 刷新后再核对 MySQL、Bulk 和 ES 三方数量
            esClient.refresh();
            long esCount = esClient.count();
            checkCount(mysqlCount, successCount, esCount);
            return buildSuccessResult(mysqlCount, esCount, batchCount,
                    successCount, System.currentTimeMillis() - start);
        } catch (RuntimeException e) {
            log.error("ES全量同步失败, batchNo={}, lastId={}, "
                            + "successCount={}, totalCostMs={}",
                    batchCount + 1, lastId, successCount,
                    System.currentTimeMillis() - start, e);
            throw e;
        } finally {
            // 无论成功或失败都释放同步标记
            syncing.set(false);
        }
    }

    private void checkEngineIsMysql() {
        if (properties.getEngine() != SearchEngineType.MYSQL) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"全量同步前必须将search.song.engine设为MYSQL");

        }
    }

    private int checkBatchSize(Integer batchSize) {
        if (batchSize == null
                || batchSize < MIN_BATCH_SIZE
                || batchSize > MAX_BATCH_SIZE) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"sync-batch-size必须在1至2000之间: {}",batchSize);

        }
        return batchSize;
    }

    private void checkCount(long mysqlCount,
                            long successCount,
                            long esCount) {
        if (mysqlCount != successCount || successCount != esCount) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode()
                    ,"同步数量不一致, mysqlCount={}"
                            + ", successCount={}"
                            + ", esCount={}",mysqlCount,successCount,esCount);
        }
    }

    private IndexSongEsSyncResult buildSuccessResult(long mysqlCount,
                                                      long esCount,
                                                      int batchCount,
                                                      long successCount,
                                                      long costMs) {
        IndexSongEsSyncResult result = new IndexSongEsSyncResult();
        result.setMysqlCount(mysqlCount);
        result.setEsCount(esCount);
        result.setBatchCount(batchCount);
        result.setSuccessCount(successCount);
        result.setCostMs(costMs);
        result.setSuccess(true);
        return result;
    }
}
