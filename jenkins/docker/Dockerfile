# Jenkins controller with Docker CLI (talks to host engine via mounted /var/run/docker.sock)
FROM jenkins/jenkins:lts-jdk17

USER root
RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends docker.io \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

USER jenkins
