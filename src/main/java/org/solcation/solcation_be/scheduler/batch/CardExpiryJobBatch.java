package org.solcation.solcation_be.scheduler.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.scheduler.dto.CardExpiryDTO;
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
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.*;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CardExpiryJobBatch {
    private static final String JOB_NAME = "cardExpiryJob";
    private static final String STEP_NAME = "cardExpiryStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    @Bean
    public Job cardExpProcessing(Step cardExpiryStep) {
        log.info("[Batch-start] Batch Start");
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new CustomJobParameterIncrementer())
                .start(cardExpiryStep)
                .build();
    }

    @Bean
    @JobScope
    public Step cardExpiryStep() {
        DefaultTransactionAttribute attr = new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED); //청크를 트랜잭션 단위로 취급
        attr.setTimeout(30); //30초 이내 트랜잭션 성공 요구

        return new StepBuilder(STEP_NAME, jobRepository)
                .<Long, CardExpiryDTO>chunk(50, transactionManager) //sac_pk -> CardExpiryDTO
                .reader(itemReader())
                .processor(itemProcessor())

                .writer(itemWriter())

                /* 내결함 */
                .faultTolerant()

                /* Retry */
                .retry(PessimisticLockingFailureException.class) //retry: 교착 예외
                .retry(CannotAcquireLockException.class) //retry: 락 획들 실패
                .retry(QueryTimeoutException.class) //retry: 타임아웃
                .retry(CannotGetJdbcConnectionException.class) //retry: 커넥션 오류
                .retry(DataAccessException.class) //retry: 자원 실패(네트워크 오류, 서버 오류 등)
                .retryLimit(3)
                .backOffPolicy(exponentialBackoff())

                /* Skip */
                .skip(DataIntegrityViolationException.class) //skip: 제약 조건 위반
                .skipLimit(25)

                /* No Rollback */
                .noRollback(EmptyResultDataAccessException.class) //no rollback: 0건 예외 취급 제외

                /* No Retry */
                .noRetry(EmptyResultDataAccessException.class) //no retry: 0건 예외 취급 제외
                .transactionAttribute(attr)
                .build();
    }

    /* reader */
    @Bean
    @StepScope
    public ItemReader<Long> itemReader() {
        log.info("[Batch-start] ItemReader Start");
        //파라미터 설정: KST 기준 년/월
        Map<String, Object> params = new LinkedHashMap<>();

        LocalDate ld = ZonedTimeUtil.now();
        params.put("year", ld.getYear());
        params.put("month", ld.getMonth().getValue());

        //정렬 기준 설정(sac_pk 오름차순)
        Map<String, Order> sort = new LinkedHashMap<>();
        sort.put("sac_pk", Order.ASCENDING);

        return new JdbcPagingItemReaderBuilder<Long>()
                .name("cardReader")
                .dataSource(dataSource)
                .parameterValues(params)
                .saveState(true)
                .pageSize(50)
                .rowMapper((rs, i) -> rs.getObject("sac_pk", Long.class))
                .sortKeys(sort)
                .selectClause("SELECT sac_pk")
                .fromClause("FROM shared_account_card_tb")
                .whereClause("""
                        WHERE cancellation = false
                        AND (
                            expiration_year < :year
                            OR (expiration_year = :year AND expiration_month <= :month)
                        )
                        """)
                .build();
    }

    /* processor */
    @Bean
    @StepScope
    public ItemProcessor<Long, CardExpiryDTO> itemProcessor() {
        log.info("[Batch-start] ItemProcessor Start");
        final Instant nowIns = Instant.now();
        return pk -> {
            log.info("[Batch-Processor] Read sac_pk = {}", pk);
            return CardExpiryDTO.builder()
                    .sacPk(pk)
                    .nowIns(nowIns)
                    .build();
        };
    }

    /* writer */
    @Bean
    @StepScope
    public JdbcBatchItemWriter<CardExpiryDTO> itemWriter() {
        log.info("[Batch-start] JdbcBatchItemWriter Start");
        return new JdbcBatchItemWriterBuilder<CardExpiryDTO>()
                .dataSource(dataSource)
                .sql("""
                    UPDATE shared_account_card_tb
                    SET cancellation = true, cancellation_date = :nowIns
                    WHERE sac_pk = :sacPk AND cancellation = false
                    """)
                .itemSqlParameterSourceProvider(item ->
                    new MapSqlParameterSource()
                            .addValue("sacPk", item.getSacPk())
                            .addValue("nowIns", item.getNowIns())
                )
                .assertUpdates(false)
                .build();
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