package com.bobooi.watch.common.advice;

import com.bobooi.watch.common.exception.ApplicationException;
import com.bobooi.watch.common.response.ApplicationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.bobooi.watch.common.response.SystemCodeEnum.*;


/**
 * @author bobo
 * @date 2021/3/31
 */

@RestController
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ApplicationResponse<String> defaultErrorHandler(HttpServletRequest request, Exception exception){
        if(exception instanceof ApplicationException){
            ApplicationException applicationException = (ApplicationException) exception;
            log.info("捕获应用错误", exception);
            return ApplicationResponse.fail(applicationException.getSystemCode(), applicationException.getResponseMessage());
        } else if (exception instanceof IllegalArgumentException
                || exception instanceof HttpMessageNotReadableException
                || exception instanceof MethodArgumentNotValidException
                || exception instanceof BindException) {
            log.info("捕获参数错误", exception);
            return ApplicationResponse.fail(ARGUMENT_WRONG);
        } else if (exception instanceof HttpRequestMethodNotSupportedException) {
            log.debug("捕获调用方法错误", exception);
            return ApplicationResponse.fail(REQUEST_METHOD_NOT_SUPPPORTED);
        }
        log.error("捕获意外异常", exception);
        return ApplicationResponse.fail(UNKNOWN_ERROR);
    }
}
