package com.example.job.pass;

import com.example.repository.pass.PassEntity;
import com.example.repository.pass.PassStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ExpirePassesJobConfig {

    private final int CHUNK_SIZE = 5;

    // @EnableBatchProcessing 로 인해 Bean 으로 제공됨
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job expirePassesJob(){
        return jobBuilderFactory.get("expirePassesJob")
                .incrementer(new RunIdIncrementer())
                .start(expirePassesStep())
                .build()
                ;
    }

    @Bean
    public Step expirePassesStep(){
        return stepBuilderFactory.get("expirePassesStep")
                .<PassEntity,PassEntity>chunk(CHUNK_SIZE)
                .reader(expirePassesItemReader())
                .processor(expirePassesItemProcessor())
                .writer(expirePassesItemWriter())
                .build()
                ;

    }

    // 페이징이 아닌 커서 사용의 이유는 성능 문제도 존재하지만 무결성 유지를 위함
    @Bean
    @StepScope
    public JpaCursorItemReader<PassEntity> expirePassesItemReader(){
        return new JpaCursorItemReaderBuilder<PassEntity>()
                .name("expirePassesItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select p from PassEntity p where p.status = :status and p.endedAt <= :endedAt")
                .parameterValues(Map.of("status", PassStatus.IN_PROGRESS, "endedAt", LocalDateTime.now()))
                .build()
                ;
    }

    @Bean
    @StepScope
    public ItemProcessor<PassEntity, PassEntity> expirePassesItemProcessor(){
        return passEntity -> {
            passEntity.setStatus(PassStatus.EXPIRED);
            passEntity.setExpiredAt(LocalDateTime.now());
            return passEntity;
        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<PassEntity> expirePassesItemWriter(){
        return new JpaItemWriterBuilder<PassEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build()
                ;
    }

}
