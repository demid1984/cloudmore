# Cloudmore
Cloudmore Java Test Task

## Install applications

Run install.sh script for installing consumer and producer
```
./install.sh
```
If the compilation is OK and all tests are passed, installer will create 'producer.sh' and 'consumer.sh' scripts.

## Environment
- MYSQL_HOST - mysql database host address, default localhost
- KAFKA_SERVERS - kafka server address, default localhost
- can add application.yaml to /home/config in docker container

## Run applications
You should use 'producer.sh' and 'consumer.sh' for starting, stopping, watching info and errors
### Commands
- start - start application
- stop - stop application
- info - watch application log

## Additional utilities
- Kafka Web Client - find out IP of 'cloudmore_kafka_web' container, port 9000
- Swagger UI - find out IP of 'cloudmore_producer' container, IP:8080/producer-swagger/swagger-ui.html