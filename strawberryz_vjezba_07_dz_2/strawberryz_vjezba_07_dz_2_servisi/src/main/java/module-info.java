module strawberryz_vjezba_07_dz_2_servisi {
  requires jakarta.ws.rs;

  requires org.glassfish.grizzly.http.server;

  requires org.glassfish.jersey.core.server;
  requires org.glassfish.jersey.container.grizzly2.http;

  requires transitive strawberryz_vjezba_07_dz_2_konfiguracije;
  requires transitive strawberryz_vjezba_07_dz_2_podaci;
  requires transitive strawberryz_vjezba_07_dz_2_dao;
  requires java.sql;
  requires org.glassfish.jersey.media.json.jackson;

  exports edu.unizg.foi.nwtis.strawberryz.vjezba_07_dz_2;
}
