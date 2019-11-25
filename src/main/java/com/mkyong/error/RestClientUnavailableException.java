package com.mkyong.error;


import com.mkyong.BizException;
import org.springframework.http.HttpStatus;



public class RestClientUnavailableException extends BizException {

    public static final String DEFAULT_MESSAGE="Downstream service is unavailable";

    public RestClientUnavailableException(String message) {
        super(message);
    }

    public RestClientUnavailableException()
    {
        super(DEFAULT_MESSAGE);

    }


    public RestClientUnavailableException(String code, String message, HttpStatus httpStatus) {
        super(code, message, httpStatus);
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        return message;
    }


}
