package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.controller;

import java.io.Serializable;
import java.net.URI;

import edu.unizg.foi.nwtis.Ulaznica;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.RadneRezervacije;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.rest.KontrolaResource;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.binding.BindingResult;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Controller
@Path("prijava")
@PermitAll
public class KontrolerPrijava implements Serializable {

    private static final long serialVersionUID = 3488625477601542324L;

    @Inject
    private Models model;

    @Inject
    private BindingResult bindingResult;

    @Inject
    KorisniciFacade korisniciFacade;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private RadneRezervacije radneRezervacije;

    @Inject
    @RestClient
    private KontrolaResource kontrolaResource;

    @GET
    @Path("prijavaKorisnika")
    public String prijavaKorisnika() {
        return "prijavaKorisnika.jsp";
    }

    @GET
    @Path("prijavaKorisnikaPogreska")
    public String prijavaKorisnikaPogreska() {
        return "prijavaKorisnikaPogreska.jsp";
    }

    @GET
    @Path("odjavaKorisnika")
    public Response odjavaKorisnika(@Context HttpServletRequest request) throws Exception {
    	obrisiUlaznicu();
        request.logout();
        request.getSession().invalidate();
        return Response
            .seeOther(URI.create(request.getContextPath() + "/")).build();
    }

   private void obrisiUlaznicu() {
       String id = radneRezervacije.getIdUlaznice();
       if (id == null || id.isBlank()) {
           return;
       }
       try (Response odgovor = kontrolaResource.ulaznicaPonisti(id)) {
           odgovor.getStatus();
       } catch (Exception _) {
       }
       radneRezervacije.setIdUlaznice(null);
   }
}
