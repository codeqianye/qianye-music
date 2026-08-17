package cn.qiany.basic.abtest.exception;

/**
 * 本地流程引擎统一异常。
 *
 * <p>通用模块通过该异常向业务模块报告配置、解析和执行错误，
 * 避免依赖若依的业务异常体系。</p>
 */
public class AbFlowException extends RuntimeException {

    public AbFlowException(String message) {
        super(message);
    }

    public AbFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}