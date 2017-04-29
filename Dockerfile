FROM azul/zulu-openjdk:8

ENV DOCKER true

COPY FredBoat/target/FredBoat.jar /FredBoat.jar
COPY FredBoat-Bootloader/target/FredBoat-Bootloader.jar /FredBoat-Bootloader.jar
COPY FredBoat/config.yaml /config.yaml
COPY FredBoat/credentials.yaml.example /credentials.yaml
COPY FredBoat-Bootloader/bootloader.json /bootloader.json

ENTRYPOINT java -jar FredBoat-Bootloader.jar