FROM java:8-jre
COPY zeppelin-0.6.0-incubating-SNAPSHOT /zeppelin-0.6.0-incubating-SNAPSHOT
EXPOSE 8080 8081
WORKDIR /
CMD /zeppelin-0.6.0-incubating-SNAPSHOT/bin/zeppelin.sh stop && /zeppelin-0.6.0-incubating-SNAPSHOT/bin/zeppelin.sh start