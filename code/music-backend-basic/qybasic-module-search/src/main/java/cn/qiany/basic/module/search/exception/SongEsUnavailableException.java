package cn.qiany.basic.module.search.exception;

public class SongEsUnavailableException extends RuntimeException {

    public SongEsUnavailableException(String message) {
        super(message);
    }

    public SongEsUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}