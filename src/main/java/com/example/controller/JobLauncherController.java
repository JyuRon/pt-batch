package com.example.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobLauncherController {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    /**
     * #Response Body [POST]
     * {
     *     "name" : "makeStatisticsJob",
     *     "jobParameters" : {
     *         "from" : "2022-09-01 00:00",
     *         "to" : "2022-09-11 00:00"
     *     }
     * }
     */
    @PostMapping("/launcher")
    public ExitStatus launchJob(@RequestBody JobLauncherRequest request) throws Exception{
        Job job = jobRegistry.getJob(request.getName());
        return jobLauncher.run(job, request.getJobParameters()).getExitStatus();
    }

}
