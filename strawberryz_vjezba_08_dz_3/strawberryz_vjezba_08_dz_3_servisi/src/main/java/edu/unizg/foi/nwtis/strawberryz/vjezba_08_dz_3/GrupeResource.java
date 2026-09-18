package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3;

import edu.unizg.foi.nwtis.Grupa;
import edu.unizg.foi.nwtis.dao.GrupaDAO;
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

@Path("api/v1/grupe")
public class GrupeResource {

  @Path("test")
  @GET
  @Consumes({MediaType.TEXT_PLAIN})
  @Produces({MediaType.TEXT_PLAIN})
  public Response test() {
    return Response.ok().entity("OK").build();
  }

  @GET
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajSveGrupe() {
    try (var vezaBP = Main.dajVezu()) {
      var grupaDAO = new GrupaDAO(vezaBP);
      var grupe = grupaDAO.dohvatiSve();
      if (grupe != null) {
        return Response.ok().entity(grupe).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).entity("Grupe ne postoje").build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dodajGrupu(Grupa grupa) {
    try (var vezaBP = Main.dajVezu()) {
      var grupaDAO = new GrupaDAO(vezaBP);
      boolean odgovor = grupaDAO.dodaj(grupa);
      if (odgovor) {
        return Response.status(Response.Status.CREATED).entity(odgovor).build();
      }
      return Response.status(Response.Status.CONFLICT).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{gid}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajGrupu(@PathParam("gid") String gid) {
    try (var vezaBP = Main.dajVezu()) {
      var grupaDAO = new GrupaDAO(vezaBP);
      var grupa = grupaDAO.dohvati(gid);
      if (grupa != null) {
        return Response.ok().entity(grupa).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{gid}")
  @PUT
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response azurirajGrupu(@PathParam("gid") String gid, Grupa grupa) {
    try (var vezaBP = Main.dajVezu()) {
      var grupaDAO = new GrupaDAO(vezaBP);
      boolean odgovor = grupaDAO.azuriraj(gid, grupa);
      if (odgovor) {
        return Response.ok().entity(odgovor).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{gid}")
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiGrupu(@PathParam("gid") String gid) {
    try (var vezaBP = Main.dajVezu()) {
      var grupaDAO = new GrupaDAO(vezaBP);

      boolean uloge = grupaDAO.obrisiSveKorisnike(gid);

      boolean odgovor = grupaDAO.obrisi(gid);
      if (odgovor && uloge) {
        return Response.ok().entity(odgovor).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{gid}/korisnici")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajKorisnikeGrupe(@PathParam("gid") String gid) {
    try (var vezaBP = Main.dajVezu()) {
      var grupaDAO = new GrupaDAO(vezaBP);
      var korisnici = grupaDAO.dohvatiKorisnike(gid);
      if (korisnici != null && !korisnici.isEmpty()) {
        return Response.ok ().entity(korisnici).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{gid}/korisnik/{id}")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  public Response dodajKorisnika(
      @PathParam("gid") String gid,
      @PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      var grupaDAO = new GrupaDAO(vezaBP);
      boolean odgovor = grupaDAO.dodajKorisnika(gid, id);
      if (odgovor) {
        return Response.status(Response.Status.CREATED).entity(odgovor).build();
      }
      return Response.status(Response.Status.CONFLICT).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{gid}/korisnik/{id}")
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiKorisnika(
      @PathParam("gid") String gid,
      @PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      var grupaDAO = new GrupaDAO(vezaBP);
      boolean odgovor = grupaDAO.obrisiKorisnika(gid, id);
      if (odgovor) {
        return Response.ok().entity(odgovor).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}