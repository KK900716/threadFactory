package com.example.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 44380
 * @version 2022~10~15~21:54
 */
public final class ExecuteFactory {
    private static final int DEFAULT_THREAD_NUM = 9;
    private volatile int exeId = 0;
    private final ExecutorService executorService;
    private final int threadNum;
    private final List<ReentrantLock> exeLocks;
    private final List<Condition> exeConditions;

    /**
     * 构建线程执行工厂。
     *
     * @param threadNum 线程数
     */
    public ExecuteFactory(final int threadNum,
                          final List<Connection> connections,
                          final List<PreparedStatement> preparedStatements) {
        this.threadNum = threadNum;
        executorService = Executors.newFixedThreadPool(threadNum);
        // 提交要按序提交，建立一把提交锁
        final ReentrantLock commitLock = new ReentrantLock();
        // 各线程间要按需通知依次提交，构建条件
        final Condition commitCondition = commitLock.newCondition();
        // 建立一个全局可用的标志变量，标识当前执行的线程
        final AtomicInteger exeSignal = new AtomicInteger(0);
        // 每个线程的执行必须等待该线程上个任务执行结束才能执行，构建一组执行锁
        exeLocks = new ArrayList<>(threadNum);
        // 构建一组线程执行条件，用于通知该不该执行下一个任务
        exeConditions = new ArrayList<>(threadNum);
        for (int i = 0; i < threadNum; i++) {
            final ReentrantLock exeLock = new ReentrantLock();
            final Condition exeCondition = exeLock.newCondition();
            exeLocks.add(exeLock);
            exeConditions.add(exeCondition);
            // 构建并执行线程
            executorService.execute(new Execute(commitLock, commitCondition, exeLock, exeCondition, exeSignal,
                    i, threadNum, connections.get(i), preparedStatements.get(i)));
        }
    }

    public ExecuteFactory(final List<Connection> connections,
                          final List<PreparedStatement> preparedStatements) {
        this(DEFAULT_THREAD_NUM, connections, preparedStatements);
    }

    /**
     * 执行一个任务，每次调用该方法必须是互斥的。
     */
    public void execute() {
        int exeIdValue;
        // 保证下一次执行execute方法是针对与下一个线程
        synchronized (this) {
            exeIdValue = exeId;
            exeId = exeId == threadNum - 1 ? 0 : exeId + 1;
        }
        // 当该该线程未执行完，则阻塞当前执行
        exeLocks.get(exeIdValue).lock();
        // 通知该线程执行
        exeConditions.get(exeIdValue).signalAll();
        exeLocks.get(exeIdValue).unlock();
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void shutdownNow() {
        executorService.shutdownNow();
    }
}
