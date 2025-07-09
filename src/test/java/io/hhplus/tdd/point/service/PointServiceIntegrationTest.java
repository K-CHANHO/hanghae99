package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PointServiceIntegrationTest {

    @Autowired
    PointService pointService;

    @Test
    @DisplayName("포인트 조회")
    @Order(1)
    void viewPoint(){
        // given
        long userId = 1;

        // when
        UserPoint userPoint = pointService.viewPoint(userId);

        // then
        assertThat(userPoint).isNotNull();
    }

    @Test
    @DisplayName("포인트 충전")
    @Order(2)
    void chargePoint() throws Exception {
        // given
        long userId = 1;
        long chargeAmount = 20000;

        // when
        UserPoint userPoint = pointService.chargePoint(userId, chargeAmount);

        // then
        assertThat(userPoint.point()).isEqualTo(20000);
    }

    @Test
    @DisplayName("포인트는 총 10만원까지만 충전가능")
    @Order(3)
    void chargePointUntil10만원() throws Exception {
        // given
        long userId = 1;
        long chargeAmount = 90000;

        // when, then
        assertThatThrownBy(() -> pointService.chargePoint(userId, chargeAmount))
                .isInstanceOf(Exception.class)
                .hasMessage("포인트는 10만원을 넘길 수 없습니다.");

    }

    @Test
    @DisplayName("포인트 사용")
    @Order(4)
    void usePoint() throws Exception {
        // given
        long userId = 1;
        long amount = 10000;

        // when
        UserPoint userPoint = pointService.usePoint(userId, amount);

        // then
        assertThat(userPoint.point()).isEqualTo(10000);
        assertThat(pointService.viewPoint(1).point()).isEqualTo(10000);
    }

    @Test
    @DisplayName("보유중인 포인트 내에서만 사용 가능")
    @Order(5)
    void usePointFail(){
        // given
        long userId = 1;
        long amount = 20000;

        // when, then
        assertThatThrownBy(() -> pointService.usePoint(userId, amount))
                .isInstanceOf(Exception.class)
                .hasMessage("포인트가 부족합니다.");
    }

    @Test
    @DisplayName("포인트 충전/사용 내역 조회")
    @Order(6)
    void viewPointChargeAndUseHistory(){
        // given
        long userId = 1;

        // when
        List<PointHistory> pointHistoryList = pointService.viewPointHistory(userId);

        // then
        assertThat(pointHistoryList.isEmpty()).isFalse();
        assertThat(pointHistoryList.size()).isEqualTo(2);
        assertThat(pointHistoryList.get(0).amount()).isEqualTo(20000);
        assertThat(pointHistoryList.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(pointHistoryList.get(1).amount()).isEqualTo(10000);
        assertThat(pointHistoryList.get(1).type()).isEqualTo(TransactionType.USE);

    }

    @Test
    @DisplayName("포인트 충전 동시성 테스트")
    @Order(7)
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
        assertThat(pointService.viewPoint(1).point()).isEqualTo(20000);

    }



}
