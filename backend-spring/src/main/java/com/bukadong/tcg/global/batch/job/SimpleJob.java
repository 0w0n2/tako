package com.bukadong.tcg.global.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * SimpleJob
 * - Reader / Processor / Writer 3단 구성
 * - 실제 로직은 모두 TODO로 비워둔 최소 골격
 */
@Configuration
@RequiredArgsConstructor
@Log4j2
public class SimpleJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionListener jobExecutionListener;

    /** ===================== Job ===================== */
    @Bean
    public Job simpleJobJob() {
        return new JobBuilder("simpleJob", jobRepository) // simpleJob 말고 다른 이름 사용 권장
                .listener(jobExecutionListener) // 처리 시간 로깅 리스너
                .start(simpleJobStep())
                .build();
    }

    /** ===================== Step ===================== */
    @Bean
    public Step simpleJobStep() {
        return new StepBuilder("simpleJobStep", jobRepository) // simpleJobStep 말고 다른 이름 사용 권장
                .<String, String>chunk(100, transactionManager) // chunk 단위 처리
                .reader(simpleReader()) // 읽기
                .processor(simpleProcessor()) // 처리
                .writer(simpleWriter()) // 쓰기
                .build();
    }

    /** ===================== Reader (TODO) ===================== */
    @Bean
    public ItemReader<String> simpleReader() {
        return () -> {
            // TODO: 읽기 로직 구현
            // 예: DB/JDBC 페이징 리더, 파일 리더, 큐/메시지 리더 등
            // 데이터가 더 이상 없으면 null 반환
            return null;
        };
    }

    /** ===================== Processor (TODO) ===================== */
    @Bean
    public ItemProcessor<String, String> simpleProcessor() {
        return item -> {
            // TODO: 처리(변환/필터링) 로직 구현
            // 특정 조건에서 필터링하려면 null 반환
            return item;
        };
    }

    /** ===================== Writer (TODO) ===================== */
    @Bean
    public ItemWriter<String> simpleWriter() {
        return items -> {
            // TODO: 쓰기 로직 구현
            // 예: DB 일괄 쓰기, 파일/외부 API 전송 등
        };
    }
}
