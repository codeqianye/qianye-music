
package cn.qiany.basic.module.search.exception;

/**
 * 表示 ES 命中数据无法映射为业务对象。
 */
public class SongEsResultMappingException extends RuntimeException {

    public SongEsResultMappingException(String message) {
        super(message);
    }

    public SongEsResultMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}