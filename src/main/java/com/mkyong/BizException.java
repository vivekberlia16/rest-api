package com.mkyong;

import org.springframework.http.HttpStatus;

public class BizException extends Exception {

    private String code;
    private String message;
    private HttpStatus httpStatus;
    private Throwable cause;

    public BizException(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public BizException(String message) {
        this.message = message;
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.cause = cause;
    }

    public BizException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.cause = cause;
    }

}
