package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.ApiControllerAdvice;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointController;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PointControllerIntegrationTest {

    @Autowired
    private PointController pointController;

    @Autowired
    private PointService pointService;

    private MockMvc mockMvc;

    @Autowired
    private UserPointTable userPointTable;


    @BeforeEach
    void setMockMvc(){
        mockMvc = MockMvcBuilders
                .standaloneSetup(pointController)
                .setControllerAdvice(new ApiControllerAdvice())
                .build();
    }

    @BeforeAll
    void dummyData(){
        userPointTable.insertOrUpdate(1, 10000);
        userPointTable.insertOrUpdate(2, 20000);
        userPointTable.insertOrUpdate(3, 30000);
        userPointTable.insertOrUpdate(4, 40000);
        userPointTable.insertOrUpdate(5, 50000);
    }

    /**
     * 포인트 조회
     */
    @Test
    @Order(1)
    void viewPoint() throws Exception {
        // given
        String url = "/point/{id}";

        // when
        ResultActions actions = mockMvc.perform(
                get(url, 1)
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(10000));
    }

    /**
     * 포인트 충전
     * 사용자 당 포인트는 최대 10만원까지만 충전 가능.
     */
    @Test
    @Order(2)
    void chargePoint10만원까지() throws Exception {
        // given
        String url = "/point/{id}/charge";
        String requestBody = "{\"amount\" : \"100000\"}";

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
     * 포인트 충전
     * 마이너스 값으로 충전
     */
    @Test
    @Order(3)
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
    @Order(4)
    void chargePoint() throws Exception {
        // given
        String url = "/point/{id}/charge";
        String requestBody = "{\"amount\" : \"5000\"}";

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
                .andExpect(jsonPath("$.point").value("15000"));

    }

    /**
     * 포인트 사용
     * 0원 밑으로 사용
     */
    @Test
    @Order(5)
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
     * 보유한 포인트보다 많이 사용
     */
    @Test
    @Order(6)
    void usePointMoreThanHave() throws Exception {
        // given
        String url = "/point/{id}/use";
        String requestBody = "{\"amount\" : \"30000\"}";

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
    @Order(7)
    void usePoint() throws Exception {
        // given
        String url = "/point/{id}/use";
        String requestBody = "{\"amount\" : \"5000\"}";

        // when
        ResultActions actions = mockMvc.perform(
                patch(url, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(10000));
    }

    /**
     * 포인트 충전/이용 내역 조회
     */
    @Test
    @Order(8)
    void viewPointHistory() throws Exception {
        // given
        String url = "/point/{id}/histories";

        // when
        ResultActions actions = mockMvc.perform(get(url, 1));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].amount").value(5000))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))

                .andExpect(jsonPath("$[1].amount").value(5000))
                .andExpect(jsonPath("$[1].type").value("USE"))
        ;
    }

}
