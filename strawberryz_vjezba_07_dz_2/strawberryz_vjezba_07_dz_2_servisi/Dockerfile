FROM eclipse-temurin:25-jre

COPY ./target/strawberryz_vjezba_07_dz_2_servisi-1.0.0-jar-with-dependencies.jar /usr/app/

COPY ./NWTiS_04_servisi.txt /usr/app/

WORKDIR /usr/app

EXPOSE 8080

CMD ["java", "-jar", "strawberryz_vjezba_07_dz_2_servisi-1.0.0-jar-with-dependencies.jar", "NWTiS_04_servisi.txt"]
