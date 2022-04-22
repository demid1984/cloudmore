package com.cloudmore.producer.lock;

public interface Lock extends AutoCloseable {

    @Override
    void close();
}
