package org.solcation.solcation_be.scheduler.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.wallet.account.SharedAccountService;
import org.solcation.solcation_be.entity.enums.DEPOSITCYCLE;
import org.solcation.solcation_be.scheduler.dto.DepositAlarmDTO;
import org.solcation.solcation_be.util.timezone.ZonedTimeUtil;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.*;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RegularDepositAlarmJobBatch {
    private static final String JOB_NAME = "RegularDepositAlarmJob";
    private static final String STEP_NAME = "RegularDepositAlarmStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final SharedAccountService sharedAccountService;

    @Bean
    public Job regularDepositAlarmJob() {
        log.info("[Batch-start] Batch Start");
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new CustomJobParameterIncrementer())
                .start(regularDepositAlarmStep())
                .build();
    }

    @Bean
    @JobScope
    public Step regularDepositAlarmStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<DepositAlarmDTO, DepositAlarmDTO>chunk(1000, transactionManager)
                .reader(depositReader())
                .processor(depositProcessor())
                .writer(depositWriter())
                .faultTolerant()

                /* Retry */
                .retry(PessimisticLockingFailureException.class) //retry: 교착 예외
                .retry(CannotAcquireLockException.class) //retry: 락 획들 실패
                .retry(QueryTimeoutException.class) //retry: 타임아웃
                .retry(CannotGetJdbcConnectionException.class) //retry: 커넥션 오류
                .retryLimit(3)
                .backOffPolicy(exponentialBackoff())

                /* Skip */
                .skip(DataIntegrityViolationException.class) //skip: 제약 조건 위반
                .skip(CustomException.class) //skip: 비즈니스 로직 오류
                .skip(org.hibernate.exception.ConstraintViolationException.class)
                .skip(java.sql.SQLIntegrityConstraintViolationException.class)
                .skip(DataIntegrityViolationException.class)
                .skipLimit(25)

                /* No Rollback */
                .noRollback(EmptyResultDataAccessException.class) //no rollback: 0건 예외 취급 제외

                /* No Retry */
                .noRetry(EmptyResultDataAccessException.class) //no retry: 0건 예외 취급 제외
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<DepositAlarmDTO> depositReader() {
        log.info("[Batch-start] depositReader Start");
        LocalDate todayKst = ZonedTimeUtil.now();

        int dowMon0 = switch (todayKst.getDayOfWeek()) {
            case MONDAY -> 0; case TUESDAY -> 1; case WEDNESDAY -> 2;
            case THURSDAY -> 3; case FRIDAY -> 4; case SATURDAY -> 5; default -> 6; // SUNDAY
        };

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("today", todayKst);
        params.put("dow", dowMon0);
        params.put("cycleMonth", DEPOSITCYCLE.MONTH.getCode());
        params.put("cycleWeek", DEPOSITCYCLE.WEEK.getCode());

        //정렬키
        Map<String, Order> sort = new LinkedHashMap<>();
        sort.put("saPk", Order.ASCENDING);

        return new JdbcPagingItemReaderBuilder<DepositAlarmDTO>()
                .name("depositReader")
                .dataSource(dataSource)
                .pageSize(1000)
                .parameterValues(params)
                .rowMapper(new BeanPropertyRowMapper<>(DepositAlarmDTO.class))
                .sortKeys(sort)
                .selectClause("""
                        SELECT
                            sa_pk AS saPk,
                            deposit_alarm AS depositAlarm,
                            deposit_cycle AS depositCycle,
                            deposit_date AS depositDate,
                            deposit_day AS depositDay,
                            deposit_amount AS depositAmount
                        """)
                .fromClause("FROM shared_account_tb")
                .whereClause("""
                        WHERE deposit_alarm = 1
                        AND (
                            (deposit_cycle = :cycleMonth
                                AND DAY(:today) = LEAST(deposit_date, DAY(LAST_DAY(:today)))
                            )
                            OR (deposit_cycle = :cycleWeek
                                AND deposit_day = :dow
                            )
                        )
                        """)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<DepositAlarmDTO, DepositAlarmDTO> depositProcessor() {
        log.info("[Batch-start] depositProcessor Start");
        LocalDate today = ZonedTimeUtil.now();

        return item -> {
            log.info("[Batch-Processor] Read sa_pk = {}", item.getSaPk());
            //예외 처리
            if(item.getDepositCycle() == null){
                item.updateDisableAlarm();
                return item;
            }

            int cycle = item.getDepositCycle();

            if(cycle == DEPOSITCYCLE.MONTH.getCode()) {
                if(item.getDepositDate() == null){
                    item.updateDisableAlarm();
                    return item;
                }
            } else if(cycle == DEPOSITCYCLE.WEEK.getCode()) {
                if(item.getDepositDay() == null) {
                    item.updateDisableAlarm();
                    return item;
                }
            } else {
                item.updateDisableAlarm();
                return item;
            }

            item.updateNotifyToday();
            return item;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<DepositAlarmDTO> depositWriter() {
        log.info("[Batch-start] depositWriter Start");
        return items -> {
            for(DepositAlarmDTO item: items) {
                if(item.isDisableAlarm()) {
                    //정기 알림 비활성화
                    log.info("[Batch-Writer] Disable sa_pk = {}", item.getSaPk());
                    sharedAccountService.disableDepositCycle(item.getSaPk());
                } else if(item.isNotifyToday()) {
                    //알림 전송
                    log.info("[Batch-Writer] Send Notification sa_pk = {}", item.getSaPk());
                    sharedAccountService.sendRegularDepositAlarm(item.getSaPk());
                }
            }
        };
    }

    /* 재시도(Retry) 까지의 지연 시간(ms) 설정 */
    @Bean
    public ExponentialBackOffPolicy exponentialBackoff() {
        var p = new ExponentialBackOffPolicy();
        p.setInitialInterval(50);
        p.setMultiplier(2.0);
        p.setMaxInterval(2000);
        return p;
    }
}
