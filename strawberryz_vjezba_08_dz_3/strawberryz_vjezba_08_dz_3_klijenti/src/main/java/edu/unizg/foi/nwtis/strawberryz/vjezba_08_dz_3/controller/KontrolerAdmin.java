package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.Ulaznica;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.RadneRezervacije;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jms.SpremnikJmsPoruka;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Racun;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici.RacunFacade;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici.RacunStavkaFacade;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.rest.KontrolaResource;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.rest.RezervacijeResource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.mvc.binding.BindingResult;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Controller
@Path("admin")
@RequestScoped
@RolesAllowed("admin")
public class KontrolerAdmin {

    public static class StatusRezervacije {
        private final String id;
        private final boolean korisnickiAktivan;
        private final boolean avioAktivan;
        private final boolean adminAktivan;

        public StatusRezervacije(String id, boolean korisnickiAktivan, boolean avioAktivan,
                boolean adminAktivan) {
            this.id = id;
            this.korisnickiAktivan = korisnickiAktivan;
            this.avioAktivan = avioAktivan;
            this.adminAktivan = adminAktivan;
        }

        public String getId() {
            return this.id;
        }

        public boolean isKorisnickiAktivan() {
            return this.korisnickiAktivan;
        }

        public boolean isAvioAktivan() {
            return this.avioAktivan;
        }

        public boolean isAdminAktivan() {
            return this.adminAktivan;
        }
    }

    public class RacunPrikaz {

        private final int id;
        private final Timestamp vrijeme;
        private final String korisnik;
        private final int brojStavki;
        private final long stvarniBrojStavki;
        private final double iznos;
        private final boolean otvoren;
        private final boolean uskladjen;

        public RacunPrikaz(int id, Timestamp vrijeme, String korisnik, int brojStavki,
                long stvarniBrojStavki, double iznos, boolean otvoren, boolean uskladjen) {
            this.id = id;
            this.vrijeme = vrijeme;
            this.korisnik = korisnik;
            this.brojStavki = brojStavki;
            this.stvarniBrojStavki = stvarniBrojStavki;
            this.iznos = iznos;
            this.otvoren = otvoren;
            this.uskladjen = uskladjen;
        }

        public int getId()                { return id; }
        public Timestamp getVrijeme()     { return vrijeme; }
        public String getKorisnik()       { return korisnik; }
        public int getBrojStavki()        { return brojStavki; }
        public long getStvarniBrojStavki(){ return stvarniBrojStavki; }
        public double getIznos()          { return iznos; }
        public boolean isOtvoren()        { return otvoren; }
        public boolean isUskladjen()      { return uskladjen; }
    }

    @Inject
    private Models model;
    @Inject
    private BindingResult bindingResult;
    @Inject
    private SpremnikJmsPoruka spremnikJmsPoruka;
    @Inject
    private KorisniciFacade korisniciFacade;
    @Inject
    private RacunFacade racunFacade;
    @Inject
    private RacunStavkaFacade racunStavkaFacade;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private RadneRezervacije radneRezervacije;

    @Inject
    @RestClient
    private KontrolaResource kontrolaRestClient;

    @Inject
    @RestClient
    private RezervacijeResource rezervacijeRestClient;

    @GET
    @Path("pocetak")
    @View("admin/index.jsp")
    public void pocetak() {
        if (this.securityContext.getCallerPrincipal() != null) {
            var ime = this.securityContext.getCallerPrincipal().getName();
            model.put("korisnik", ime);
        }
    }

    @GET
    @Path("pregledPosluzitelja")
    @View("admin/pregledPosluzitelja.jsp")
    public void pregledPosluzitelja() {
        var config = ConfigProvider.getConfig();

        var kontrolaKorisnickiAktivan = this.provjeriHead(() -> this.kontrolaRestClient.ping());
        var kontrolaAdminAktivan = this.provjeriHead(() -> this.kontrolaRestClient.adminPing());

        model.put("kontrolaKorisnickiAktivan", kontrolaKorisnickiAktivan);
        model.put("kontrolaAdminAktivan", kontrolaAdminAktivan);

        List<StatusRezervacije> statusiRezervacije = new ArrayList<>();
        var idx = 0;
        while (this.postojiRezervacija(config, idx)) {
            final var id = idx;
            var korisnickiAktivan = this.provjeriHead(() -> this.rezervacijeRestClient.ping(id));
            var avioAktivan = this.provjeriHead(() -> this.rezervacijeRestClient.pingAvioTvrtke(id));
            var adminAktivan = this.provjeriHead(() -> this.rezervacijeRestClient.pingAdmin(id));
            statusiRezervacije
                    .add(new StatusRezervacije(String.valueOf(idx), korisnickiAktivan, avioAktivan, adminAktivan));
            idx++;
        }
        model.put("statusiRezervacije", statusiRezervacije);
    }

    @GET
    @Path("krajKontrolaAdmin")
    @View("admin/pregledPosluzitelja.jsp")
    public void krajKontrolaAdmin() {
        this.provjeriHead(() -> this.kontrolaRestClient.adminKraj());
        this.pregledPosluzitelja();
    }

    @GET
    @Path("krajRezervacijeAdmin/{id}")
    @View("admin/pregledPosluzitelja.jsp")
    public void krajRezervacijeAdmin(@PathParam("id") int id) {
        this.provjeriHead(() -> this.rezervacijeRestClient.krajAdmin(id));
        this.pregledPosluzitelja();
    }

    private boolean postojiRezervacija(Config config, int idx) {
        Optional<String> adresa = config.getOptionalValue("rezervacije." + idx + ".adresa", String.class);
        return adresa.isPresent();
    }

    private boolean provjeriHead(Supplier<Response> poziv) {
        try {
            Response response = poziv.get();
            try {
                var status = response.getStatus();
                return status >= 200 && status < 300;
            } finally {
                response.close();
            }
        } catch (Exception e) {
            return false;
        }
    }

    @GET
    @Path("nadzornaPlocaRezervacije")
    @View("admin/nadzornaPlocaRezervacije.jsp")
    public void nadzornaPlocaRezervacije() {
    }

    @GET
    @Path("nadzornaPlocaKorisnici")
    @View("admin/nadzornaPlocaKorisnici.jsp")
    public void nadzornaPlocaKorisnici() {
        var listaPoruka = this.spremnikJmsPoruka.dajPorukeTeme();
        model.put("korisniciPoruke", listaPoruka);
    }

    @GET
    @Path("obrisiKorisnikaIzListe/{indeks}")
    @View("admin/nadzornaPlocaKorisnici.jsp")
    public void obrisiKorisnikaIzListe(@PathParam("indeks") int indeks) {
        this.spremnikJmsPoruka.obrisiKorisnikaIzTeme(indeks);
        var listaPoruka = this.spremnikJmsPoruka.dajPorukeTeme();
        model.put("korisniciPoruke", listaPoruka);
    }

    @GET
    @Path("obrisiSveKorisnike")
    @View("admin/nadzornaPlocaKorisnici.jsp")
    public void obrisiSveKorisnike() {
        this.spremnikJmsPoruka.obrisiPorukeTeme();
        var listaPoruka = this.spremnikJmsPoruka.dajPorukeTeme();
        model.put("korisniciPoruke", listaPoruka);
    }

    @GET
    @Path("nadzornaPlocaRacuni")
    @View("admin/nadzornaPlocaRacuni.jsp")
    public void nadzornaPlocaRacuni() {
        model.put("racuni", parsirajRacune(this.spremnikJmsPoruka.dajRedPoruka()));
    }

    @GET
    @Path("pregledStavkiRacunaPoruke/{indeks}")
    @View("admin/pregledStavkiRacunaPoruke.jsp")
    public void pregledStavkiRacunaPoruke(@PathParam("indeks") int indeks) {
        var poruke = this.spremnikJmsPoruka.dajRedPoruka();
        edu.unizg.foi.nwtis.Racun racun = null;
        if (indeks >= 0 && indeks < poruke.size()) {
            racun = parsirajRacun(poruke.get(indeks));
        }
        model.put("indeks", indeks);
        model.put("racun", racun);
        model.put("stavke", racun != null ? racun.stavke() : new ArrayList<>());
    }

    @GET
    @Path("obrisiRacunIzListe/{indeks}")
    @View("admin/nadzornaPlocaRacuni.jsp")
    public void obrisiRacunIzListe(@PathParam("indeks") int indeks) {
        this.spremnikJmsPoruka.obrisiRedPoruka(indeks);
        model.put("racuni", parsirajRacune(this.spremnikJmsPoruka.dajRedPoruka()));
    }

    @GET
    @Path("obrisiSveRacune")
    @View("admin/nadzornaPlocaRacuni.jsp")
    public void obrisiSveRacune() {
        this.spremnikJmsPoruka.obrisiRedPoruka();
        model.put("racuni", parsirajRacune(this.spremnikJmsPoruka.dajRedPoruka()));
    }

    @GET
    @Path("pregledZatvorenihRacuna")
    @View("admin/pregledZatvorenihRacuna.jsp")
    public void pregledZatvorenihRacuna(@QueryParam("imeRacuna") String imeRacuna,
            @QueryParam("prezimeRacuna") String prezimeRacuna,
            @QueryParam("imeStavke") String imeStavke,
            @QueryParam("prezimeStavke") String prezimeStavke) {

        List<RacunPrikaz> racuni = new ArrayList<>();
        for (Racun r : racunFacade.findZatvoreni(imeRacuna, prezimeRacuna, imeStavke, prezimeStavke)) {
            long stvarni = racunFacade.stvarniBrojStavki(r.getId());
            String korisnik = r.getKorisnici() != null
                ? r.getKorisnici().getIme() + " " + r.getKorisnici().getPrezime() : "";
            racuni.add(new RacunPrikaz(r.getId(), r.getVrijeme(), korisnik, r.getBrojstavki(),
                stvarni, r.getIznos(), false, r.getBrojstavki() == stvarni));
        }

        model.put("racuni", racuni);
        model.put("imeRacuna", imeRacuna);
        model.put("prezimeRacuna", prezimeRacuna);
        model.put("imeStavke", imeStavke);
        model.put("prezimeStavke", prezimeStavke);
    }

    @GET
    @Path("pregledStavkiZatvorenogRacuna/{id}")
    @View("admin/pregledStavkiZatvorenogRacuna.jsp")
    public void pregledStavkiZatvorenogRacuna(@PathParam("id") int id) {
        Racun r = racunFacade.find(id);
        if (r != null) {
            String korisnik = r.getKorisnici() != null
                ? r.getKorisnici().getIme() + " " + r.getKorisnici().getPrezime() : "";
            long stvarni = racunFacade.stvarniBrojStavki(id);
            model.put("racun", new RacunPrikaz(r.getId(), r.getVrijeme(), korisnik,
                r.getBrojstavki(), stvarni, r.getIznos(), false, r.getBrojstavki() == stvarni));
            model.put("stavke", racunStavkaFacade.findByRacun(id));
        } else {
            model.put("stavke", new ArrayList<>());
        }
    }

    private edu.unizg.foi.nwtis.Racun parsirajRacun(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(json, edu.unizg.foi.nwtis.Racun.class);
        } catch (Exception e) {
            System.out.println("Greška kod parsiranja računa: " + e.getMessage());
            return null;
        }
    }

    private List<edu.unizg.foi.nwtis.Racun> parsirajRacune(List<String> poruke) {
        List<edu.unizg.foi.nwtis.Racun> racuni = new ArrayList<>();
        for (String p : poruke) {
            edu.unizg.foi.nwtis.Racun r = parsirajRacun(p);
            if (r != null) {
                racuni.add(r);
            }
        }
        return racuni;
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
        try (Response odgovor = kontrolaRestClient.ulaznicaProvjeri(id)) {
            return odgovor.getStatus();
        } catch (Exception e) {
            return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        }
    }

    private void ponistiUlaznicu(String id) {
        try (Response odgovor = kontrolaRestClient.ulaznicaPonisti(id)) {
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
        try (Response odgovor = kontrolaRestClient.ulaznicaDodaj(korId)) {
            if (odgovor.getStatus() == Response.Status.CREATED.getStatusCode()
                && odgovor.hasEntity()) {
                Ulaznica ulaznica = odgovor.readEntity(Ulaznica.class);
                radneRezervacije.setIdUlaznice(ulaznica.id());
            }
        } catch (Exception _) {
        }
    }
}
