package com.fivlo.fivlo_backend.config;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final UserRepository userRepository;
    private final EntityManagerFactory entityManagerFactory;

    private static final int CHUNK_SIZE = 100; // 한 번에 처리할 데이터 양 (사용자 수 늘어나면 수정)

    @Bean
    public Job userManagementJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("userManagementJob", jobRepository)
                .start(deactivateUserStep(jobRepository, transactionManager)) // 휴면계정 배치 작업
                .next(hardDeleteUserStep(jobRepository, transactionManager)) // 영구 탈퇴 배치 작업
                .build();
    }

    @Bean
    public Step deactivateUserStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("deactivateUserStep", jobRepository)
                .<User, User>chunk(CHUNK_SIZE, transactionManager)
                .reader(deactivatedUserReader())
                .processor(deactivatedUserProcessor())
                .writer(userUpdateWriter())
                .build();
    }

    @Bean
    @StepScope // step 실행 범위 안에서 Bean이 생성되도록 설정
    public JpaPagingItemReader<User> deactivatedUserReader() {
        return new JpaPagingItemReaderBuilder<User>()
                .name("deactivatedUserReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("select u from User u where u.status = 'DEACTIVATED' and u.deactivatedAt <= :sevenDaysAgo")
                .parameterValues(Map.of("sevenDaysAgo", LocalDateTime.now().minusDays(7)))
                .build();
    }

    @Bean
    public ItemProcessor<User, User> deactivatedUserProcessor() {
        return user -> {
            log.info("Processing user for deactivation to deleted: {}", user.getId());
            user.updateStatus(User.Status.DELETED);
            return user;
        };
    }

    @Bean // 변경된 상태를 전부 DB에 저장
    public ItemWriter<User> userUpdateWriter() {
        return users -> userRepository.saveAll(users);
    }

    @Bean
    public Step hardDeleteUserStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("hardDeleteUserStep", jobRepository)
                .<User, User>chunk(CHUNK_SIZE, transactionManager)
                .reader(deletedUserReader())
                .processor(deletedUserProcessor())
                .writer(userHardDeleteWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<User> deletedUserReader() {
        return new JpaPagingItemReaderBuilder<User>()
                .name("deletedUserReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("select u from User u where u.status = 'DELETED' and u.updatedAt <= :threeMonthsAgo")
                .parameterValues(Map.of("threeMonthsAgo", LocalDateTime.now().minusMonths(3)))
                .build();
    }

    @Bean
    public ItemProcessor<User, User> deletedUserProcessor() {
        return user -> {
            log.info("Processing user for hard delete: {}", user.getEmail());
            return user;
        };
    }

    @Bean
    public ItemWriter<User> userHardDeleteWriter() {
        return chunk -> {
            userRepository.deleteAllInBatch((Iterable<User>) chunk.getItems());
        };
    }
}