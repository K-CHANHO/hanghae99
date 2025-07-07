package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
        Mockito.when(userPointTable.selectById(userId)).thenReturn(new UserPoint(0,0,0));

        // when
        UserPoint userPoint = pointService.viewPoint(userId);

        // then
        Assertions.assertThat(userPoint).isNotNull();
    }
}
