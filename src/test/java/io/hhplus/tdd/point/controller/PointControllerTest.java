package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.ApiControllerAdvice;
import io.hhplus.tdd.point.PointController;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
        mockMvc = MockMvcBuilders
                .standaloneSetup(pointController)
                .setControllerAdvice(new ApiControllerAdvice())
                .build();
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
                get(url, 1)
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
    void chargePoint10만원까지() throws Exception {
        // given
        String url = "/point/{id}/charge";
        String requestBody = "{\"amount\" : \"110000\"}";
        when(pointService.chargePoint(anyLong(), anyLong())).thenThrow(new Exception("포인트는 10만원을 넘길 수 없습니다."));

        // when
        ResultActions actions =
        mockMvc.perform(
                        patch(url, 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                );

        // then
        actions.andExpect(status().is(500))
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(Exception.class))
                .andExpect(jsonPath("$.message").value("에러가 발생했습니다."))
                .andExpect(jsonPath("$.code").value("500"));

    }

    /**
     * 포인트 충전
     * 마이너스 값으로 충전
     */
    @Test
    void chargeMinusPoint() throws Exception {
        // given
        String url = "/point/{id}/charge";
        String requestBody = "{\"amount\" : \"-5000\"}";

        // when
        ResultActions actions =
                mockMvc.perform(
                        patch(url, 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                );

        // then
        actions.andExpect(status().is(500))
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(Exception.class))
                .andExpect(jsonPath("$.message").value("에러가 발생했습니다."))
                .andExpect(jsonPath("$.code").value("500"));

    }

    /**
     * 포인트 충전
     */
    @Test
    void chargePoint() throws Exception {
        // given
        String url = "/point/{id}/charge";
        String requestBody = "{\"amount\" : \"5000\"}";
        when(pointService.chargePoint(anyLong(), anyLong())).thenReturn(new UserPoint(1, 50000, System.currentTimeMillis()));

        // when
        ResultActions actions =
                mockMvc.perform(
                        patch(url, 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value("50000"));

    }

    /**
     * 포인트 사용
     * 0원 밑으로 사용
     */
    @Test
    void usePointUnderZero() throws Exception {
        // given
        String url = "/point/{id}/use";
        String requestBody = "{\"amount\" : \"-5000\"}";

        // when
        ResultActions actions = mockMvc.perform(
                patch(url, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        actions.andExpect(status().is(500))
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(Exception.class))
                .andExpect(jsonPath("$.message").value("에러가 발생했습니다."))
                .andExpect(jsonPath("$.code").value("500"));
    }

    /**
     * 포인트 사용
     */
    @Test
    void usePoint() throws Exception {
        // given
        String url = "/point/{id}/use";
        String requestBody = "{\"amount\" : \"5000\"}";
        when(pointService.usePoint(anyLong(), anyLong())).thenReturn(new UserPoint(1, 1000, System.currentTimeMillis()));

        // when
        ResultActions actions = mockMvc.perform(
                patch(url, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(1000));
    }

    /**
     * 포인트 충전/이용 내역 조회
     */
    @Test
    void viewPointHistory() throws Exception {
        // given
        String url = "/point/{id}/histories";
        List<PointHistory> expectedList = Arrays.asList(
                new PointHistory(1, 1, 1000, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2, 1, 2000, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(3, 1, 2000, TransactionType.USE, System.currentTimeMillis()),
                new PointHistory(4, 1, 10000, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(5, 1, 5000, TransactionType.USE, System.currentTimeMillis())
        );
        when(pointService.viewPointHistory(anyLong())).thenReturn(expectedList);

        // when
        ResultActions actions = mockMvc.perform(get(url, 1));
        MvcResult mvcResult = actions.andReturn();

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(5))
                .andExpect(jsonPath("$[0].amount").value(1000))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))

                .andExpect(jsonPath("$[1].amount").value(2000))
                .andExpect(jsonPath("$[1].type").value("CHARGE"))

                .andExpect(jsonPath("$[2].amount").value(2000))
                .andExpect(jsonPath("$[2].type").value("USE"))

                .andExpect(jsonPath("$[3].amount").value(10000))
                .andExpect(jsonPath("$[3].type").value("CHARGE"))

                .andExpect(jsonPath("$[4].amount").value(5000))
                .andExpect(jsonPath("$[4].type").value("USE"));


    }

}
