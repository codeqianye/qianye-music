package cn.qiany.basic.module.search.enums;

import cn.qiany.basic.framework.common.exception.ErrorCode;

/**
 * Infra 错误码枚举类
 *
 * infra 系统，使用 1-001-000-000 段
 */
public interface ErrorCodeConstants {
    ErrorCode INDEX_SONG_NOT_EXISTS = new ErrorCode(1_004_000_001, "歌曲不存在");

}
