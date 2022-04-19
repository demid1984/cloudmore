#!/bin/sh

producer_id=$(docker container ls -a -f "name=cloudmore_producer" --format '{{.ID}}')
if [ -n "$producer_id" ]
then
  echo "Please stop and remove producer before installation. Container id is $producer_id"
  exit
fi

consumer_id=$(docker container ls -a -f "name=cloudmore_consumer" --format '{{.ID}}')
if [ -n "$consumer_id" ]
then
  echo "Please stop and remove consumer before installation. Container id is $consumer_id"
  exit
fi

docker-compose up -d
KAFKA_HOST=$(docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -aqf "name=^cloudmore_kafka$"))
export KAFKA_SERVERS="$KAFKA_HOST:9092"
export MYSQL_HOST=$(docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -aqf "name=^cloudmore_mysql$"))

mvn clean install -Dmaven.test.skip=true -f consumer/pom.xml
if [ $? -ne 0 ]
then
    echo 'Cannot build consumer or test failed'
    exit 1
fi
cp consumer/target/consumer.jar app.jar
docker build --tag="cloudmore/consumer:latest" .
rm app.jar

mvn clean install -Dmaven.test.skip=true -f producer/pom.xml
if [ $? -ne 0 ]
then
    echo 'Cannot build producer or test failed'
    exit 1
fi
cp producer/target/producer.jar app.jar
docker build --tag="cloudmore/producer:latest" .
rm app.jar

#######################################################################################################################
echo '#!/bin/sh' > producer.sh
echo 'case $1 in' >> producer.sh
echo '\tstart)' >> producer.sh
echo "\t\tdocker run -d -e KAFKA_SERVERS=$KAFKA_SERVERS -e MYSQL_HOST=$MYSQL_HOST --network cloudmore --name cloudmore_producer -P cloudmore/producer:latest" >> producer.sh
echo "\t\t;;" >> producer.sh
echo '\tstop)' >> producer.sh
echo '\t\tdocker stop cloudmore_producer' >> producer.sh
echo "\t\t;;" >> producer.sh
echo '\tinfo)' >> producer.sh
echo '\t\tdocker logs -f cloudmore_producer' >> producer.sh
echo "\t\t;;" >> producer.sh
echo '\t*)' >> producer.sh
echo '\t\techo "use start/stop/info commands"' >> producer.sh
echo "\t\t;;" >> producer.sh
echo "esac" >> producer.sh
chmod +x producer.sh
#######################################################################################################################
echo '#!/bin/sh' > consumer.sh
echo 'case $1 in' >> consumer.sh
echo '\tstart)' >> consumer.sh
echo "\t\tdocker run -d -e KAFKA_SERVERS=$KAFKA_SERVERS -e MYSQL_HOST=$MYSQL_HOST --network cloudmore --name cloudmore_consumer -P cloudmore/consumer:latest" >> consumer.sh
echo "\t\t;;" >> consumer.sh
echo '\tstop)' >> consumer.sh
echo '\t\tdocker stop cloudmore_consumer' >> consumer.sh
echo "\t\t;;" >> consumer.sh
echo '\tinfo)' >> consumer.sh
echo '\t\tdocker logs -f cloudmore_consumer' >> consumer.sh
echo "\t\t;;" >> consumer.sh
echo '\t*)' >> consumer.sh
echo '\t\techo "use start/stop/info commands"' >> consumer.sh
echo "\t\t;;" >> consumer.sh
echo "esac" >> consumer.sh
chmod +x consumer.sh
#######################################################################################################################