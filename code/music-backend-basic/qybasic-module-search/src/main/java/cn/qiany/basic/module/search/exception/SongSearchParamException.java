package cn.qiany.basic.module.search.exception;

/**
 * 表示单曲搜索参数不合法。
 */
public class SongSearchParamException extends RuntimeException {

    public SongSearchParamException(String message) {
        super(message);
    }
}