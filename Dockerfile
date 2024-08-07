# ----------------------------------
# Pterodactyl Core Dockerfile
# Environment: Java
# Minimum Panel Version: 0.6.0
# ----------------------------------
FROM openjdk:8

MAINTAINER Danil ZeyDie Zalov, <zeydie.dev@gmail.com>

USER container
ENV  USER=container HOME=/home/container

RUN apt-get install lib32z1
RUN apt-get install glibc.i686

WORKDIR /home/container

CMD [ "chmod +X libraries/launch4j"]