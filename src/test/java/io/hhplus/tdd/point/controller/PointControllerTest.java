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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
        String url = "/point/1";
        when(pointService.viewPoint(1)).thenReturn(new UserPoint(1, 2000, System.currentTimeMillis()));

        // when
        ResultActions actions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(2000));
    }
}
