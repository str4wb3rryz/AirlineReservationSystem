package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.mikroservisi.klijenti;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import edu.unizg.foi.nwtis.Grupa;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "servisi.grupe")
public interface GrupeRestKlijent {

  @Path("test")
  @GET
  @Consumes({MediaType.TEXT_PLAIN})
  @Produces({MediaType.TEXT_PLAIN})
  public Response test();

  @GET
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajSveGrupe();

  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dodajGrupu(Grupa grupa);

  @Path("{gid}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajGrupu(@PathParam("gid") String gid);

  @Path("{gid}")
  @PUT
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response azurirajGrupu(@PathParam("gid") String gid, Grupa grupa);

  @Path("{gid}")
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiGrupu(@PathParam("gid") String gid);

  @Path("{gid}/korisnici")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajKorisnikeGrupe(@PathParam("gid") String gid);

  @Path("{gid}/korisnik/{id}")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  public Response dodajKorisnika(
      @PathParam("gid") String gid,
      @PathParam("id") String id);

  @Path("{gid}/korisnik/{id}")
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiKorisnika(
      @PathParam("gid") String gid,
      @PathParam("id") String id);
}
