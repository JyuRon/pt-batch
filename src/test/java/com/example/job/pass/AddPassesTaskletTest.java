package com.example.job.pass;

import com.example.repository.pass.*;
import com.example.repository.user.UserGroupMappingEntity;
import com.example.repository.user.UserGroupMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AddPassesTaskletTest {

    @InjectMocks
    private AddPassesTasklet addPassesTasklet;
    @Mock private StepContribution stepContribution;
    @Mock private ChunkContext chunkContext;
    @Mock private PassRepository passRepository;
    @Mock private BulkPassRepository bulkPassRepository;
    @Mock private UserGroupMappingRepository userGroupMappingRepository;

    @Test
    void test_execute() throws Exception {
        // Given
        String userGroupId = "GROUP";
        String userId = "A1000000";
        Integer packageSeq = 1;
        Integer count = 10;
        LocalDateTime now = LocalDateTime.now();

        BulkPassEntity bulkPassEntity = new BulkPassEntity();
        bulkPassEntity.setPackageSeq(packageSeq);
        bulkPassEntity.setUserGroupId(userGroupId);
        bulkPassEntity.setStatus(BulkPassStatus.READY);
        bulkPassEntity.setCount(count);
        bulkPassEntity.setStartedAt(now);
        bulkPassEntity.setEndedAt(now.plusDays(60));

        UserGroupMappingEntity userGroupMappingEntity = new UserGroupMappingEntity();
        userGroupMappingEntity.setUserGroupId(userGroupId);
        userGroupMappingEntity.setUserId(userId);

        given(bulkPassRepository.findByStatusAndStartedAtGreaterThan(eq(BulkPassStatus.READY), any()))
                .willReturn(List.of(bulkPassEntity));

        given(userGroupMappingRepository.findByUserGroupId(eq(userGroupId)))
                .willReturn(List.of(userGroupMappingEntity));

        // When
        RepeatStatus repeatStatus = addPassesTasklet.execute(stepContribution, chunkContext);


        // Then
        assertEquals(RepeatStatus.FINISHED, repeatStatus);

        ArgumentCaptor<List> passEntitiesCaptor = ArgumentCaptor.forClass(List.class);
        then(passRepository).should().saveAll(passEntitiesCaptor.capture());
        List<PassEntity> passEntities = passEntitiesCaptor.getValue();

        assertEquals(1, passEntities.size());

        PassEntity passEntity = passEntities.get(0);
        assertEquals(packageSeq, passEntity.getPackageSeq());
        assertEquals(userId, passEntity.getUserId());
        assertEquals(PassStatus.READY, passEntity.getStatus());
        assertEquals(count, passEntity.getRemainingCount());

    }




}