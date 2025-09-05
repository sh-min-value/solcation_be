package org.solcation.solcation_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/*
    SSE 하트비트 전송용 공용 스케줄러 풀
 */
@Configuration
public class SseConfig {
    @Bean(destroyMethod = "shutdown")
    public ScheduledThreadPoolExecutor heartbeatPool() {
        int poolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);

        //데몬 스레드로 설정: 백그라운드용 스레드
        ScheduledThreadPoolExecutor ex = new ScheduledThreadPoolExecutor(poolSize, r -> {
            Thread t = new Thread(r, "sse-heartbeat-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        });

        //취소된 작업을 대기 큐에서 즉시 제거
        ex.setRemoveOnCancelPolicy(true);
        return ex;
    }
}
