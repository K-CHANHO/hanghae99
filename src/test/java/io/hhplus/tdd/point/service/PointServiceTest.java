package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.UserPoint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    PointService pointService;

    @Test
    void viewPoint(){
        // given
        long userId = 1;
        Mockito.when(pointService.viewPoint(userId)).thenReturn(new UserPoint(0,0,0));

        // when
        UserPoint userPoint = pointService.viewPoint(userId);

        // then
        Assertions.assertThat(userPoint).isNotNull();
    }
}
