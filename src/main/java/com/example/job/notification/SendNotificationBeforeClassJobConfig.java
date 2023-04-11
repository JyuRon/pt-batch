package com.example.job.notification;

import com.example.repository.booking.BookingEntity;
import com.example.repository.notification.NotificationEntity;
import com.example.repository.notification.NotificationEvent;
import com.example.repository.notification.NotificationModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SendNotificationBeforeClassJobConfig {

    private final int CHUNK_SIZE = 10;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final SendNotificationItemWriter sendNotificationItemWriter;

    @Bean
    public Job sendNotificationBeforeClassJob(){
        return this.jobBuilderFactory.get("sendNotificationBeforeClassJob")
                .start(addNotificationStep())
                .start(sendNotificationStep())
                .build();
    }

    @Bean
    public Step addNotificationStep(){
        return stepBuilderFactory.get("addNotificationStep")
                .<BookingEntity, NotificationEntity>chunk(CHUNK_SIZE)
                .reader(addNotificationItemReader())
                .processor(addNotificationItemProcessor())
                .writer(addNotificationItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<BookingEntity> addNotificationItemReader(){
        return new JpaPagingItemReaderBuilder<BookingEntity>()
                .name("addNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("select b from BookingEntity b join fetch b.userEntity where b.status = :status and b.startedAt <= :startedAt order by b.bookingSeq")
                .build();
    }

    @Bean
    public ItemProcessor<BookingEntity, NotificationEntity> addNotificationItemProcessor(){
        return bookingEntity -> NotificationModelMapper.INSTANCE.toNotificationEntity(bookingEntity, NotificationEvent.BEFORE_CLASS);

    }

    @Bean
    public JpaItemWriter<NotificationEntity> addNotificationItemWriter(){
        return new JpaItemWriterBuilder<NotificationEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step sendNotificationStep(){
        return stepBuilderFactory.get("sendNotificationStep")
                .<NotificationEntity, NotificationEntity>chunk(CHUNK_SIZE)
                .reader(sendNotificationItemReader())
                .writer(sendNotificationItemWriter)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();

    }

    // cursor reader 자  페이징 : 데이터 누락 , 커서는 thread safe 하지 않음 이럴 경우 사용
    // 이렇기 때문에 reader 부분은 순차적으로, processor, writer 는 멀티 쓰레드 방식
    // read 보단 write 가 코스트가 많이 들어 이 방식으로 구현
    @Bean
    public SynchronizedItemStreamReader<NotificationEntity> sendNotificationItemReader(){
        JpaCursorItemReader<NotificationEntity> itemReader = new JpaCursorItemReaderBuilder<NotificationEntity>()
                .name("sendNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select n from NotificationEntity n where n.event = :event and n.sent = :sent")
                .parameterValues(Map.of("event",NotificationEvent.BEFORE_CLASS,"sent",false))
                .build();

        return new SynchronizedItemStreamReaderBuilder<NotificationEntity>()
                .delegate(itemReader)
                .build();
    }

}
