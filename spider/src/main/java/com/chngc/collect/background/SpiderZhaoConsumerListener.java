
package com.chngc.collect.background;

import com.chngc.collect.common.ZhaoonlineConfig;
import com.chngc.collect.service.SpiderZhaoonlineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class SpiderZhaoConsumerListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private SpiderZhaoonlineService spiderZhaoonlineService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            log.info("赵涌在线-队列监控已启动");
            ExecutorService executorService = Executors.newFixedThreadPool(ZhaoonlineConfig.THREAD_NUM);
            for (int i = 0; i < ZhaoonlineConfig.THREAD_NUM; i++) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        spiderZhaoonlineService.startConsumQueue();
                    }
                });
            }
        }
    }
}

