
package com.chngc.collect.background;

import com.chngc.collect.service.SpiderHtmlYiJinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class SpiderYiJinConsumerListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private SpiderHtmlYiJinService spiderHtmlYiJinService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            int threadCount = 5;
            ExecutorService executorServiceSynMysql = Executors.newFixedThreadPool(threadCount);
            log.info("+++++++++【易金在线】爬取数据同步到数据库开始++++++++");
            for (int i = 0; i < threadCount; i++) {
                executorServiceSynMysql.execute(new Runnable() {
                    @Override
                    public void run() {
                        spiderHtmlYiJinService.startConsumQueue();
                    }
                });
            }
        }
    }
}

