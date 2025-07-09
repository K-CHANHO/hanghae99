package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.PointChargeRequest;
import io.hhplus.tdd.point.dto.PointUseRequest;
import io.hhplus.tdd.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final PointService pointService;

    /**
     * 특정 유저의 포인트를 조회
     */
    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        return pointService.viewPoint(id);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        return pointService.viewPointHistory(id);
    }

    /**
     * 특정 유저의 포인트를 충전
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
//            @RequestBody Long amount
            @RequestBody PointChargeRequest pointChargeRequest
            ) throws Exception {

        if(pointChargeRequest.getAmount() < 0) throw new Exception("포인트는 0원 이하로 충전할 수 없습니다");

        return pointService.chargePoint(id, pointChargeRequest.getAmount());
    }

    /**
     * 특정 유저의 포인트를 사용
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
//            @RequestBody long amount
            @RequestBody PointUseRequest pointUseRequest
    ) throws Exception {

        if(pointUseRequest.getAmount() < 0) throw new Exception("포인트는 0원 이하로 사용할 수 없습니다.");

        return pointService.usePoint(id, pointUseRequest.getAmount());
    }
}
