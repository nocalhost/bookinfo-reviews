FROM gradle:6.7.0-jdk8 as builder

COPY . /opt/src
WORKDIR /opt/src

RUN ["gradle", "build"]


FROM java:8-jdk

COPY --from=builder /opt/src/build/libs/reviews-0.0.1-SNAPSHOT.jar /opt/

CMD ["java", "-jar", "/opt/reviews-0.0.1-SNAPSHOT.jar"]
