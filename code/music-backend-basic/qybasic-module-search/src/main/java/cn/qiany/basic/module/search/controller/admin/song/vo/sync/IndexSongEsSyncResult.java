package cn.qiany.basic.module.search.controller.admin.song.vo.sync;

import lombok.Data;

/**
 * 记录一次 ES 全量同步的结果。
 */
@Data
public class IndexSongEsSyncResult {
    // MySQL 有效数据量
    private long mysqlCount;
    // ES 最终文档数
    private long esCount;
    // 实际执行批次数
    private int batchCount;
    // Bulk 成功写入数
    private long successCount;
    // 同步总耗时
    private long costMs;
    // 数量校验是否通过
    private boolean success;
}