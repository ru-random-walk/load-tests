FROM openjdk:17-jdk-slim

ENV GATLING_VERSION=3.14.3
ENV GATLING_HOME=/opt/gatling

RUN apt-get update && apt-get install -y unzip curl && \
    curl -sL https://github.com/gatling/gatling/releases/download/${GATLING_VERSION}/gatling-charts-highcharts-bundle-${GATLING_VERSION}-bundle.zip -o gatling.zip && \
    unzip gatling.zip -d /opt && \
    mv /opt/gatling-charts-highcharts-bundle-${GATLING_VERSION} ${GATLING_HOME} && \
    rm gatling.zip

ENV PATH="${GATLING_HOME}/bin:${PATH}"
WORKDIR ${GATLING_HOME}
ENTRYPOINT ["/bin/bash"]
CMD ["gatling.sh", "-s", "simulation.WsStompClientSimulation"]
