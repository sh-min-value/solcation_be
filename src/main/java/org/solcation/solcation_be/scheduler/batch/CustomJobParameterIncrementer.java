package org.solcation.solcation_be.scheduler.batch;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CustomJobParameterIncrementer implements JobParametersIncrementer {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy MM.dd HH:mm:ss SSS").withZone(ZoneId.of("Asia/Seoul"));


    @Override
    public JobParameters getNext(JobParameters jobParameters) {
        String id = FMT.format(Instant.now());

        return new JobParametersBuilder().addString("run.id", id).toJobParameters();
    }
}
