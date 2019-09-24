package com.chngc.collect.common;

import com.chngc.core.common.ResponseResult;
import com.chngc.core.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@RestControllerAdvice
@Slf4j
public class DefaultExceptionHandler {
    /**
     * 异常处理
     * @param ex
     * @return
     */
    @ExceptionHandler({BindException.class, RuntimeException.class, Exception.class})
    public String bindException(Exception ex) {
        log.error(ex.getMessage(), ex);
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode("-1");
        responseResult.setMsg("error");
        responseResult.setData(new HashMap());
        return JsonUtils.toJsonStr(responseResult);
    }
}
