## Stage 1 : build with maven builder image with native capabilities
FROM maven:3.6.3-openjdk-11 AS build
COPY mvnsettings.xml /settings.xml
COPY pom.xml /pom.xml
RUN mvn -s /settings.xml -f /pom.xml clean package -U -Dmaven.test.skip=true
COPY src /src
RUN mvn -s /settings.xml -f /pom.xml -Pdeb clean package -U -Dmaven.test.skip=true

FROM debian:buster
EXPOSE 9298
WORKDIR /work/
COPY --from=build /target/hsp-nachweis_*_all.deb /nachweis/hsp-nachweis.deb
COPY apt.conf /etc/apt/apt.conf
RUN apt update && apt install -y /nachweis/hsp-nachweis.deb
COPY commands.sh /scripts/commands.sh
RUN ["chmod", "+x", "/scripts/commands.sh"]
ENTRYPOINT ["/scripts/commands.sh"]