package org.solcation.solcation_be.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class BatchSchedulers {
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;

    private final Job cardExpProcessing;
    private final Job updateTravelStateJob;
    private final Job regularDepositAlarmJob;

    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
    public void runCardExpProcessing() throws Exception {
        launchIfNotRunning(cardExpProcessing, "card-exp-processing");
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void runUpdateTravelStateJob() throws Exception {
        launchIfNotRunning(updateTravelStateJob, "update-travel-state");
    }

    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Seoul")
    public void runRegularDepositAlarmJob() throws Exception {
        launchIfNotRunning(regularDepositAlarmJob, "regular-deposit-alarm");
    }

    private void launchIfNotRunning(Job job, String trigger) throws Exception {
        // 이미 실행 중이면 중복 기동 방지
        if (!jobExplorer.findRunningJobExecutions(job.getName()).isEmpty()) {
            log.warn("[Scheduler] {} is already running. Skip.", job.getName());
            return;
        }

        var nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        var params = new JobParametersBuilder()
                .addString("trigger", trigger)
                .addString("kstDate", nowKst.toLocalDate().toString())
                .addLong("ts", System.currentTimeMillis())
                .toJobParameters();

        log.info("[Scheduler] Launch {} at {}", job.getName(), nowKst);
        jobLauncher.run(job, params);
    }
}
