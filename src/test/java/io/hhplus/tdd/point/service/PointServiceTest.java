package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    UserPointTable userPointTable;

    @Mock
    PointHistoryTable pointHistoryTable;

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
        Mockito.when(pointService.viewPoint(userId)).thenReturn(new UserPoint(0,0,0));

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
        UserPoint userPoint = pointService.chargePoint(userId, chargeAmount);

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
        long chargeAmount = 60000;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 50000, System.currentTimeMillis()));

        // when, then
        assertThatThrownBy(() -> pointService.chargePoint(userId, chargeAmount))
                .isInstanceOf(Exception.class)
                .hasMessage("포인트는 10만원을 넘길 수 없습니다.");

    }

    /**
     * 포인트 사용
     */
    @Test
    void usePoint() throws Exception {
        // given
        long userId = 1;
        long amount = 1000;
        when(userPointTable.selectById(any())).thenReturn(new UserPoint(userId, 2000, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(anyLong(), anyLong())).thenReturn(new UserPoint(userId, 1000, System.currentTimeMillis()));

        // when
        UserPoint userPoint = pointService.usePoint(userId, amount);

        // then
        assertThat(userPoint.point()).isEqualTo(1000);
    }

    /**
     * 포인트 사용
     * 현재 보유중인 포인트 이상으로 사용하면 에러
     */
    @Test
    void usePointFail(){
        // given
        long userId = 1;
        long amount = 5000;
        when(userPointTable.selectById(any())).thenReturn(new UserPoint(userId, 2000, System.currentTimeMillis()));

        // when, then
        assertThatThrownBy(() -> pointService.usePoint(userId, amount))
                .isInstanceOf(Exception.class)
                .hasMessage("포인트가 부족합니다.");
    }

    /**
     * 포인트 충전 시 내역 저장
     */
    @Test
    void saveHistoryWhenCharge(){
        // given
        long userId = 1;
        long amount = 5000;
        when(pointHistoryTable.insert(anyLong(), anyLong(), any(), anyLong())).thenReturn(
                new PointHistory(1, userId, amount, TransactionType.CHARGE, System.currentTimeMillis())
        );

        // when
        PointHistory pointHistory = pointService.saveHistory(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        // then
        assertThat(pointHistory.amount()).isEqualTo(5000);
        assertThat(pointHistory.type()).isEqualTo(TransactionType.CHARGE);
        assertThat(pointHistory.id()).isEqualTo(1);

    }

    /**
     * 포인트 사용 시 내역 저장
     */
    @Test
    void saveHistoryWhenUse(){
        // given
        long userId = 1;
        long amount = 5000;
        when(pointHistoryTable.insert(anyLong(), anyLong(), any(), anyLong())).thenReturn(
                new PointHistory(1, userId, amount, TransactionType.USE, System.currentTimeMillis())
        );

        // when
        PointHistory pointHistory = pointService.saveHistory(userId, amount, TransactionType.USE, System.currentTimeMillis());

        // then
        assertThat(pointHistory.amount()).isEqualTo(5000);
        assertThat(pointHistory.type()).isEqualTo(TransactionType.USE);
        assertThat(pointHistory.id()).isEqualTo(1);

    }

    /**
     *  포인트 충전/이용 내역 조회
     */
    /*
    @Test
    void viewPointChargeAndUseHistory(){
        // given
        long userId = 1;
        when()

        // when
        List<PointHistory> pointHistoryList = pointService.viewPointHistory(userId);

        // then

    }
    */

}
