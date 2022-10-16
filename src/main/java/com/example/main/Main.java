package com.example.main;

import lombok.extern.slf4j.Slf4j;


/**
 * @author 44380
 * @version 2022~07~12~22:04
 */
@Slf4j
public class Main {
    public static void main(String[] args) {
        // 创建线程执行工厂，传入线程数，Connection[]
        ExecuteFactory executeFactory = new ExecuteFactory();
        for (int i = 0; i < 5000000; i++) {
            // 模拟流源源不断到来执行
            executeFactory.execute();
            log.info("{}",i);
        }
    }
}