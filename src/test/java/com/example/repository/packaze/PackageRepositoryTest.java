package com.example.repository.packaze;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class PackageRepositoryTest {

    private final PackageRepository packageRepository;

    public PackageRepositoryTest(
            @Autowired PackageRepository packageRepository
    ) {
        this.packageRepository = packageRepository;
    }

    @DisplayName("패키지 테이블에 insert 작업을 진행한다.")
    @Test
    void test_save(){
        // Given
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("바디 챌린지 PT 12주");
        packageEntity.setPeriod(84);

        // When
        packageRepository.save(packageEntity);

        // Then
        assertThat(packageEntity.getPackageSeq()).isNotNull();

    }

    @DisplayName("패키지 테이블에 2개의 정보를 입력 후 특정 시간 이후의 데이터를 가져온다.")
    @Test
    void test_findByCreateAtAfter(){
        // Given
        LocalDateTime dateTime = LocalDateTime.now().minusMinutes(1);

        PackageEntity packageEntity0 = new PackageEntity();
        packageEntity0.setPackageName("학생 전용 3개월");
        packageEntity0.setPeriod(90);
        packageRepository.save(packageEntity0);

        PackageEntity packageEntity1 = new PackageEntity();
        packageEntity1.setPackageName("학생 전용 6개월");
        packageEntity1.setPeriod(180);
        packageRepository.save(packageEntity1);

        // When
        List<PackageEntity> packageEntityList = packageRepository.findByCreatedAtAfter(dateTime,
                PageRequest.of(0, 1, Sort.by("packageSeq").descending())
        );

        // Then
        assertThat(packageEntityList).hasSize(1);
        assertThat(packageEntity1.getPackageSeq()).isEqualTo(packageEntityList.get(0).getPackageSeq());
    }

    @DisplayName("패키지 횟수와 기간을 수정한다.")
    @Test
    void test_updateCountAndPeriod(){
        // Given
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("바디프로필 이벤트 4개월");
        packageEntity.setPeriod(90);
        packageRepository.save(packageEntity);

        // When
        int updateCount = packageRepository.updateCountAndPeriod(packageEntity.getPackageSeq(), 30, 120);
        PackageEntity updatedPackageEntity = packageRepository.findById(packageEntity.getPackageSeq()).get();

        // Then
        assertThat(updateCount).isEqualTo(1);
        assertThat(updatedPackageEntity.getCount()).isEqualTo(30);
        assertThat(updatedPackageEntity.getPeriod()).isEqualTo(120);
    }

    @DisplayName("패키지를 삭제한다.")
    @Test
    void test_delete(){
        // Given
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("제거할 이용권");
        packageEntity.setCount(1);
        PackageEntity newPackageEntity = packageRepository.save(packageEntity);

        // When
        packageRepository.deleteById(newPackageEntity.getPackageSeq());

        // Then
        assertThat(packageRepository.findById(newPackageEntity.getPackageSeq())).isEmpty();
    }
}