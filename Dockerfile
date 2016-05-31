FROM java:8-jre
MAINTAINER WIZE  <analytics-support@wizni.com>
LABEL GITSHA="BUILD_COMMIT_ID"
LABEL BUILD_NUMBER="CI_BUILD_NUMBER"
LABEL BRANCH="BRANCH_NAME"
LABEL Description="Wizni zeppelin docker image" Vendor="Wizni Organization" Version="demo"

COPY zeppelin-0.6.0-incubating-SNAPSHOT /zeppelin-0.6.0-incubating-SNAPSHOT
EXPOSE 8080 8081
WORKDIR /
CMD /zeppelin-0.6.0-incubating-SNAPSHOT/bin/zeppelin.sh stop && /zeppelin-0.6.0-incubating-SNAPSHOT/bin/zeppelin.sh start
