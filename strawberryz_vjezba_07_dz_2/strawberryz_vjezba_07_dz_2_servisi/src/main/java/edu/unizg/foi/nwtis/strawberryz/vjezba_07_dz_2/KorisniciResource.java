package edu.unizg.foi.nwtis.strawberryz.vjezba_07_dz_2;

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

@Path("api/v1/korisnici")
public class KorisniciResource {

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
  public Response dajSveKorisnike() {
    try (var vezaBP = Main.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var korisnici = korisnikDAO.dohvatiSve();
      if (korisnici != null) {
        return Response.ok().entity(korisnici).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).entity("Korisnici ne postoje").build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response kreirajKorisnika(Korisnik korisnik) {
    try (var vezaBP = Main.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var odgovor = korisnikDAO.dodaj(korisnik);
      if (odgovor) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.CONFLICT).entity("Korisnik već postoji").build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}")
  @GET
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajKorisnika(@PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var korisnik = korisnikDAO.dohvati(id, null, false);
      if (korisnik != null) {
        return Response.ok().entity(korisnik).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).entity("Korisnik ne postoji").build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}/{lozinka}")
  @GET
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajPrivatnoKorisnika(@PathParam("id") String id,
      @PathParam("lozinka") String lozinka) {
    try (var vezaBP = Main.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var korisnik = korisnikDAO.dohvati(id, lozinka);
      if (korisnik != null) {
        return Response.ok().entity(korisnik).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).entity("Korisnik ne postoji").build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}")
  @PUT
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response azurirajKorisnika(Korisnik korisnik) {
    try (var vezaBP = Main.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var odgovor = korisnikDAO.azuriraj(korisnik);
      if (odgovor) {
        return Response.ok().entity(odgovor).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).entity("Korisnik ne postoji").build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}")
  @DELETE
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiKorisnika(@PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var odgovor = korisnikDAO.obrisi(id);
      if (odgovor) {
        return Response.ok().entity(odgovor).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).entity("Korisnik ne postoji").build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("email/{email}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajKorisnikaEmail(@PathParam("email") String email) {

    try (var vezaBP = Main.dajVezu()) {

      var korisnikDAO = new KorisnikDAO(vezaBP);
      var korisnik = korisnikDAO.dohvatiEmail(email);

      if (korisnik != null) {
        return Response.ok().entity(korisnik).build();
      }

      return Response.status(Response.Status.NOT_FOUND).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @GET
  @Path("{odStranice}/{brojStranica}")
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajKorisnikeStranicenje(
      @PathParam("odStranice") int odStranice,
      @PathParam("brojStranica") int brojStranica) {

    try (var vezaBP = Main.dajVezu()) {

      var korisnikDAO = new KorisnikDAO(vezaBP);

      var korisnici = korisnikDAO.dohvatiSveStranicenje(odStranice, brojStranica);

      return Response.ok().entity(korisnici).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @Path("{ime}/{prezime}/{odStranice}/{brojStranica}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajImePrezime(
      @PathParam("ime") String ime,
      @PathParam("prezime") String prezime,
      @PathParam("odStranice") int odStranice,
      @PathParam("brojStranica") int brojStranica) {

    try (var vezaBP = Main.dajVezu()) {

      var korisnikDAO = new KorisnikDAO(vezaBP);

      var korisnici =
          korisnikDAO.dohvatiImePrezimeStranicenje(
              ime, prezime, odStranice, brojStranica);

      return Response.ok().entity(korisnici).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @Path("{id}/grupa")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajGrupe(@PathParam("id") String id) {

    try (var vezaBP = Main.dajVezu()) {

      var korisnikDAO = new KorisnikDAO(vezaBP);

      var grupe = korisnikDAO.dohvatiGrupe(id);

      if (grupe != null && !grupe.isEmpty()) {
        return Response.ok().entity(grupe).build();
      }

      return Response.status(Response.Status.NOT_FOUND).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @Path("{id}/grupa/{gid}")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  public Response dodajGrupu(
      @PathParam("id") String id,
      @PathParam("gid") String gid) {

    try (var vezaBP = Main.dajVezu()) {

      var korisnikDAO = new KorisnikDAO(vezaBP);

      boolean odgovor =
          korisnikDAO.dodajGrupu(id, gid);

      if (odgovor) {
        return Response.status(Response.Status.CREATED).entity(odgovor).build();
      }

      return Response.status(Response.Status.CONFLICT).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @Path("{id}/grupa/{gid}")
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiGrupu(
      @PathParam("id") String id,
      @PathParam("gid") String gid) {

    try (var vezaBP = Main.dajVezu()) {

      var korisnikDAO = new KorisnikDAO(vezaBP);

      boolean odgovor =
          korisnikDAO.obrisiGrupu(id, gid);

      if (odgovor) {
        return Response.ok().entity(odgovor).build();
      }

      return Response.status(Response.Status.NOT_FOUND).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @Path("{id}/aviotvrtka")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajAviotvrtku(@PathParam("id") String id) {

    try (var vezaBP = Main.dajVezu()) {

      var korisnikDAO = new KorisnikDAO(vezaBP);

      String tvrtka =
          korisnikDAO.dohvatiAviotvrtku(id);

      if (tvrtka != null) {
        return Response.ok().entity(tvrtka).build();
      }

      return Response.status(Response.Status.NOT_FOUND).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @Path("{id}/aviotvrtka/{atid}")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  public Response dodajAviotvrtku(
      @PathParam("id") String id,
      @PathParam("atid") String atid) {

    try (var vezaBP = Main.dajVezu()) {

      var korisnikDAO = new KorisnikDAO(vezaBP);

      boolean odgovor =
          korisnikDAO.dodajAviotvrtku(id, atid);

      if (odgovor) {
        return Response.status(Response.Status.CREATED).entity(odgovor).build();
      }

      return Response.status(Response.Status.CONFLICT).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @Path("{id}/aviotvrtka/{atid}")
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiAviotvrtku(
      @PathParam("id") String id,
      @PathParam("atid") String atid) {

    try (var vezaBP = Main.dajVezu()) {

      var korisnikDAO = new KorisnikDAO(vezaBP);

      boolean odgovor =
          korisnikDAO.obrisiAviotvrtku(id, atid);

      if (odgovor) {
        return Response.ok().entity(odgovor).build();
      }

      return Response.status(Response.Status.NOT_FOUND).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @Path("email/{email}/aviotvrtka")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajAviotvrtkuEmail(
      @PathParam("email") String email) {

    try (var vezaBP = Main.dajVezu()) {

      var korisnikDAO = new KorisnikDAO(vezaBP);

      String tvrtka =
          korisnikDAO.dohvatiAviotvrtkuEmail(email);

      if (tvrtka != null) {
        return Response.ok().entity(tvrtka).build();
      }

      return Response.status(Response.Status.NOT_FOUND).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
}
