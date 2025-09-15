package com.bukadong.tcg.global.batch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/batch")
@Tag(name = "Batch", description = "배치 작업 수동 실행 API")
@Log4j2
public class BatchController {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Operation(summary = "심플잡 테스트 배치 실행", description = "잡을 수동으로 실행합니다.")
    @GetMapping("/simple-job")
    public String runCarInspectionAlertBatch(@RequestParam(value = "date", required = false) String date)
            throws Exception {
        String jobName = "simpleJob"; // 실행할 Job 이름 (simpleJob은 job 패키지 참고)
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date == null ? String.valueOf(System.currentTimeMillis()) : date).toJobParameters();
        jobLauncher.run(jobRegistry.getJob(jobName), jobParameters);
        log.info(jobName + " 실행 완료");
        return "ok";
    }
}
