package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint viewPoint(long userId) {

        return userPointTable.selectById(userId);
    }

    public UserPoint chargePoint(long userId, long chargeAmount) throws Exception {
        UserPoint userPoint = userPointTable.selectById(userId);
        long chargedPoint = userPoint.point() + chargeAmount;
        if(chargedPoint > 100000) throw new Exception("포인트는 10만원을 넘길 수 없습니다.");

        return userPointTable.insertOrUpdate(userId, chargedPoint);
    }

    public UserPoint usePoint(long userId, long amount) throws Exception {

        UserPoint userPoint = userPointTable.selectById(userId);
        long afterPoint = userPoint.point() - amount;
        if(afterPoint < 0) throw new Exception("포인트가 부족합니다.");
        UserPoint usedPoint = userPointTable.insertOrUpdate(userId, afterPoint);

        return usedPoint;
    }

    public PointHistory saveHistory(long userId, long amount, TransactionType type, long updateMillis) {

        PointHistory pointHistory = pointHistoryTable.insert(userId, amount, type, updateMillis);

        return pointHistory;
    }
}
