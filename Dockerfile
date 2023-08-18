FROM payara/server-full:6.2023.8-jdk17


ENV PAYARA_ARGS --debug

COPY target/*.war ${DEPLOY_DIR}

EXPOSE 8080
EXPOSE 4848
EXPOSE 9009




