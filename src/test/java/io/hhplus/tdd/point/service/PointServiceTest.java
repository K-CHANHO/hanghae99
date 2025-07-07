package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    UserPointTable userPointTable;

    @InjectMocks
    PointService pointService;

    /**
     * 포인트 조회
     */
    @Test
    void viewPoint(){
        // given
        long userId = 1;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(0,0,0));

        // when
        UserPoint userPoint = pointService.viewPoint(userId);

        // then
        assertThat(userPoint).isNotNull();
    }

    /**
     * 포인트 충전
     */
    @Test
    void chargePoint() throws Exception {
        // given
        long userId = 1;
        long chargeAmount = 20000;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 5000, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(new UserPoint(userId, 25000, System.currentTimeMillis()));

        // when
        UserPoint userPoint = pointService.charge(userId, chargeAmount);

        // then
        assertThat(userPoint.point()).isEqualTo(25000);
    }

    /**
     * 포인트 충전
     * 포인트는 10만원까지만 충전 가능
     */
    @Test
    void chargePointUntil10만원() throws Exception {
        // given
        long userId = 1;
        long chargeAmount = 200000;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, chargeAmount, System.currentTimeMillis()));

        // when, then
        assertThatThrownBy(() -> pointService.charge(userId, chargeAmount))
                .isInstanceOf(Exception.class)
                .hasMessage("포인트는 10만원을 넘길 수 없습니다.");
    }

}
