package com.chngc.collect.common;

import com.chngc.core.common.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.HtmlUtils;

/**
 * 控制器支持类
 */
@Slf4j
public abstract class BaseController {
    /**
     * 客户端返回JSON字符串
     */
    protected ResponseResult buildResult(String code, String message) {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(code);
        responseResult.setMsg(message);
        return responseResult;
    }

    /**
     * 客户端返回JSON字符串
     */
    protected ResponseResult buildResult(String code, String message, Object data) {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(code);
        responseResult.setMsg(message);
        responseResult.setData(data);
        return responseResult;
    }

    /**
     * 客户端返回JSON字符串
     */
    protected ResponseResult buildResult(Object data) {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(Constants.SUCCESS_CODE);
        responseResult.setData(data);
        return responseResult;
    }

    /**
     * 将所有传递进来的String进行HTML编码，防止XSS攻击
     *
     * @param str
     * @return
     */
    protected String htmlEscape(String str) {
        return str == null ? null : HtmlUtils.htmlEscape(str.trim());
    }
}
