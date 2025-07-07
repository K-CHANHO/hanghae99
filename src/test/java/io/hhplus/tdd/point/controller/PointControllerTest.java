package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.PointController;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class PointControllerTest {

    @InjectMocks
    private PointController pointController;

    @Mock
    private PointService pointService;

    private MockMvc mockMvc;

    @BeforeEach
    void setMockMvc(){
        mockMvc = MockMvcBuilders.standaloneSetup(pointController).build();
    }

    /**
     * 포인트 조회
     */
    @Test
    void viewPoint() throws Exception {
        // given
        String url = "/point/{id}";
        when(pointService.viewPoint(1)).thenReturn(new UserPoint(1, 2000, System.currentTimeMillis()));

        // when
        ResultActions actions = mockMvc.perform(
                MockMvcRequestBuilders.get(url, 1)
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(2000));
    }

    /**
     * 포인트 충전
     * 사용자 당 포인트는 최대 10만원까지만 충전 가능.
     */
    @Test
    void chargePoint() throws Exception {
        // given
        String url = "/point/{id}/charge";
        String requestBody = "{\"amount\" : \"110000\"}";
        when(pointService.charge(anyLong(), anyLong())).thenThrow(new Exception("포인트는 10만원을 넘길 수 없습니다."));

        // when
        ResultActions actions = mockMvc.perform(
                MockMvcRequestBuilders.patch(url, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        actions.andExpect(MockMvcResultMatchers.status().is(500))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("에러가 발생했습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("500"));

    }
}
