package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.rest;

import edu.unizg.foi.nwtis.Korisnik;
import edu.unizg.foi.nwtis.Ulaznica;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "mikroservisi.kontrola")
@Produces(MediaType.APPLICATION_JSON)
public interface KontrolaResource {

  @HEAD
  @Path("ping")
  Response ping();

  @HEAD
  @Path("pauza/{milisek}")
  Response pauza(@PathParam("milisek") String milisek);

  @POST
  @Path("korisnik/dodaj/{korisnik}")
  @Consumes(MediaType.APPLICATION_JSON)
  Response korisnikDodaj(@PathParam("korisnik") String korisnik);

  @GET
  @Path("korisnik/provjeri/{korisnik}")
  Response korisnikProvjeri(@PathParam("korisnik") String korisnik);

  @POST
  @Path("ulaznica/dodaj/{korisnik}")
  Response ulaznicaDodaj(@PathParam("korisnik") String korisnik);

  @GET
  @Path("ulaznica/provjeri/{id}")
  Response ulaznicaProvjeri(@PathParam("id") String id);

  @GET
  @Path("ulaznica/korisnik/{id}")
  Response ulaznicaKorisnik(@PathParam("id") String id);

  @PUT
  @Path("ulaznica/ponisti/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  Response ulaznicaPonisti(@PathParam("id") String id);

  @HEAD
  @Path("admin/ping")
  Response adminPing();

  @HEAD
  @Path("admin/kraj")
  Response adminKraj();

  @PUT
  @Path("admin/ulaznica")
  @Consumes(MediaType.APPLICATION_JSON)
  Response adminUlaznicaPonisti();

  @POST
  @Path("admin/registriraj")
  @Consumes(MediaType.APPLICATION_JSON)
  Response adminRegistriraj(
      @HeaderParam("adresa") String adresa,
      @HeaderParam("mreznaVrata") String mreznaVrata,
      @HeaderParam("mreznaVrataAdministracija") String mreznaVrataAdministracija,
      @HeaderParam("kodZaKraj") String kodZaKraj);

  @POST
  @Path("admin/zabrana")
  Response adminZabrana(@HeaderParam("adresa") String adresa);

  @DELETE
  @Path("admin/dozvola")
  Response adminDozvola(@HeaderParam("adresa") String adresa);
}
