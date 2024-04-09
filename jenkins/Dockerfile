FROM jenkins/jenkins:2.452-jdk21

ENV CASC_JENKINS_CONFIG /jenkins_configs/jenkins.yaml
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false

# Install plugins
COPY plugins.txt /usr/share/jenkins/ref/
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt