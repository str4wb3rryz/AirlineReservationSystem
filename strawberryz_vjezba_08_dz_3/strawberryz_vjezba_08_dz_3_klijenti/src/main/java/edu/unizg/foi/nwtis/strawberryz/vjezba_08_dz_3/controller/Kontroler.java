package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.Korisnik;
import edu.unizg.foi.nwtis.Ulaznica;
import edu.unizg.foi.nwtis.AvioTvrtka;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.RadneRezervacije;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jms.PosiljateljJmsTema;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jms.SpremnikJmsPoruka;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Aviotvrtke;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Zastupnici;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici.AvioTvrtkeFacade;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.mikroservisi.klijenti.KorisniciRestKlijent;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.rest.KontrolaResource;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.mvc.binding.BindingResult;
import jakarta.mvc.binding.MvcBinding;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Controller
@Path("korisnici")
@RequestScoped
@PermitAll
public class Kontroler {

    @Inject
    private Models model;

    @Inject
    private BindingResult bindingResult;

    @Inject
    KorisniciRestKlijent korisniciRestKlijent;

    @Inject
    KorisniciFacade korisniciFacade;

    @Inject
    PosiljateljJmsTema posiljateljJmsTema;

    @Inject
    AvioTvrtkeFacade avioTvrtkeFacade;

    @Inject
    private SecurityContext securityContext;

    @Inject
    SpremnikJmsPoruka spremnikJmsPoruka;

    @Inject
    private RadneRezervacije radneRezervacije;

    @Inject
    @RestClient
    private KontrolaResource kontrolaResource;

    @GET
    @Path("pocetak")
    @View("index.jsp")
    @PermitAll
    public void pocetak() {
        osigurajUlaznicu();
        if (this.securityContext.getCallerPrincipal() != null) {
            var korId = this.securityContext.getCallerPrincipal().getName();
            model.put("korisnik", korId);

            if (!radneRezervacije.isUlaznicaKreirana()) {
            	var entitetKorisnika = korisniciFacade.find(korId);
                if (entitetKorisnika != null) {
                	try (Response odgovorKontrola = kontrolaResource.korisnikDodaj(korId)) {
                        if (odgovorKontrola.getStatus() != Response.Status.CREATED.getStatusCode()
                                && odgovorKontrola.getStatus() != Response.Status.CONFLICT.getStatusCode()) {
                            model.put("korisniciPoruke", "Korisnik upisan u bazu, ali nije registriran na poslužitelju kontrola.");
                        }
                    } catch (Exception e) {
                        model.put("greskaUlaznica", "Greška komunikacije s mikroservisom Korisnik " + e.getMessage());
                    }

                    try (Response odgovor = kontrolaResource.ulaznicaDodaj(korId)) {
                        if (odgovor.getStatus() == Response.Status.CREATED.getStatusCode()
                                && odgovor.hasEntity()) {
                            Ulaznica ulaznica = odgovor.readEntity(Ulaznica.class);
                            radneRezervacije.setIdUlaznice(ulaznica.id());
                        } else if (odgovor.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                        } else {
                            model.put("greskaUlaznica", "Nije moguće kreirati ulaznicu (status "
                                    + odgovor.getStatus() + ").");
                        }
                    } catch (Exception e) {
                        model.put("greskaUlaznica", "Greška komunikacije s mikroservisom Ulaznice " + e.getMessage());
                    }
                }
            }
        }
    }

    @GET
    @Path("noviKorisnik")
    @View("noviKorisnik.jsp")
    @PermitAll
    public void noviKorisnik() {
    }

    @POST
    @Path("dodajKorisnika")
    @PermitAll
    public String dodajKorisnika(@MvcBinding @FormParam("korisnik") String korId,
            @FormParam("lozinka") String lozinka, @FormParam("prezime") String prezime,
            @FormParam("ime") String ime, @FormParam("email") String email) {
        if (bindingResult.isFailed() || korId == null || korId.trim().length() == 0
                || prezime == null || lozinka.trim().length() == 0 || lozinka == null
                || prezime.trim().length() == 0 || ime == null || ime.trim().length() == 0
                || email == null || email.trim().length() == 0) {
            model.put("poruka", "Nisu upisani potrebni podaci.");
            model.put("pogreska", true);
            model.put("korId", korId != null ? korId : "");
            model.put("lozinka", "");
            model.put("prezime", prezime != null ? prezime : "");
            model.put("ime", ime != null ? ime : "");
            model.put("email", email != null ? email : "");
            return "noviKorisnik.jsp";
        }
        var korisnik = new Korisnik(korId, lozinka, prezime, ime, email);
        try {
            korisniciFacade.create(korisniciFacade.pretvori(korisnik), "nwtis");
        } catch (Exception _) {
            model.put("korisniciPoruke", "Problem kod upisa korisnika.");
            model.put("pogreska", true);
            return "noviKorisnik.jsp";
        }
        try {
            posiljateljJmsTema.novaPorukaObjekt(korisnik);
        } catch (Exception _) {
        }
        try (Response odgovorKontrola = kontrolaResource.korisnikDodaj(korId)) {
            odgovorKontrola.getStatus();
        } catch (Exception _) {
        }
        model.put("korisniciPoruke", "Uspješno dodan korisnik: " + korisnik.ime() + " " + korisnik.prezime());
        return "noviKorisnik.jsp";
    }

    @GET
    @Path("ispisAvioTvrtki")
    @View("ispisAvioTvrtki.jsp")
    @PermitAll
    public void ispisAvioTvrtki() {
        List<Aviotvrtke> aviotvrtke = avioTvrtkeFacade.findAll();
        if (aviotvrtke == null) {
            aviotvrtke = new ArrayList<>();
        }

        Map<String, List<Zastupnici>> zastupniciPoTvrtki = new LinkedHashMap<>();
        for (Aviotvrtke at : aviotvrtke) {
            List<Zastupnici> zastupnici = avioTvrtkeFacade.findZastupnici(at.getTvrtka());
            zastupniciPoTvrtki.put(at.getTvrtka(),
                    zastupnici != null ? zastupnici : new ArrayList<>());
        }

        model.put("aviotvrtke", aviotvrtke);
        model.put("zastupnici", zastupniciPoTvrtki);
    }

    @GET
    @Path("pregledRedaPoruka")
    @View("pregledRedaPoruka.jsp")
    @PermitAll
    public void pregledRedaPoruka() {
        osigurajUlaznicu();
      var listaPoruka = this.spremnikJmsPoruka.dajRedPoruka();
      model.put("poruke", listaPoruka);
    }

    @GET
    @Path("obrisiRedPoruka")
    @View("pregledRedaPoruka.jsp")
    @PermitAll
    public void obrisiRedPoruka() {
        osigurajUlaznicu();
      this.spremnikJmsPoruka.obrisiRedPoruka();
      var listaPoruka = this.spremnikJmsPoruka.dajRedPoruka();
      model.put("poruke", listaPoruka);
    }

    private void osigurajUlaznicu() {
        if (this.securityContext.getCallerPrincipal() == null) {
            return;
        }
        String id = radneRezervacije.getIdUlaznice();
        if (id == null || id.isBlank()) {
            kreirajUlaznicu();
            return;
        }
        int status = provjeriUlaznicu(id);
        if (status == Response.Status.OK.getStatusCode()) {
            return;
        }
        if (status == Response.Status.NOT_FOUND.getStatusCode()) {
            kreirajUlaznicu();
            return;
        }
        int status2 = provjeriUlaznicu(id);
        if (status2 == Response.Status.OK.getStatusCode()) {
            return;
        }
        if (status2 == Response.Status.NOT_FOUND.getStatusCode()) {
            kreirajUlaznicu();
            return;
        }
        ponistiUlaznicu(id);
        kreirajUlaznicu();
    }

    private int provjeriUlaznicu(String id) {
        try (Response odgovor = kontrolaResource.ulaznicaProvjeri(id)) {
            return odgovor.getStatus();
        } catch (Exception e) {
            return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        }
    }

    private void ponistiUlaznicu(String id) {
        try (Response odgovor = kontrolaResource.ulaznicaPonisti(id)) {
            odgovor.getStatus();
        } catch (Exception _) {
        }
        radneRezervacije.setIdUlaznice(null);
    }

    private void kreirajUlaznicu() {
        if (this.securityContext.getCallerPrincipal() == null) {
            return;
        }
        String korId = this.securityContext.getCallerPrincipal().getName();
        try (Response registracija = kontrolaResource.korisnikDodaj(korId)) {
            registracija.getStatus();
        } catch (Exception _) {
        }
        try (Response odgovor = kontrolaResource.ulaznicaDodaj(korId)) {
            int status = odgovor.getStatus();
            if (status == Response.Status.CREATED.getStatusCode() && odgovor.hasEntity()) {
                Ulaznica ulaznica = odgovor.readEntity(Ulaznica.class);
                radneRezervacije.setIdUlaznice(ulaznica.id());
            } else {
                radneRezervacije.setIdUlaznice(null);
            }
        } catch (Exception _) {
            radneRezervacije.setIdUlaznice(null);
        }
    }

}
