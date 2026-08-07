package cn.qiany.basic.module.search.exception;

public class SongEsSyncException extends RuntimeException {

    public SongEsSyncException(String message) {
        super(message);
    }

    public SongEsSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}