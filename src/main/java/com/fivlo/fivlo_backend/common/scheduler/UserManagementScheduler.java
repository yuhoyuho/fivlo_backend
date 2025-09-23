package com.fivlo.fivlo_backend.common.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserManagementScheduler {

    private final JobLauncher jobLauncher;
    private final Job userManagementJob;

    @Scheduled(cron = "0 0 4 * * *")
    public void runUserManagementJob() throws Exception {
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("runTime", LocalDateTime.now().toString());

        jobLauncher.run(userManagementJob, builder.toJobParameters());
    }
}
