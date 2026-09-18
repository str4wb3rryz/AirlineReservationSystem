package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.mikroservisi.klijenti;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import edu.unizg.foi.nwtis.AvioTvrtka;
import edu.unizg.foi.nwtis.dao.AvioTvrtkeDAO;
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

//@RegisterRestClient(baseUri = "http://20.24.5.5:8080/api/v1/aviotvrtke")
@RegisterRestClient(configKey = "servisi.aviotvrtke")
public interface AvioTvrtkeRestKlijent {

  @Path("test")
  @GET
  @Produces({MediaType.TEXT_PLAIN})
  public Response test();

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajSveAvioTvrtke();

  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dodajAvioTvrtku(AvioTvrtka avioTvrtka);

  @Path("{atid}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajAvioTvrtku(@PathParam("atid") String atid);

  @Path("{atid}")
  @PUT
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response azurirajAvioTvrtku(
      @PathParam("atid") String atid,
      AvioTvrtka avioTvrtka);

  @Path("{atid}")
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiAvioTvrtku(@PathParam("atid") String atid);

  @Path("{atid}/zastupnici")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajZastupnikeAvioTvrtke(@PathParam("atid") String atid);

  @Path("{atid}/zastupnik/{id}")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  public Response dodajZastupnika(
      @PathParam("atid") String atid,
      @PathParam("id") String id);

  @Path("{atid}/zastupnik/{id}")
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiZastupnika(
      @PathParam("atid") String atid,
      @PathParam("id") String id);
}