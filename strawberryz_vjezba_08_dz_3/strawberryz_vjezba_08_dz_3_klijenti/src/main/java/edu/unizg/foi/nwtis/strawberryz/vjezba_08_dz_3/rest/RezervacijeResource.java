package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.rest;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import edu.unizg.foi.nwtis.Let;
import edu.unizg.foi.nwtis.RezervacijaSjedala;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "mikroservisi.rezervacije")
@Produces(MediaType.APPLICATION_JSON)
public interface RezervacijeResource {

    @HEAD
    @Path("{id}/ping")
    Response ping(@PathParam("id") int id);

    @HEAD
    @Path("{id}/pauza/{milisek}")
    Response pauza(@PathParam("id") int id, @PathParam("milisek") String milisek);

    @GET
    @Path("{id}/{idRezervacijeSjedala}")
    Response ispisSjedala(
        @PathParam("id") int id,
        @PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
        @HeaderParam("idUlaznice") String idUlaznice);

    @GET
    @Path("{id}/ispis/putnik")
    Response ispisPutnik(
        @PathParam("id") int id,
        @HeaderParam("idUlaznice") String idUlaznice,
        @HeaderParam("putnik") String putnik,
        @HeaderParam("odDatuma") String odDatuma,
        @HeaderParam("doDatuma") String doDatuma);

    @GET
    @Path("{id}/ispis/let")
    Response ispisLet(
        @PathParam("id") int id,
        @HeaderParam("idUlaznice") String idUlaznice,
        @HeaderParam("oznakaLeta") String oznakaLeta,
        @HeaderParam("datum") String datum);

    @GET
    @Path("{id}/ispis/letovi/{avioTvrtka}")
    Response ispisLetovi(
        @PathParam("id") int id,
        @PathParam("avioTvrtka") String avioTvrtka,
        @HeaderParam("idUlaznice") String idUlaznice);

    @GET
    @Path("{id}/ispis/{idLeta}")
    Response ispisLeta(
        @PathParam("id") int id,
        @PathParam("idLeta") String idLeta,
        @HeaderParam("idUlaznice") String idUlaznice);

    @POST
    @Path("{id}/rezerviraj")
    @Consumes(MediaType.APPLICATION_JSON)
    Response rezerviraj(
        @PathParam("id") int id,
        @HeaderParam("idUlaznice") String idUlaznice,
        @HeaderParam("idLeta") String idLeta,
        @HeaderParam("razred") String razred,
        @HeaderParam("putnik") String putnik);

    @PUT
    @Path("{id}/{idRezervacijeSjedala}")
    @Consumes(MediaType.APPLICATION_JSON)
    Response potvrdi(
        @PathParam("id") int id,
        @PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
        @HeaderParam("idUlaznice") String idUlaznice,
        RezervacijaSjedala rezervacija);

    @POST
    @Path("{id}/rezerviraj/grupa")
    @Consumes(MediaType.APPLICATION_JSON)
    Response rezervirajGrupa(
        @PathParam("id") int id,
        @HeaderParam("idUlaznice") String idUlaznice,
        @HeaderParam("idLeta") String idLeta,
        @HeaderParam("razred") String razred,
        List<RezervacijaSjedala> rezervacije);

    @PUT
    @Path("{id}/potvrdi/grupa")
    @Consumes(MediaType.APPLICATION_JSON)
    Response potvrdiGrupa(
        @PathParam("id") int id,
        @HeaderParam("idUlaznice") String idUlaznice,
        List<RezervacijaSjedala> rezervacije);

    @HEAD
    @Path("aviotvrtke/{id}/ping")
    Response pingAvioTvrtke(@PathParam("id") int id);

    @GET
    @Path("aviotvrtke/{id}/ispis/putnik")
    Response avioTvrtkeIspisPutnik(
        @PathParam("id") int id,
        @HeaderParam("idUlaznice") String idUlaznice,
        @HeaderParam("putnik") String putnik,
        @HeaderParam("odDatuma") String odDatuma,
        @HeaderParam("doDatuma") String doDatuma);

    @GET
    @Path("aviotvrtke/{id}/ispis/letovi")
    Response avioTvrtkeIspisLetovi(
        @PathParam("id") int id,
        @HeaderParam("idUlaznice") String idUlaznice);

    @GET
    @Path("aviotvrtke/{id}/stanje/{idLeta}")
    Response avioTvrtkeStanje(
        @PathParam("id") int id,
        @PathParam("idLeta") String idLeta,
        @HeaderParam("idUlaznice") String idUlaznice);

    @PUT
    @Path("aviotvrtke/{id}/zatvori/{idLeta}")
    @Consumes(MediaType.APPLICATION_JSON)
    Response avioTvrtkeZatvoriLet(
        @PathParam("id") int id,
        @PathParam("idLeta") String idLeta,
        @HeaderParam("idUlaznice") String idUlaznice,
        @HeaderParam("odReda") String odReda,
        @HeaderParam("doReda") String doReda,
        Let let);

    @PUT
    @Path("aviotvrtke/{id}/zatvori")
    @Consumes(MediaType.APPLICATION_JSON)
    Response avioTvrtkeZatvori(
        @PathParam("id") int id,
        @HeaderParam("idUlaznice") String idUlaznice,
        @HeaderParam("odDatuma") String odDatuma,
        @HeaderParam("doDatuma") String doDatuma,
        Let let);

    @POST
    @Path("aviotvrtke/{id}/inicijaliziraj/let")
    @Consumes(MediaType.APPLICATION_JSON)
    Response avioTvrtkeInicijalizirajLet(
        @PathParam("id") int id,
        @HeaderParam("idUlaznice") String idUlaznice,
        @HeaderParam("oznakaLeta") String oznakaLeta,
        @HeaderParam("odDatuma") String odDatuma,
        @HeaderParam("doDatuma") String doDatuma,
        Let let);

    @POST
    @Path("aviotvrtke/{id}/inicijaliziraj")
    @Consumes(MediaType.APPLICATION_JSON)
    Response avioTvrtkeInicijaliziraj(
        @PathParam("id") int id,
        @HeaderParam("idUlaznice") String idUlaznice,
        @HeaderParam("odDatuma") String odDatuma,
        @HeaderParam("doDatuma") String doDatuma,
        Let let);

    @GET
    @Path("aviotvrtke/{id}/ispis/{idLeta}")
    Response avioTvrtkeIspisLeta(
        @PathParam("id") int id,
        @PathParam("idLeta") String idLeta,
        @HeaderParam("idUlaznice") String idUlaznice);

    @GET
    @Path("aviotvrtke/{id}/ispis/let")
    Response avioTvrtkeIspisLet(
        @PathParam("id") int id,
        @HeaderParam("idUlaznice") String idUlaznice,
        @HeaderParam("oznakaLeta") String oznakaLeta,
        @HeaderParam("datum") String datum);

    @HEAD
    @Path("admin/{id}/ping")
    Response pingAdmin(@PathParam("id") int id);

    @HEAD
    @Path("admin/{id}/kraj")
    Response krajAdmin(@PathParam("id") int id);

    @DELETE
    @Path("admin/{id}/rezervacije")
    Response adminObrisiRezervacije(@PathParam("id") int id);
}
