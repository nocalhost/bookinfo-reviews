FROM gradle:6.7.0-jdk8 as builder

COPY . /opt/src
WORKDIR /opt/src

RUN ["gradle", "build"]
RUN ["ls", "-R"]
FROM tomcat:jdk8-openjdk

COPY --from=builder /opt/src/build/libs/src.war /usr/local/tomcat/webapps/ROOT.war

