package com.example.HightConcurrence.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class ZkLockService {
    private Integer count = 100;
    @Resource
    private ZookeeperLock lock;
    //private Lock lock =  new ReentrantLock();
    public void threadJdk() throws InterruptedException {
        ThreadJdkRunnable threadJdkRunnable = new  ThreadJdkRunnable();
        Thread t1 = new Thread(threadJdkRunnable,"窗口A");
        Thread t2 = new Thread(threadJdkRunnable,"窗口B");
        Thread t3 = new Thread(threadJdkRunnable,"窗口C");
        Thread t4 = new Thread(threadJdkRunnable,"窗口D");
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        Thread.currentThread().join();
    }
    public class ThreadJdkRunnable implements Runnable{
        @Override
        public void run() {
            while (count > 0) {
                if (count > 0) {
                    //下面的方法解决不了分布式高并发
//                    synchronized (count) {}
                    try {
                    lock.lock();
                    log.info(Thread.currentThread().getName() + "第" + (count--) + "张票");

                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        lock.unlock();
                    }
                }
            }
        }
    }
}
