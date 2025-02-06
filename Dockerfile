FROM openjdk:17-slim-buster
RUN mkdir -p /opt/payara/config
RUN mkdir -p /opt/payara/deploy
RUN mkdir -p /opt/payara/libs
COPY target/chatbot-payments.jar /opt/payara/deploy
CMD ["--postdeploycommandfile", "/opt/payara/config/postdeploy.txt","--postbootcommandfile","/opt/payara/config/post-boot-commands.asadmin","--noCluster"]