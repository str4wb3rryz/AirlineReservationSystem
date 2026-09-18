FROM eclipse-temurin:25-jre

COPY ./target/strawberryz_vjezba_08_dz_3_servisi-1.1.0-jar-with-dependencies.jar /usr/app/

COPY ./NWTiS_04_servisi.txt /usr/app/

WORKDIR /usr/app

EXPOSE 8080

CMD ["java", "-jar", "strawberryz_vjezba_08_dz_3_servisi-1.1.0-jar-with-dependencies.jar", "NWTiS_04_servisi.txt"]
