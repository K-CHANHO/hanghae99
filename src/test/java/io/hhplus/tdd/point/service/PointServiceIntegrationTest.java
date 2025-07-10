package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
public class PointServiceIntegrationTest {

    //@Autowired
    PointService pointService;

    @BeforeEach
    void initData() throws Exception {
        UserPointTable userPointTable = new UserPointTable();
        PointHistoryTable pointHistoryTable = new PointHistoryTable();
        pointService = new PointService(userPointTable, pointHistoryTable);

        pointService.chargePoint(1,80000);
        pointService.usePoint(1, 10000);
    }

    @Test
    @DisplayName("포인트 조회")
    void viewPoint(){
        // given
        long userId = 1;

        // when
        UserPoint userPoint = pointService.viewPoint(userId);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isEqualTo(70000);
    }

    @Test
    @DisplayName("포인트 충전")
    void chargePoint() throws Exception {
        // given
        long userId = 1;
        long chargeAmount = 10000;

        // when
        UserPoint userPoint = pointService.chargePoint(userId, chargeAmount);

        // then
        assertThat(userPoint.point()).isEqualTo(80000);
    }

    @Test
    @DisplayName("포인트는 총 10만원까지만 충전가능")
    void chargePointUntil10만원() throws Exception {
        // given
        long userId = 1;
        long chargeAmount = 50000;

        // when, then
        assertThatThrownBy(() -> pointService.chargePoint(userId, chargeAmount))
                .isInstanceOf(Exception.class)
                .hasMessage("포인트는 10만원을 넘길 수 없습니다.");

    }

    @Test
    @DisplayName("포인트 사용")
    void usePoint() throws Exception {
        // given
        long userId = 1;
        long amount = 10000;

        // when
        UserPoint userPoint = pointService.usePoint(userId, amount);

        // then
        assertThat(userPoint.point()).isEqualTo(60000);
        assertThat(pointService.viewPoint(1).point()).isEqualTo(60000);
    }

    @Test
    @DisplayName("보유중인 포인트 내에서만 사용 가능")
    void usePointFail(){
        // given
        long userId = 1;
        long amount = 90000;

        // when, then
        assertThatThrownBy(() -> pointService.usePoint(userId, amount))
                .isInstanceOf(Exception.class)
                .hasMessage("포인트가 부족합니다.");
    }

    @Test
    @DisplayName("포인트 충전/사용 내역 조회")
    void viewPointChargeAndUseHistory(){
        // given
        long userId = 1;

        // when
        List<PointHistory> pointHistoryList = pointService.viewPointHistory(userId);

        // then
        assertThat(pointHistoryList.isEmpty()).isFalse();
        assertThat(pointHistoryList.size()).isEqualTo(2);
        assertThat(pointHistoryList.get(0).amount()).isEqualTo(80000);
        assertThat(pointHistoryList.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(pointHistoryList.get(1).amount()).isEqualTo(10000);
        assertThat(pointHistoryList.get(1).type()).isEqualTo(TransactionType.USE);

    }

    @Test
    @DisplayName("포인트 충전 동시성 테스트")
    void chargePointConcurrentTest() throws InterruptedException {
        // given
        // 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        int executeCount = 100;
        CountDownLatch countDownLatch = new CountDownLatch(executeCount);

        // when
        for(int i=0; i<executeCount; i++){
            executorService.submit(() -> {
                try {
                    UserPoint userPoint = pointService.chargePoint(1, 100);
                    countDownLatch.countDown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // then
        countDownLatch.await();
        assertThat(pointService.viewPoint(1).point()).isEqualTo(80000);

    }

    @Test
    @DisplayName("포인트 사용 동시성 테스트")
    void usePointConcurrentTest() throws InterruptedException {
        // given
        // 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        int executeCount = 100;
        CountDownLatch countDownLatch = new CountDownLatch(executeCount);

        // when
        for(int i=0; i<executeCount; i++){
            executorService.submit(() -> {
                try {
                    UserPoint userPoint = pointService.usePoint(1, 100);
                    countDownLatch.countDown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // then
        countDownLatch.await();
        assertThat(pointService.viewPoint(1).point()).isEqualTo(60000);

    }



}
