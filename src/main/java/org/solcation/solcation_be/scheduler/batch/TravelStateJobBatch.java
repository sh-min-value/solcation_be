package org.solcation.solcation_be.scheduler.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.scheduler.dto.TravelDTO;
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
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TravelStateJobBatch {

    private static final String JOB_NAME  = "updateTravelStateJob";
    private static final String STEP_NAME = "updateTravelStateStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    /* Job */
    @Bean
    public Job updateTravelStateJob(Step updateTravelStateStep) {
        log.info("[Batch-start] TravelState Job Start");
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new CustomJobParameterIncrementer())
                .start(updateTravelStateStep)
                .build();
    }

    /* Step */
    @Bean
    @JobScope
    public Step updateTravelStateStep(
            ItemReader<TravelDTO> travelStateItemReader,
            ItemProcessor<TravelDTO, TravelDTO> travelStateItemProcessor,
            JdbcBatchItemWriter<TravelDTO> travelStateItemWriter,
            ExponentialBackOffPolicy travelStateExponentialBackoff
    ) {
        DefaultTransactionAttribute attr =
                new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED);
        attr.setTimeout(30);

        return new StepBuilder(STEP_NAME, jobRepository)
                .<TravelDTO, TravelDTO>chunk(1000, transactionManager)
                .reader(travelStateItemReader)
                .processor(travelStateItemProcessor)
                .writer(travelStateItemWriter)

                .faultTolerant()

                .retry(org.springframework.dao.PessimisticLockingFailureException.class)
                .retry(CannotAcquireLockException.class)
                .retry(org.springframework.dao.QueryTimeoutException.class)
                .retryLimit(3)
                .backOffPolicy(travelStateExponentialBackoff)

                .skip(DataIntegrityViolationException.class)
                .skipLimit(25)

                .noRollback(EmptyResultDataAccessException.class)
                .noRetry(EmptyResultDataAccessException.class)

                .transactionAttribute(attr)
                .build();
    }

    /* Reader */
    @Bean
    @StepScope
    public ItemReader<TravelDTO> travelStateItemReader() {
        //KST 기준 현재 시각
        LocalDate base = ZonedTimeUtil.now();
        Map<String, Object> params = new  LinkedHashMap<>();
        params.put("base", base);
        params.put("before", TRAVELSTATE.BEFORE.getCode());
        params.put("ongoing", TRAVELSTATE.ONGOING.getCode());
        params.put("finish", TRAVELSTATE.FINISH.getCode());

        log.info("[Batch-start] TravelState Reader Start (runDate={})", base);

        // 정렬 키
        Map<String, Order> sort = new LinkedHashMap<>();
        sort.put("pk", Order.ASCENDING);

        return new JdbcPagingItemReaderBuilder<TravelDTO>()
                .name("travelStateReader")
                .dataSource(dataSource)
                .parameterValues(params)
                .saveState(true)
                .pageSize(1000)
                .rowMapper(new BeanPropertyRowMapper<>(TravelDTO.class))
                .sortKeys(sort)
                .selectClause("""
                    SELECT
                        tp_pk    AS pk,
                        tp_start AS startDate,
                        tp_end   AS endDate,
                        tp_state AS state
                    """)
                .fromClause("FROM travel_plan_tb")
                .whereClause("""
                    WHERE
                        (tp_end   < :base AND tp_state <> :finish) /* FINISH 후보 */
                     OR (tp_start <= :base AND tp_end >= :base AND tp_state <> :ongoing) /* ONGOING 후보 */
                     OR (tp_start  > :base AND tp_state <> :before) /* BEFORE 후보 */
                    """)
                .build();
    }

    /* Processor */
    @Bean
    @StepScope
    public ItemProcessor<TravelDTO, TravelDTO> travelStateItemProcessor() {
        //KST 기준 현재 시각
        LocalDate base = ZonedTimeUtil.now();
        log.info("[Batch-start] TravelState Processor Start (runDate={})", base);

        int finish = TRAVELSTATE.FINISH.getCode();
        int ongoing = TRAVELSTATE.ONGOING.getCode();
        int before = TRAVELSTATE.BEFORE.getCode();

        return item -> {
            int newState;
            if (item.getEndDate().isBefore(base)) {
                newState = finish; // FINISH
            } else if (item.getStartDate().isAfter(base)) {
                newState = before; // BEFORE
            } else {
                newState = ongoing; // ONGOING
            }

            Integer cur = item.getState();
            if (!Objects.equals(cur, newState)) {
                item.setState(newState);
                log.info("[Batch-Processor] pk={} state {} -> {}", item.getPk(), cur, newState);
                return item; // 변경건만 전달
            }
            return null; // 미변경 시 필터링
        };
    }

    /* Writer */
    @Bean
    @StepScope
    public JdbcBatchItemWriter<TravelDTO> travelStateItemWriter() {
        log.info("[Batch-start] TravelState Writer Start");

        return new JdbcBatchItemWriterBuilder<TravelDTO>()
                .dataSource(dataSource)
                .sql("""
                    UPDATE travel_plan_tb
                       SET tp_state = :state
                     WHERE tp_pk   = :pk
                    """)
                .itemSqlParameterSourceProvider(
                        new org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider<>())
                .assertUpdates(false)
                .build();
    }

    /* Exponential Backoff (Retry 간 지연) */
    @Bean(name = "travelStateExponentialBackoff")
    public ExponentialBackOffPolicy travelStateExponentialBackoff() {
        var p = new ExponentialBackOffPolicy();
        p.setInitialInterval(50);
        p.setMultiplier(2.0);
        p.setMaxInterval(2000);
        return p;
    }
}
