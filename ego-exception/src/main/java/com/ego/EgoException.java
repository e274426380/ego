package com.ego;


import com.ego.IException;
import lombok.Data;
import org.slf4j.Logger;

@Data
public class EgoException extends RuntimeException {
    private Integer errorCode;
    private String errorMessage;

    public EgoException(IException iException) {
        this.errorCode = iException.getCode();
        this.errorMessage = iException.getMessage();
    }

    public static void error(Logger log, ExceptionEnum badKeyRequest) {
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static void error(Logger logger, IException e, Exception source) {
        logger.error("异常码[{}],异常提示[{}],异常详情:",
                e.getCode(), e.getMessage(),source);
        throw new EgoException(e);
    }
}
