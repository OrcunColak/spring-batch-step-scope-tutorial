package com.colak.springtutorial.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

// Spring Batch 4 to 5 migration breaking changes
// See https://levelup.gitconnected.com/spring-batch-4-to-5-migration-breaking-changes-9bac1c063dc5
@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        // We are now required to pass in JobRepository upon using JobBuilder
        return new JobBuilder("job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }


    @Bean
    public Step step(ItemReader<MarketData> reader) {
        // We are now required to pass in JobRepository upon using StepBuilder
        // Tasklet and Chunk processing in the Step bean requires a PlatformTransactionManager.
        return new StepBuilder("step_first", jobRepository)
                .<MarketData, MarketData>chunk(4, transactionManager)
                .reader(reader)
                // Chunk processing now processes Chunk datatype instead of a List
                .writer(chunk -> chunk.forEach(item -> log.info("Market Data: {}", item)))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<MarketData> reader(@Value("#{jobParameters['targetFilePath']}") String targetFilePath) {

        ClassPathResource resource = new ClassPathResource(targetFilePath);

        return new FlatFileItemReaderBuilder<MarketData>()
                .name("marketDataCsvReader") // Reader name
                .resource(resource)         // Resource (CSV file)
                .linesToSkip(1)              // Skip the header line
                .delimited()                 // Enable delimited tokenizer

                // Set names of FieldSet that we can use in MarketDataFieldSetMapper
                .names("TID", "TickerName", "TickerDescription") // Column names
                .targetType(MarketData.class)
                .build();
    }
}