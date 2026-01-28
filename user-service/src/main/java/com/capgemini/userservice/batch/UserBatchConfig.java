package com.capgemini.userservice.batch;

import com.capgemini.userservice.entity.User;
import com.capgemini.userservice.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
//@EnableBatchProcessing
public class UserBatchConfig {

    @Bean
    public FlatFileItemReader<UserCSV> reader() {
        FlatFileItemReader<UserCSV> reader = new FlatFileItemReader<>();
        System.out.println("**** READER ****");
        reader.setResource(new ClassPathResource("users.csv"));
        reader.setLinesToSkip(1); // Skip header
        reader.setLineMapper(new DefaultLineMapper<>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("username", "email", "canInitiateSession");
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(UserCSV.class);
            }});
        }});
        return reader;
    }

    @Bean
    public ItemProcessor<UserCSV, User> processor() {
        System.out.println("**** PROCESSOR ****");
        return userCSV -> {
            User user = new User();
            user.setUsername(userCSV.getUsername());
            user.setEmail(userCSV.getEmail());
            user.setCanInitiateSession(userCSV.isCanInitiateSession());
            return user;
        };
    }

    @Bean
    public ItemWriter<User> writer(UserRepository userRepository) {
        System.out.println("**** WRITER ****");
        return users -> {
            for (User user : users) {
                // Check if user already exists to avoid duplicates
                if (userRepository.findByUsername(user.getUsername()).isEmpty() &&
                        userRepository.findByEmail(user.getEmail()).isEmpty()) {
                    userRepository.save(user);
                }
            }
        };
    }

    @Bean
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      ItemReader<UserCSV> reader,
                      ItemProcessor<UserCSV, User> processor,
                      ItemWriter<User> writer) {
        System.out.println("**** STEP ****");
        return new StepBuilder("csvImportStep", jobRepository)
                .<UserCSV, User>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository, Step step1) {
        System.out.println("**** USER JOB ****");
        return new JobBuilder("importUserJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .build();
    }
}