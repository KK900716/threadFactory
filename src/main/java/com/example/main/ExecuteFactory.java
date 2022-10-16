package com.example.main;

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

    public ExecuteFactory(final int threadNum) {
        this.threadNum = threadNum;
        executorService = Executors.newFixedThreadPool(threadNum);
        final ReentrantLock commitLock = new ReentrantLock();
        final Condition commitCondition = commitLock.newCondition();
        final AtomicInteger exeSignal = new AtomicInteger(0);
        exeLocks = new ArrayList<>(threadNum);
        exeConditions = new ArrayList<>(threadNum);
        for (int i = 0; i < threadNum; i++) {
            final ReentrantLock exeLock = new ReentrantLock();
            final Condition exeCondition = exeLock.newCondition();
            exeLocks.add(exeLock);
            exeConditions.add(exeCondition);
            executorService.execute(new Execute(commitLock, commitCondition, exeLock, exeCondition, exeSignal,
                    i, threadNum));
        }
    }

    public ExecuteFactory() {
        this(DEFAULT_THREAD_NUM);
    }

    public synchronized void execute() {
        exeLocks.get(exeId).lock();
        exeConditions.get(exeId).signalAll();
        exeLocks.get(exeId).unlock();
        exeId = exeId == threadNum - 1 ? 0 : exeId + 1;
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void shutdownNow() {
        executorService.shutdownNow();
    }
}
