FROM jenkins/jenkins:lts-jdk17

ENV CASC_JENKINS_CONFIG /jenkins_configs/jenkins.yaml
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false

# Install plugins
COPY plugins.txt /usr/share/jenkins/ref/
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt