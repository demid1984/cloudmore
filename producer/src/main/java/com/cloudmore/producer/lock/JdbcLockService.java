package com.cloudmore.producer.lock;

import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JdbcLockService implements LockService {

    private final LockRegistry jdbcLockRegistry;

    @Override
    public Lock tryLock(String name) {
        try {
            var lock = jdbcLockRegistry.obtain(name);
            if (lock.tryLock()) {
                return lock::unlock;
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            return null;
        }
    }
}
