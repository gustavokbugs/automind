package com.automind.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class NumeroOSGenerator {

    private static final AtomicInteger contador = new AtomicInteger(0);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private NumeroOSGenerator() {}

    public static String gerar() {
        String data = LocalDateTime.now().format(FMT);
        int seq = contador.incrementAndGet() % 10000;
        return String.format("OS-%s-%04d", data, seq);
    }
}
