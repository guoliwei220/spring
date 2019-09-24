package com.chngc.collect.controller;

import com.chngc.collect.common.BaseController;
import com.chngc.collect.common.Constants;
import com.chngc.collect.common.ZhaoonlineConfig;
import com.chngc.collect.service.SpiderHtmlYiJinService;
import com.chngc.collect.service.SpiderZhaoonlineService;
import com.chngc.core.common.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RequestMapping("spider")
@RestController
public class PriceSpiderController extends BaseController {
    @Autowired
    private SpiderHtmlYiJinService spiderHtmlYiJinService;
    @Autowired
    private SpiderZhaoonlineService spiderZhaoonlineService;

    @GetMapping("yiJinPrice")
    public Mono<ResponseResult> spiderPriceYiJin(int startPageNo, int endPageNo){

        if(StringUtils.isNotBlank(String.valueOf(startPageNo)) && StringUtils.isNotBlank(String.valueOf(endPageNo)) && endPageNo >= startPageNo){
            log.info("手动执行爬取易金数据，开始页:"+startPageNo+",结束页:"+endPageNo);
            Boolean flag = spiderHtmlYiJinService.spiderHtmlYiJin(startPageNo, endPageNo);
            if(flag){
                return Mono.justOrEmpty(buildResult(Constants.NOLOGIN_CODE,"存在未处理完的数据，请稍后重试......"));
            }else {
                return Mono.justOrEmpty(buildResult(Constants.SUCCESS_CODE,"成功加入数据队列，正在处理....."));
            }
        }else {
            return Mono.justOrEmpty(buildResult(Constants.NOLOGIN_CODE,"请正确输入页数"));
        }
    }

    /**
     *
     * @param startPage 首页
     * @param endPage 末页
     * @return
     */
    @GetMapping(value = "spiderzhaobypage")
    public Mono<ResponseResult> spiderZhaoByPage(Integer startPage, Integer endPage) {
        if (startPage == null || endPage == null || startPage > endPage){
            return Mono.justOrEmpty(buildResult(Constants.NOLOGIN_CODE,"参数有误！"));
        }
        if(!ZhaoonlineConfig.lock){
            log.info("————————————手动执行爬取【赵涌在线】任务开始：第"+ startPage +"页，到第"+ endPage +"页————————————————");
            spiderZhaoonlineService.getCookie();// 获取cookie
            ZhaoonlineConfig.lock = true;
            ZhaoonlineConfig.URL_QUEUE_INTERFACE.add(ZhaoonlineConfig.PAGE_URL + startPage + ".htm");
            ZhaoonlineConfig.endPageUrl = ZhaoonlineConfig.PAGE_URL + endPage + ".htm";
            return Mono.justOrEmpty(buildResult(Constants.SUCCESS_CODE,"成功加入队列，正在爬取页面数据！"));
        } else {
            return Mono.justOrEmpty(buildResult(Constants.NOLOGIN_CODE,"上次手动设置爬取任务还未执行完毕，请稍后重试！"));
        }
    }
}
