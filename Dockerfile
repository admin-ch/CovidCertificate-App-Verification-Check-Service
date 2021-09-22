FROM adoptopenjdk/openjdk11

LABEL maintainer="Lasse Meinen <meinen@ubique.ch>"

ARG VERSION=v2.6.0-prerelease

# Install ws
RUN useradd verification-ws

WORKDIR /home/verification-ws/

# Copy configs
COPY ./ch-covidcertificate-backend-verification-check/ch-covidcertificate-backend-verification-check-ws/simple-config /home/verification-ws/conf

# Create skeleton
RUN mkdir -p /home/verification-ws/bin && \
    mkdir -p /home/verification-ws/archive && \
    mkdir -p /home/verification-ws/log && \
    mkdir -p /home/verification-ws/tmp

# Copy binary
ADD https://github.com/admin-ch/CovidCertificate-App-Verification-Check-Service/releases/download/${VERSION}/ch-covidcertificate-backend-verification-check-ws.jar /home/verification-ws/bin/ch-covidcertificate-backend-verification-check-ws.jar

RUN chown -R verification-ws:verification-ws /home/verification-ws

# Access to webinterface
EXPOSE 8080

CMD java -jar $JAVA_OPTS -Dlogging.config=/home/verification-ws/conf/simple-verification-ws-logback.xml -Dspring.config.location=/home/verification-ws/conf/simple-verification-ws.properties /home/verification-ws/bin/ch-covidcertificate-backend-verification-check-ws.jar
