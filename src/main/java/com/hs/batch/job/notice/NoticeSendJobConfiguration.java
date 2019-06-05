package com.hs.batch.job.notice;

import com.hs.batch.dto.notice.NoticeSend;
import com.hs.batch.service.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mybatis batch 처리 예제
 *
 * reader, processor, writer 로 구성
 * processor 는 없어도 됨
 *
 * mybatis paging 처리는
 * MyBatisPagingItemReader, MyBatisCursorItemReader
 * 두가지가 존재하나 cursor 방식은 jtds 에서 지원하지 않음
 * 계속 연결상태에서 처리하는 경우라 추천하지 않음
 *
 * MyBatisPagingItemReader 를 사용할때는
 * 아래 파라미터를 이용해 쿼리상에 페이지 처리를 구현해야 됨
 *
 * #{_page} 현재 페이지 (0부터 시작)
 * #{_pagesize} reader 의 pageSize 값
 * #{_skiprows} _page * _pagesize 값
 *
 *
 * 아래쪽에 기존방식인 tasklet 방식 예제
 * chunk 방식 사용 안함 (reader, writer 없음)
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class NoticeSendJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private SqlSessionFactory sqlSessionFactory;
    private NoticeService noticeService;

    @Autowired
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Autowired
    public void setNoticeService(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Bean
    public Job noticeSendPagingJob() {
        return jobBuilderFactory.get("noticeSendPagingJob")
                .start(noticeSendPagingStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    @JobScope
    public Step noticeSendPagingStep() {
        // transaction 단위 (500건 마다 commit)
        int chunkSize = 500;

        /*
        paging 처리에서 transaction 이 있으면 아래오류 발생
        SELECT INTO command not allowed within multi-statement transaction.
        chunk 단위로 트랜잭션이 묶여 있어서 발생
        (chunk 단위로 reader - processor - writer 가 한 묶음의 트랜잭션으로 물려있음)
        때문에 propagation 을 not_supported 로 설정

        ※ 중요
        transactionAttribute 설정시 method chain 의 최하단으로 지정필요
        반환값이 AbstractTaskletStepBuilder 로 반환됨
         */

        DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
        attribute.setPropagationBehavior(TransactionAttribute.PROPAGATION_NOT_SUPPORTED);

        return stepBuilderFactory.get("mybatisPagingStep")
                .<NoticeSend, NoticeSend>chunk(chunkSize)
                .reader(findNoticeSend(null))
                .processor(noticeSendProcess()) // 처리 내용 없을 경우 없어도 됨
                .writer(createNoticeSend())
                .transactionAttribute(attribute)
                .build();
    }

    /**
     * reader
     *
     * query xml 의 id 값으로 조회 처리
     *
     * @param memberNumber 회원번호
     * @return Reader
     */
    @Bean
    @StepScope
    public MyBatisPagingItemReader<NoticeSend> findNoticeSend(
            @Value("#{jobParameters['memberNumber']}") String memberNumber) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("memberNumber", memberNumber);

        return new MyBatisPagingItemReaderBuilder<NoticeSend>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.hs.batch.dao.notice.NoticeMapper.findNoticeSendListByMemberNumber")
                .parameterValues(parameterValues)
                .pageSize(100)
                .build();
    }

    /**
     * process
     *
     * 데이터 값 가공 처리
     * generic 은 입력, 출력값
     * 처리코드가 길어지면 별도 클래스로 빼는걸 추천
     *
     * @return ItemProcessor
     */
    @Bean
    @StepScope
    public ItemProcessor<NoticeSend, NoticeSend> noticeSendProcess() {
        return item -> {
            /*
            processor 에서 별도 조회 후 처리 예 (아래는 별도 처리 없이 조회만 함)

            MyBatisPagingItemReader 을 사용할 경우
            default-executor-type 은 기본으로 batch 로 설정해서 조회됨

            reader 안에서 별도 쿼리를 실행할 경우 executor type 이 동일해야 됨 (같은 transaction 에서 실행)
            그래서 application.yml 의 default-executor-type 도 동일하게 batch 로 설정

            다를 경우 아래 오류가 발생한다.
            TransientDataAccessResourceException: Cannot change the ExecutorType when there is an existing transaction
             */
            List<NoticeSend> result = noticeService.findNoticeSend(item.getMemberNumber());
            System.out.println(result);

            System.out.println(item);
            return item;
        };
    }

    /**
     * writer
     *
     * @return ItemWriter
     */
    @Bean
    @StepScope
    public MyBatisBatchItemWriter<NoticeSend> createNoticeSend() {
        return new MyBatisBatchItemWriterBuilder<NoticeSend>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId("com.hs.batch.dao.notice.NoticeMapper.createNoticeSend")
                .build();
    }

    /**
     * tasklet 방식의 job 설정
     *
     * @return
     */
    @Bean
    public Job noticeSendTaskletJob() {
        return jobBuilderFactory.get("noticeSendTaskletJob")
                .start(noticeSendTaskletStep())
                .build();
    }

    /**
     * tasklet 방식 step 설정
     * @return
     */
    @Bean
    @JobScope
    public Step noticeSendTaskletStep() {
        return stepBuilderFactory.get("noticeSendTaskletStep")
                .tasklet(noticeSendTasklet())
                .build();
    }

    /**
     * tasklet 설정
     *
     * 직접 select 후 insert 처리
     *
     * @return
     */
    @Bean
    @StepScope
    public Tasklet noticeSendTasklet() {
        return (contribution, chunkContext) -> {
            String memberNumber = (String) chunkContext.getStepContext().getJobParameters().get("memberNumber");
            List<NoticeSend> noticeSend = noticeService.findNoticeSend(memberNumber);

            noticeSend.forEach(item -> noticeService.createNoticeSend(item));

            return RepeatStatus.FINISHED;
        };
    }
}