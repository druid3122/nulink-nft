package com.project.nulinknft.contract.task.listener;

import com.project.nulinknft.contract.event.listener.impl.BlockEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


@Component
public class Tasks {

    public static Logger logger = LoggerFactory.getLogger(Tasks.class);


    /**
     * 分布式锁本可解决该问题，但是由于锁有最长时间限制，就会一定会存在锁超时(但是任务没有执行完)被释放的问题，造成扫块任务可能同时重入的问题，增加本地同步块
     * 保证每个任务执行完了，在进入下一次定时去执行该任务
     */
    private static final Object blockListenerTaskKey = new Object();
    private static boolean lockBlockListenerTaskFlag = false;

    private static final Object blockListenerDelay3TaskKey = new Object();
    private static boolean lockBlockListenerDelay3TaskFlag = false;

    private static final Object blockListenerDelayTaskKey = new Object();
    private static boolean lockBlockListenerDelayTaskFlag = false;

    //springboot scheduled 解决多定时任务不执行的问题，多线程配置的几种方式 https://blog.csdn.net/liaoyi9203/article/details/109842925

    @Autowired
    BlockEventListener blockEventListener;

    @Autowired
    BlockEventListener blockEventDelayListener3;

    @Autowired
    BlockEventListener blockEventDelayListener15;

    @Async
    @Scheduled(cron = "0/6 * * * * ?")
    public void scanBlockEvent() {


        synchronized (blockListenerTaskKey) {
            if (Tasks.lockBlockListenerTaskFlag) {
                logger.warn("区块链事件扫描任务 已经在执行,等待执行完成...");
                return;
            }
            Tasks.lockBlockListenerTaskFlag = true;
        }

        logger.info("开始执行 区块链事件扫描任务");
        try {
            blockEventListener.start(0, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Tasks.lockBlockListenerTaskFlag = false;

        logger.info("结束执行 区块链事件扫描任务");
    }

    @Async
    @Scheduled(cron = "0/6 * * * * ?")
    public void scanBlockEventDelay3() {

        synchronized (blockListenerDelay3TaskKey) {
            if (Tasks.lockBlockListenerDelay3TaskFlag) {
                logger.warn("Delay3区块链事件扫描任务 已经在执行,等待执行完成...");
                return;
            }
            Tasks.lockBlockListenerDelay3TaskFlag = true;
        }

        logger.info("开始执行 Delay3区块链事件扫描任务");
        try {

            //TODO：等待将来 "Game2"，"Game3" 上线时，Game就不用扫了，下线了。需要屏蔽 enableTaskNameLists.add("Game"); 这一行
            Set<String> enableTaskNameLists = new HashSet<>();
            enableTaskNameLists.add("Game");
            enableTaskNameLists.add("Game2"); // 注意配置文件enabled同时必须设置为true, 才能真正启用
            enableTaskNameLists.add("Game3"); // 注意配置文件enabled同时必须设置为true, 才能真正启用
            blockEventDelayListener3.start(3, enableTaskNameLists, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Tasks.lockBlockListenerDelay3TaskFlag = false;

        logger.info("结束执行 Delay3区块链事件扫描任务");
    }

    //时间不要乱改，与 scanBlockEvent 一致，本来就延迟了15个块，大概75秒, 10个块时间太短.
    @Async
    @Scheduled(cron = "0/6 * * * * ?")
    public void scanBlockEventDelay15() {

        synchronized (blockListenerDelayTaskKey) {
            if (Tasks.lockBlockListenerDelayTaskFlag) {
                logger.warn("Delay15区块链事件扫描任务 已经在执行,等待执行完成...");
                return;
            }
            Tasks.lockBlockListenerDelayTaskFlag = true;
        }

        logger.info("开始执行 Delay15区块链事件扫描任务");
        try {

            blockEventDelayListener15.start(15, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Tasks.lockBlockListenerDelayTaskFlag = false;

        logger.info("结束执行 Delay15区块链事件扫描任务");
    }

}
