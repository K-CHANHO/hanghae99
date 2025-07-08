package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;

    public UserPoint viewPoint(long userId) {

        return userPointTable.selectById(userId);
    }

    public UserPoint charge(long userId, long chargeAmount) throws Exception {
        UserPoint userPoint = userPointTable.selectById(userId);
        long chargedPoint = userPoint.point() + chargeAmount;
        if(chargedPoint > 100000) throw new Exception("포인트는 10만원을 넘길 수 없습니다.");

        return userPointTable.insertOrUpdate(userId, chargedPoint);
    }
}
