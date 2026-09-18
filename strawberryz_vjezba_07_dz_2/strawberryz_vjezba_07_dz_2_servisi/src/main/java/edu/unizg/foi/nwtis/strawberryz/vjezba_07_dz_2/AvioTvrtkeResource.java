package edu.unizg.foi.nwtis.strawberryz.vjezba_07_dz_2;

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

@Path("api/v1/aviotvrtke")
public class AvioTvrtkeResource {

  @Path("test")
  @GET
  @Produces({MediaType.TEXT_PLAIN})
  public Response test() {
    return Response.ok().entity("OK").build();
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajSveAvioTvrtke() {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new AvioTvrtkeDAO(vezaBP);
      var tvrtke = dao.dohvatiSve();
      if (tvrtke != null && !tvrtke.isEmpty()) {
        return Response.ok().entity(tvrtke).build();
      }
      return Response.status(Response.Status.NOT_FOUND)
                     .entity("Avio tvrtke ne postoje").build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dodajAvioTvrtku(AvioTvrtka avioTvrtka) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new AvioTvrtkeDAO(vezaBP);
      boolean odgovor = dao.dodaj(avioTvrtka);
      if (odgovor) {
        return Response.status(Response.Status.CREATED).entity(odgovor).build();
      }
      return Response.status(Response.Status.CONFLICT).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{atid}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajAvioTvrtku(@PathParam("atid") String atid) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new AvioTvrtkeDAO(vezaBP);
      var tvrtka = dao.dohvati(atid);
      if (tvrtka != null) {
        return Response.ok().entity(tvrtka).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{atid}")
  @PUT
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response azurirajAvioTvrtku(
      @PathParam("atid") String atid,
      AvioTvrtka avioTvrtka) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new AvioTvrtkeDAO(vezaBP);
      boolean odgovor = dao.azuriraj(atid, avioTvrtka);
      if (odgovor) {
        return Response.ok().entity(odgovor).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{atid}")
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiAvioTvrtku(@PathParam("atid") String atid) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new AvioTvrtkeDAO(vezaBP);
      boolean zastupnici = dao.obrisiSveZastupnike(atid);
      boolean odgovor = dao.obrisi(atid);
      if (odgovor && zastupnici) {
        return Response.ok().entity(odgovor).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{atid}/zastupnici")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajZastupnikeAvioTvrtke(@PathParam("atid") String atid) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new AvioTvrtkeDAO(vezaBP);
      var zastupnici = dao.dohvatiZastupnike(atid);
      if (zastupnici != null && !zastupnici.isEmpty()) {
        return Response.ok().entity(zastupnici).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{atid}/zastupnik/{id}")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  public Response dodajZastupnika(
      @PathParam("atid") String atid,
      @PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new AvioTvrtkeDAO(vezaBP);
      boolean odgovor = dao.dodajZastupnika(atid, id);
      if (odgovor) {
        return Response.status(Response.Status.CREATED).entity(odgovor).build();
      }
      return Response.status(Response.Status.CONFLICT).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{atid}/zastupnik/{id}")
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiZastupnika(
      @PathParam("atid") String atid,
      @PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new AvioTvrtkeDAO(vezaBP);
      boolean odgovor = dao.obrisiZastupnika(atid, id);
      if (odgovor) {
        return Response.ok().entity(odgovor).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}