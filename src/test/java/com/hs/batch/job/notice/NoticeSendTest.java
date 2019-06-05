package com.hs.batch.job.notice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
//@ActiveProfiles("prod") // 운영으로 테스트 할 경우 지정
public class NoticeSendTest {

    @Autowired
    @Qualifier("noticeSendPagingJob")
    Job noticeSendPagingJob;

    @Autowired
    JobLauncher jobLauncher;

    @Test
    public void mybatisPaging() throws Exception {
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("memberNumber", "52009342941");
        builder.addLong("time", System.currentTimeMillis());    // 파라미터값을 변경해서 계속 실행되도록 처리

        JobExecution jobExecution = jobLauncher.run(noticeSendPagingJob, builder.toJobParameters());

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }
}
