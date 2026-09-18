FROM payara/micro:7.2026.4-jdk25
COPY target/strawberryz_vjezba_08_dz_3_mikroservisi-1.1.0.war $DEPLOY_DIR/app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/payara/payara-micro.jar", "--deploy", "/opt/payara/deployments/app.war", "--contextroot", ""]
