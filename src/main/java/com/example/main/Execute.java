package com.example.main;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 44380
 * @version 2022~10~15~21:22
 */
@Slf4j
public final class Execute implements Runnable {
    // private Connection connection;
    // private PreparedStatement preparedStatement;
    private final ReentrantLock commitLock;
    private final Condition commitCondition;
    private final ReentrantLock exeLock;
    private final Condition exeCondition;
    private final AtomicInteger exeId;
    private final int id;
    private final int nextExecuteId;

    public Execute(final ReentrantLock commitLock,
                   final Condition commitCondition,
                   final ReentrantLock exeLock,
                   final Condition exeCondition,
                   final AtomicInteger exeId,
                   final int id,
                   final int threadNum) {
        this.commitLock = commitLock;
        this.commitCondition = commitCondition;
        this.exeLock = exeLock;
        this.exeCondition = exeCondition;
        this.exeId = exeId;
        this.id = id;
        nextExecuteId = id == threadNum - 1 ? 0 : id + 1;
    }

    @SneakyThrows
    @Override
    public void run() {
        for (; ; ) {
            // 执行锁，限制该线程每次
            exeLock.lock();
            exeCondition.await();
            // preparedStatement.executeBatch();
            log.debug("Id: {}, is executing", Thread.currentThread().getId());
            Thread.sleep(2000);
            commitLock.lock();
            while (this.exeId.get() != id) {
                commitCondition.await();
            }
            log.info("Id: {}, is committing {}", Thread.currentThread().getId(), id);
            Thread.sleep(500);
            // connection.commit();
            exeId.set(nextExecuteId);
            commitCondition.signalAll();
            commitLock.unlock();
            exeLock.unlock();
        }
    }
}
