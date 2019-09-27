package com.example.HightConcurrence.service;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
@Slf4j
@Service
public class ZookeeperLock  implements Lock {
    private static final String LOCK_PATH = "/LOCK";
    private static final String ZOOKEEPER_IP_PORT = "localhost:2181";
    private ZkClient zkClient = new ZkClient(ZOOKEEPER_IP_PORT,1000,1000, new SerializableSerializer());
    private CountDownLatch countDownLatch;//信号量
    private String beforePath;//当前请求节点
    private String currentPath;//当前请求节点的前一个节点

    //判断有没有LOCK目录，没有就创建
    @Override
    public void lock() {
        if(!tryLock()){
            waitLock();
            lock();
        }else {
            log.info(Thread.currentThread().getName() + "获得分布式锁");
        }
    }

    private void ZookeeperImprove(){
        //判断有没有lock记录，如果没有则创建
        if(!this.zkClient.exists(LOCK_PATH)){
            this.zkClient.createPersistent(LOCK_PATH);
        }

    }

    private void waitLock(){
        IZkDataListener listener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {

            }
            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                log.info(Thread.currentThread().getName() + "获取到dataDelete时间=============================");
                if(countDownLatch != null){
                    countDownLatch.countDown();
                }
            }
        };
        //给排在前面的节点增加数据删除的watcher
        this.zkClient.subscribeDataChanges(beforePath,listener);
        if(this.zkClient.exists(beforePath)){
            countDownLatch = new CountDownLatch(1);
            try {
                countDownLatch.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.zkClient.unsubscribeDataChanges(beforePath,listener);
    }

    @Override
    public boolean tryLock() {
        //如果currentPath为空则第一次尝试加锁，第一次加锁默认currentPath
        if(currentPath == null || currentPath.length() <= 0){
            //创建一个临时节点
            currentPath = this.zkClient.createEphemeralSequential(LOCK_PATH + '/',"lock");
            log.info("临时节点===================================>"+currentPath);
        }
        //获取所有的临时节点排序
        List<String> childrens = this.zkClient.getChildren(LOCK_PATH);
        Collections.sort(childrens);
        //如果当前节点在所有节点的排序中第一个，则加锁成功
        if(currentPath.equals(LOCK_PATH + "/"+ childrens.get(0))){
            return true;
        }else {
            //如果当前节点在所有节点的排名不在第一位，则获取前面的节点名称，并赋值给beforePath
            int wz = Collections.binarySearch(childrens,currentPath.substring(6));
            beforePath = LOCK_PATH + "/" + childrens.get(wz-1);
        }
        return false;
    }
    @Override
    public void unlock() {
        //删除临时节点
        zkClient.delete(currentPath);
    }







    //======================================================
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public Condition newCondition() {
        return null;
    }
    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

}
