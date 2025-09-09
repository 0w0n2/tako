package com.bukadong.tcg.global.batch.schedule;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
public class BatchSchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public BatchSchedule(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

    // 매일 01시 마다 실행
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void runCarInspectionAlertJob() throws Exception {
        String jobName = "simpleJob"; // 실행할 Job 이름 (simpleJob은 job 패키지 참고)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // 날짜 형식 지정
        String date = dateFormat.format(new Date());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date)
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob(jobName), jobParameters);
    }


}
