package edu.unizg.foi.nwtis.strawberryz.vjezba_07_dz_2.mikroservisi.klijenti;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import edu.unizg.foi.nwtis.Korisnik;
import edu.unizg.foi.nwtis.dao.KorisnikDAO;
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

// @RegisterRestClient(baseUri = "http://20.24.5.5:8080/api/v1/korisnici")
@RegisterRestClient(configKey = "servisi.korisnici")
public interface KorisniciRestKlijent {

  @Path("test")
  @GET
  @Consumes({MediaType.TEXT_PLAIN})
  @Produces({MediaType.TEXT_PLAIN})
  public Response test();


  @GET
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajSveKorisnike();

  @Path("{id}")
  @GET
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajKorisnika(@PathParam("id") String id);

  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response kreirajKorisnika(Korisnik korisnik);

  @Path("{id}/lozinka")
  @GET
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajPrivatnoKorisnika(@PathParam("id") String id);

  @Path("{id}")
  @PUT
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response azurirajKorisnika(@PathParam("id") Korisnik korisnik);

  @Path("{id}")
  @DELETE
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiKorisnika(@PathParam("id") String id);
  
  @Path("email/{email}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajKorisnikaEmail(@PathParam("email") String email);
  
  @GET
  @Path("{odStranice}/{brojStranica}")
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajKorisnikeStranicenje(
      @PathParam("odStranice") int odStranice,
      @PathParam("brojStranica") int brojStranica);
  
  @Path("{ime}/{prezime}/{odStranice}/{brojStranica}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajImePrezime(
      @PathParam("ime") String ime,
      @PathParam("prezime") String prezime,
      @PathParam("odStranice") int odStranice,
      @PathParam("brojStranica") int brojStranica);
  
  @Path("{id}/grupa")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajGrupe(@PathParam("id") String id);
  
  @Path("{id}/grupa/{gid}")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  public Response dodajGrupu(
      @PathParam("id") String id,
      @PathParam("gid") String gid);
  
  @Path("{id}/grupa/{gid}")
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiGrupu(
      @PathParam("id") String id,
      @PathParam("gid") String gid);
  
  @Path("{id}/aviotvrtka")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajAviotvrtku(@PathParam("id") String id);
  
  @Path("{id}/aviotvrtka/{atid}")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  public Response dodajAviotvrtku(
      @PathParam("id") String id,
      @PathParam("atid") String atid);
  
  @Path("{id}/aviotvrtka/{atid}")
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiAviotvrtku(
      @PathParam("id") String id,
      @PathParam("atid") String atid);
  
  @Path("email/{email}/aviotvrtka")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajAviotvrtkuEmail(
      @PathParam("email") String email);

}