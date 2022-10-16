package com.example.main;

import lombok.extern.slf4j.Slf4j;


/**
 * @author 44380
 * @version 2022~07~12~22:04
 */
@Slf4j
public class Main {
    public static void main(String[] args) {
        exe();
    }
    public static void exe() {
        ExecuteFactory executeFactory = new ExecuteFactory();
        for (int i = 0; i < 5000000; i++) {
            executeFactory.execute();
            log.info("{}",i);
        }
    }
}