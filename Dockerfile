FROM payara/server-full:6.2024.3-jdk21


ENV PAYARA_ARGS --debug

ARG OPEN_API_KEY

ENV OPEN_API_KEY=${OPEN_API_KEY}

COPY target/*.war ${DEPLOY_DIR}

EXPOSE 8080
EXPOSE 4848
EXPOSE 9009




