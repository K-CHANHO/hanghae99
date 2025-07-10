package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public UserPoint viewPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    public UserPoint chargePoint(long userId, long chargeAmount) throws Exception {
        ReentrantLock lock = lockMap.computeIfAbsent(userId, reentrant -> new ReentrantLock());

        try {
            lock.lock();

            UserPoint userPoint = userPointTable.selectById(userId);

            long chargedPoint = userPoint.point() + chargeAmount;
            if (chargedPoint > 100000) throw new Exception("포인트는 10만원을 넘길 수 없습니다.");

            saveHistory(userId, chargeAmount, TransactionType.CHARGE, System.currentTimeMillis());

            return userPointTable.insertOrUpdate(userId, chargedPoint);
        } finally {
            lock.unlock();
        }


    }

    public UserPoint usePoint(long userId, long amount) throws Exception {
        ReentrantLock lock = lockMap.computeIfAbsent(userId, reentrant -> new ReentrantLock());

        try {
            lock.lock();

            UserPoint userPoint = userPointTable.selectById(userId);
            long afterPoint = userPoint.point() - amount;
            if (afterPoint < 0) throw new Exception("포인트가 부족합니다.");

            saveHistory(userId, amount, TransactionType.USE, System.currentTimeMillis());

            return userPointTable.insertOrUpdate(userId, afterPoint);
        } finally {
            lock.unlock();
        }
    }

    public PointHistory saveHistory(long userId, long amount, TransactionType type, long updateMillis) {

        PointHistory pointHistory = pointHistoryTable.insert(userId, amount, type, updateMillis);

        return pointHistory;
    }

    public List<PointHistory> viewPointHistory(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
