# ----------------------------------
# Pterodactyl Core Dockerfile
# Environment: Java
# Minimum Panel Version: 0.6.0
# ----------------------------------
FROM openjdk:8

MAINTAINER Pterodactyl Software, <support@pterodactyl.io>

USER container
ENV  USER=container HOME=/home/container

WORKDIR /home/container