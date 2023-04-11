package com.example.repository.pass;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PassModelMapperTest {

    @Test
    void test_toPassEntity(){
        // Given
        LocalDateTime now = LocalDateTime.now();
        String userId = "A100000";

        BulkPassEntity bulkPassEntity = new BulkPassEntity();
        bulkPassEntity.setPackageSeq(1);
        bulkPassEntity.setUserGroupId("GROUP");
        bulkPassEntity.setStatus(BulkPassStatus.COMPLETED);
        bulkPassEntity.setCount(10);
        bulkPassEntity.setStartedAt(now.minusDays(60));
        bulkPassEntity.setEndedAt(now);

        // When
        PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);

        // Then
        assertEquals(1, passEntity.getPackageSeq());
        assertEquals(PassStatus.READY, passEntity.getStatus());
        assertEquals(10, passEntity.getRemainingCount());
        assertEquals(now.minusDays(60), passEntity.getStartedAt());
        assertEquals(now, passEntity.getEndedAt());
        assertEquals(userId, passEntity.getUserId());
    }
}