FROM ubuntu:latest

COPY ./target/maven-jlink/default/ /usr/app/

WORKDIR /usr/app/podaci

EXPOSE 8000 8001

CMD ["../bin/java", "-m", "strawberryz_vjezba_08_dz_3_kontrola/edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.PosluziteljKontrola", "NWTiS_04_kontrola.txt"]
