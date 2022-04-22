package com.cloudmore.producer.lock;

public interface LockService {

    Lock tryLock(String name);

}
