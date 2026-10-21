ARG SOURCE_IMAGE_TAG
FROM artefakt.dev.sbb.berlin:5000/sbb/base-images/java21-base:$SOURCE_IMAGE_TAG

# avoid debconf and initrd
ENV DEBIAN_FRONTEND=noninteractive

COPY --chown=925:925 hsp-nachweis/target/quarkus-app/lib/ /app/lib/
COPY --chown=925:925 --chmod=744 hsp-nachweis/target/quarkus-app/*.jar /app/
COPY --chown=925:925 hsp-nachweis/target/quarkus-app/app/ /app/app/
COPY --chown=925:925 hsp-nachweis/target/quarkus-app/quarkus/ /app/quarkus/

WORKDIR /app

USER 925:925
CMD ["/app/quarkus-run.jar"]