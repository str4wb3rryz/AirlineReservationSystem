package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.controller;

import java.net.URI;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.Korisnik;
import edu.unizg.foi.nwtis.Let;
import edu.unizg.foi.nwtis.LetPodaci;
import edu.unizg.foi.nwtis.LetSjedalo;
import edu.unizg.foi.nwtis.RazredSjedala;
import edu.unizg.foi.nwtis.RezervacijaSjedala;
import edu.unizg.foi.nwtis.StatusRezervacijeSjedala;
import edu.unizg.foi.nwtis.Ulaznica;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.RadneRezervacije;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Racun;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici.RacunFacade;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici.RacunStavkaFacade;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jms.PosiljateljJmsRedPoruka;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.mikroservisi.klijenti.AvioTvrtkeRestKlijent;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.mikroservisi.klijenti.KorisniciRestKlijent;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.rest.KontrolaResource;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.rest.RezervacijeResource;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.ws.WebSocketAvioTvrtke;
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
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Controller
@Path("aviotvrtke")
@RequestScoped
@RolesAllowed("nwtis")
public class KontrolerAvioTvrtke {

    public static class PosluziteljRezervacije {
        private final String id;
        private final String adresa;
        private final boolean aktivan;

        public PosluziteljRezervacije(String id, String adresa, boolean aktivan) {
            this.id = id;
            this.adresa = adresa;
            this.aktivan = aktivan;
        }

        public String getId()      { return id; }
        public String getAdresa()  { return adresa; }
        public boolean isAktivan() { return aktivan; }
    }

    public class PutnikPrikaz {

        private final String korisnik;
        private final String ime;
        private final String prezime;
        private final String email;
        private final boolean nasZastupnik;
        private final boolean zastupnikBiloKoje;

        public PutnikPrikaz(String korisnik, String ime, String prezime, String email,
                boolean nasZastupnik, boolean zastupnikBiloKoje) {
            this.korisnik = korisnik;
            this.ime = ime;
            this.prezime = prezime;
            this.email = email;
            this.nasZastupnik = nasZastupnik;
            this.zastupnikBiloKoje = zastupnikBiloKoje;
        }

        public String getKorisnik()           { return korisnik; }
        public String getIme()                { return ime; }
        public String getPrezime()            { return prezime; }
        public String getEmail()              { return email; }
        public boolean isNasZastupnik()       { return nasZastupnik; }
        public boolean isZastupnikBiloKoje()  { return zastupnikBiloKoje; }
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

    private final SecureRandom rnd = new SecureRandom();

    @Inject
    private Models model;

    @Inject
    private BindingResult bindingResult;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private RacunFacade racunFacade;

    @Inject
    private RacunStavkaFacade racunStavkaFacade;

    @Inject
    private KorisniciFacade korisniciFacade;

    @Inject
    private RadneRezervacije radneRezervacije;

    @Inject
    private PosiljateljJmsRedPoruka posiljateljRedPoruka;

    @Inject
    @RestClient
    private RezervacijeResource rezervacijeRestKlijent;

    @Inject
    @RestClient
    private KorisniciRestKlijent korisniciRestKlijent;

    @Inject
    @RestClient
    private AvioTvrtkeRestKlijent avioTvrtkeRestKlijent;

    @Inject
    @RestClient
    private KontrolaResource kontrolaResource;

    @GET
    @Path("pocetak")
    @View("aviotvrtke/index.jsp")
    public void pocetak() {
        if (this.securityContext.getCallerPrincipal() != null) {
            var ime = this.securityContext.getCallerPrincipal().getName();
            model.put("korisnik", ime);
        }
        osigurajUlaznicu();
        model.put("posluziteljOdabran", radneRezervacije.isPosluziteljOdabran());
        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
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

    @GET
    @Path("odabirPosluzitelja")
    @View("aviotvrtke/odabirPosluzitelja.jsp")
    public void odabirPosluzitelja() {
        osigurajUlaznicu();
        var config = ConfigProvider.getConfig();
        List<PosluziteljRezervacije> listaPosluzitelja = new ArrayList<>();

        var idx = 0;
        while (postojiRezervacija(config, idx)) {
            final var id = idx;
            String adresa = config
                .getOptionalValue("rezervacije." + idx + ".adresa", String.class)
                .orElse("N/A");
            boolean aktivan = provjeriHead(() -> rezervacijeRestKlijent.pingAvioTvrtke(id));
            listaPosluzitelja.add(new PosluziteljRezervacije(String.valueOf(idx), adresa, aktivan));
            idx++;
        }

        model.put("listaPosluzitelja", listaPosluzitelja);
        model.put("trenutniPosluzitelj", radneRezervacije.getPosluzitelj());
    }

    @POST
    @Path("odabirPosluzitelja")
    public String spremiPosluzitelja(@FormParam("posluzitelj") String posluzitelj) {
        osigurajUlaznicu();
        if (posluzitelj != null && !posluzitelj.isBlank()) {
            radneRezervacije.setPosluzitelj(posluzitelj);
            radneRezervacije.setRezervacije(new ArrayList<>());
            radneRezervacije.setPutnikKorisnickoIme(null);
            radneRezervacije.setPutnikIme(null);
            radneRezervacije.setPutnikPrezime(null);
        }
        return "redirect:aviotvrtke/pocetak";
    }

    @GET
    @Path("pregledRacuna")
    @View("aviotvrtke/pregledRacuna.jsp")
    public String pregledRacuna() {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }

        String zastupnik = this.securityContext.getCallerPrincipal() != null
            ? this.securityContext.getCallerPrincipal().getName() : null;

        List<RacunPrikaz> racuni = new ArrayList<>();
        boolean imaOtvoren = false;

        if (zastupnik != null) {
            for (Racun r : racunFacade.findByKorisnik(zastupnik)) {
                long stvarni = racunFacade.stvarniBrojStavki(r.getId());
                boolean otvoren = r.getIznos() == 0.0;
                boolean uskladjen = r.getBrojstavki() == stvarni;
                if (otvoren) {
                    imaOtvoren = true;
                }
                racuni.add(new RacunPrikaz(r.getId(), r.getVrijeme(), imePrezime(r, zastupnik),
                    r.getBrojstavki(), stvarni, r.getIznos(), otvoren, uskladjen));
            }
        }

        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("zastupnik", zastupnik);
        model.put("racuni", racuni);
        model.put("mozeOtvoritiNovi", zastupnik != null && !imaOtvoren);
        return null;
    }

    @GET
    @Path("pregledStavkiRacuna/{id}")
    @View("aviotvrtke/pregledStavkiRacuna.jsp")
    public String pregledStavkiRacuna(@PathParam("id") int id) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        String zastupnik = this.securityContext.getCallerPrincipal() != null
            ? this.securityContext.getCallerPrincipal().getName() : null;

        Racun r = racunFacade.find(id);
        if (r == null || !pripadaKorisniku(r, zastupnik)) {
            return "redirect:aviotvrtke/pregledRacuna";
        }

        long stvarni = racunFacade.stvarniBrojStavki(id);
        boolean otvoren = r.getIznos() == 0.0;
        boolean uskladjen = r.getBrojstavki() == stvarni;

        model.put("racun", new RacunPrikaz(r.getId(), r.getVrijeme(), imePrezime(r, zastupnik),
            r.getBrojstavki(), stvarni, r.getIznos(), otvoren, uskladjen));
        model.put("stavke", racunStavkaFacade.findByRacun(id));
        model.put("mozeUskladiti", otvoren && !uskladjen);
        return null;
    }

    @GET
    @Path("uskladiRacun/{id}")
    public String uskladiRacun(@PathParam("id") int id) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        String zastupnik = this.securityContext.getCallerPrincipal().getName();
        Racun r = racunFacade.find(id);
        if (r != null && pripadaKorisniku(r, zastupnik) && r.getIznos() == 0.0) {
            racunFacade.uskladiBrojStavki(r);
        }
        return "redirect:aviotvrtke/pregledStavkiRacuna/" + id;
    }

    @GET
    @Path("otvoriRacun")
    public String otvoriRacun() {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        String zastupnik = this.securityContext.getCallerPrincipal().getName();
        if (!racunFacade.imaOtvorenihRacuna(zastupnik)) {
            Korisnici k = korisniciFacade.find(zastupnik);
            if (k != null) {
                racunFacade.otvoriNoviRacun(k);
            }
        }
        return "redirect:aviotvrtke/pregledRacuna";
    }

    @GET
    @Path("zatvoriRacun/{id}")
    public String zatvoriRacun(@PathParam("id") int id) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        String zastupnik = this.securityContext.getCallerPrincipal().getName();
        Racun r = racunFacade.find(id);
        if (r != null && pripadaKorisniku(r, zastupnik)) {
            long stvarni = racunFacade.stvarniBrojStavki(id);
            if (r.getIznos() == 0.0 && r.getBrojstavki() == stvarni) {
                Racun zatvoren = racunFacade.zatvoriRacun(r);
                posaljiRacunUQueue(zatvoren, zastupnik);
                radneRezervacije.setRezervacije(new ArrayList<>());
            }
        }
        return "redirect:aviotvrtke/pregledRacuna";
    }

    @GET
    @Path("pregledPutnika")
    @View("aviotvrtke/pregledPutnika.jsp")
    public String pregledPutnika(@QueryParam("odStranice") Integer odStranice,
                                  @QueryParam("stranica") Integer stranica) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }

        int brojStranice = (odStranice != null && odStranice >= 1) ? odStranice : 1;
        int velicinaStranice = (stranica != null && stranica >= 1) ? stranica : 7;

        String zastupnik = this.securityContext.getCallerPrincipal() != null
            ? this.securityContext.getCallerPrincipal().getName() : null;
        String mojaTvrtka = dajAvioTvrtkuKorisnika(zastupnik);

        List<Korisnik> korisnici = List.of();
        Response odgovor = korisniciRestKlijent.dajKorisnikeStranicenje(brojStranice, velicinaStranice);
        if (odgovor.getStatus() == 200) {
            korisnici = parsirajKorisnike(odgovor.readEntity(String.class));
        }
        odgovor.close();

        List<PutnikPrikaz> putnici = new ArrayList<>();
        for (Korisnik k : korisnici) {
            String tvrtkaPutnika = dajAvioTvrtkuKorisnika(k.korisnik());
            boolean nasZastupnik = mojaTvrtka != null && mojaTvrtka.equals(tvrtkaPutnika);
            boolean zastupnikBiloKoje = tvrtkaPutnika != null;
            putnici.add(new PutnikPrikaz(k.korisnik(), k.ime(), k.prezime(), k.email(),
                nasZastupnik, zastupnikBiloKoje));
        }

        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("putnici", putnici);
        model.put("mojaTvrtka", mojaTvrtka);
        model.put("odStranice", brojStranice);
        model.put("stranica", velicinaStranice);
        model.put("imaPrethodna", brojStranice > 1);
        model.put("imaSljedeca", korisnici.size() == velicinaStranice);
        model.put("putnikOdabran", radneRezervacije.isPutnikOdabran());
        model.put("odabraniPutnik", radneRezervacije.getPutnikKorisnickoIme());
        return null;
    }

    @POST
    @Path("dodajZastupnika")
    public String dodajZastupnika(@FormParam("putnik") String putnik,
            @FormParam("odStranice") Integer odStranice, @FormParam("stranica") Integer stranica) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        String zastupnik = this.securityContext.getCallerPrincipal().getName();
        String mojaTvrtka = dajAvioTvrtkuKorisnika(zastupnik);
        if (mojaTvrtka != null && putnik != null && !putnik.isBlank()) {
            Response o = avioTvrtkeRestKlijent.dodajZastupnika(mojaTvrtka, putnik);
            o.close();
        }
        return preusmjeriNaPregledPutnika(odStranice, stranica);
    }

    @POST
    @Path("obrisiZastupnika")
    public String obrisiZastupnika(@FormParam("putnik") String putnik,
            @FormParam("odStranice") Integer odStranice, @FormParam("stranica") Integer stranica) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        String zastupnik = this.securityContext.getCallerPrincipal().getName();
        String mojaTvrtka = dajAvioTvrtkuKorisnika(zastupnik);
        if (mojaTvrtka != null && putnik != null && !putnik.isBlank()) {
            Response o = avioTvrtkeRestKlijent.obrisiZastupnika(mojaTvrtka, putnik);
            o.close();
        }
        return preusmjeriNaPregledPutnika(odStranice, stranica);
    }

    private String preusmjeriNaPregledPutnika(Integer odStranice, Integer stranica) {
        int brojStranice = (odStranice != null && odStranice >= 1) ? odStranice : 1;
        int velicinaStranice = (stranica != null && stranica >= 1) ? stranica : 7;
        return "redirect:aviotvrtke/pregledPutnika?odStranice=" + brojStranice
            + "&stranica=" + velicinaStranice;
    }

    @GET
    @Path("pregledLetovaPutnika")
    @View("aviotvrtke/pregledLetovaPutnika.jsp")
    public String pregledLetovaPutnika(@QueryParam("putnik") String putnik,
            @QueryParam("odDatuma") String odDatuma,
            @QueryParam("doDatuma") String doDatuma) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }

        List<RezervacijaSjedala> rezervacije = List.of();
        if (putnik != null && !putnik.isBlank() && odDatuma != null && !odDatuma.isBlank()
                && doDatuma != null && !doDatuma.isBlank()) {
            int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
            Response odgovor = rezervacijeRestKlijent.avioTvrtkeIspisPutnik(
                id, radneRezervacije.getIdUlaznice(), putnik, odDatuma, doDatuma);
            if (odgovor.getStatus() == 200) {
                rezervacije = parsirajRezervacije(odgovor.readEntity(String.class));
            }
            odgovor.close();
        }

        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("putnik", putnik);
        model.put("odDatuma", odDatuma);
        model.put("doDatuma", doDatuma);
        model.put("rezervacije", rezervacije);
        return null;
    }

    @POST
    @Path("odabirPutnika")
    public String odabirPutnika(@FormParam("putnikKorisnickoIme") String putnikKorisnickoIme) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }

        if (putnikKorisnickoIme != null && !putnikKorisnickoIme.isBlank()) {
            Korisnici k = korisniciFacade.find(putnikKorisnickoIme);
            radneRezervacije.setPutnikKorisnickoIme(putnikKorisnickoIme);
            radneRezervacije.setPutnikIme(k != null ? k.getIme() : "");
            radneRezervacije.setPutnikPrezime(k != null ? k.getPrezime() : "");
            radneRezervacije.setPutnikEmail(k != null ? k.getEmail() : "");
            radneRezervacije.setRezervacije(new ArrayList<>());
        }
        return "redirect:aviotvrtke/pregledPutnika";
    }

    @GET
    @Path("pregledLetova")
    @View("aviotvrtke/pregledLetova.jsp")
    public String pregledLetova() {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }

        List<LetPodaci> letovi = List.of();
        int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
        Response odgovor = rezervacijeRestKlijent.avioTvrtkeIspisLetovi(
            id, radneRezervacije.getIdUlaznice());
        if (odgovor.getStatus() == 200) {
            letovi = parsirajLetove(odgovor.readEntity(String.class));
        }
        odgovor.close();

        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("letovi", letovi);
        model.put("putnikOdabran", radneRezervacije.isPutnikOdabran());
        model.put("putnikKorisnickoIme", radneRezervacije.getPutnikKorisnickoIme());
        return null;
    }

    @GET
    @Path("pregledLetaAvioTvrtke")
    @View("aviotvrtke/pregledLetaAvioTvrtke.jsp")
    public String pregledLetaAvioTvrtke(@QueryParam("oznakaLeta") String oznakaLeta,
            @QueryParam("datum") String datum,
            @QueryParam("vrijemePolijetanja") String vrijemePolijetanja,
            @QueryParam("polazniAerodrom") String polazniAerodrom,
            @QueryParam("odredisniAerodrom") String odredisniAerodrom) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }

        String idLeta = null;
        if (oznakaLeta != null && datum != null && !datum.isBlank()) {
            int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
            Response odgovor = rezervacijeRestKlijent.avioTvrtkeIspisLet(
                id, radneRezervacije.getIdUlaznice(), oznakaLeta, datum);
            if (odgovor.getStatus() == 200) {
                Let let = parsirajLet(odgovor.readEntity(String.class));
                if (let != null) {
                    idLeta = let.id();
                }
            }
            odgovor.close();
        }

        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("oznakaLeta", oznakaLeta);
        model.put("datum", datum);
        model.put("vrijemePolijetanja", vrijemePolijetanja);
        model.put("polazniAerodrom", polazniAerodrom);
        model.put("odredisniAerodrom", odredisniAerodrom);
        model.put("idLeta", idLeta);
        model.put("putnikOdabran", radneRezervacije.isPutnikOdabran());
        return null;
    }

    @GET
    @Path("pregledSjedalaAvioTvrtke/{idLeta}")
    @View("aviotvrtke/pregledSjedalaAvioTvrtke.jsp")
    public String pregledSjedalaAvioTvrtke(@PathParam("idLeta") String idLeta) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }

        List<RezervacijaSjedala> sjedala = List.of();
        int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
        Response odgovor = rezervacijeRestKlijent.avioTvrtkeIspisLeta(
            id, idLeta, radneRezervacije.getIdUlaznice());
        if (odgovor.getStatus() == 200) {
            sjedala = parsirajRezervacije(odgovor.readEntity(String.class));
        }
        odgovor.close();

        String zastupnik = this.securityContext.getCallerPrincipal() != null
            ? this.securityContext.getCallerPrincipal().getName() : null;

        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("idLeta", idLeta);
        model.put("sjedala", sjedala);
        model.put("otvorenRacun", zastupnik != null && racunFacade.imaOtvorenihRacuna(zastupnik));
        model.put("putnikOdabran", radneRezervacije.isPutnikOdabran());
        model.put("putnikKorisnickoIme", radneRezervacije.getPutnikKorisnickoIme());
        return null;
    }

    @POST
    @Path("rezervirajSjedalo/{idLeta}")
    public Response rezervirajSjedalo(@PathParam("idLeta") String idLeta,
            @FormParam("razred") String razred,
            @FormParam("red") String red,
            @FormParam("oznakaSjedala") String oznakaSjedala,
            @FormParam("oznakaLeta") String oznakaLeta,
            @FormParam("datum") String datum,
            @FormParam("vrijemePolijetanja") String vrijemePolijetanja,
            @FormParam("polazniAerodrom") String polazniAerodrom,
            @FormParam("odredisniAerodrom") String odredisniAerodrom) {
        osigurajUlaznicu();

        if (!radneRezervacije.isPosluziteljOdabran()) {
            return Response.seeOther(URI.create("aviotvrtke/odabirPosluzitelja")).build();
        }

        String zastupnik = this.securityContext.getCallerPrincipal().getName();
        if (racunFacade.imaOtvorenihRacuna(zastupnik) && radneRezervacije.isPutnikOdabran()) {
            int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
            String putnik = radneRezervacije.getPutnikEmail();

            Response odgovor = rezervacijeRestKlijent.rezerviraj(
                id, radneRezervacije.getIdUlaznice(), idLeta, razred, putnik);
            if (odgovor.getStatus() == 201) {
                RezervacijaSjedala kreirana = parsirajJednuRezervaciju(
                    odgovor.readEntity(String.class));
                String idRezervacije = kreirana != null ? kreirana.id() : null;
                dodajURadneRezervacije(idRezervacije, idLeta, razred, red, oznakaSjedala,
                    oznakaLeta, datum, vrijemePolijetanja, polazniAerodrom, odredisniAerodrom,
                    putnik);
            }
            odgovor.close();
        }

        return Response.seeOther(
            URI.create("aviotvrtke/pregledSjedalaAvioTvrtke/" + idLeta)).build();
    }

    @POST
    @Path("dodajRezervaciju")
    public String dodajRezervaciju(@FormParam("idRezervacijeSjedala") String idRezervacijeSjedala,
                                    @FormParam("oznakaLeta") String oznakaLeta,
                                    @FormParam("idLeta") String idLeta) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        if (!radneRezervacije.isPutnikOdabran()) {
            return "redirect:aviotvrtke/pregledPutnika";
        }

        List<RezervacijaSjedala> lista = radneRezervacije.getRezervacije();

        boolean letVecPostoji = lista.stream()
        	    .anyMatch(r -> r.id() != null && r.id().equals(idRezervacijeSjedala));

        if (!letVecPostoji) {
            RezervacijaSjedala nova = new RezervacijaSjedala(
            	    idRezervacijeSjedala,
            	    null,
            	    radneRezervacije.getPutnikEmail(),
            	    null,
            	    StatusRezervacijeSjedala.KREIRANA);
            lista.add(nova);
        }

        return "redirect:aviotvrtke/pregledRezervacija";
    }

    @POST
    @Path("ukloniRezervaciju")
    public String ukloniRezervaciju(@FormParam("idRezervacijeSjedala") String idRezervacijeSjedala) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        radneRezervacije.getRezervacije()
            .removeIf(r -> idRezervacijeSjedala.equals(r.id()));
        return "redirect:aviotvrtke/pregledRezervacija";
    }

    @GET
    @Path("pregledRezervacija")
    @View("aviotvrtke/pregledRezervacija.jsp")
    public String pregledRezervacija() {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }

        String zastupnik = this.securityContext.getCallerPrincipal() != null
            ? this.securityContext.getCallerPrincipal().getName() : null;
        model.put("zastupnik", zastupnik);
        model.put("rezervacije", radneRezervacije.getRezervacije());
        model.put("otvorenRacun", zastupnik != null && racunFacade.imaOtvorenihRacuna(zastupnik));
        model.put("putnikOdabran", radneRezervacije.isPutnikOdabran());
        model.put("putnikKorisnickoIme", radneRezervacije.getPutnikKorisnickoIme());
        model.put("putnikIme", radneRezervacije.getPutnikIme());
        model.put("putnikPrezime", radneRezervacije.getPutnikPrezime());
        model.put("putnikEmail", radneRezervacije.getPutnikEmail());
        return null;
    }

    @POST
    @Path("potvrdi/{idRezervacijeSjedala}")
    public Response potvrdiRezervaciju(
            @PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
            @FormParam("oznakaLeta") String oznakaLeta,
            @FormParam("idLeta") String idLeta) {
        osigurajUlaznicu();

        String zastupnik = this.securityContext.getCallerPrincipal().getName();
        Racun otvoreniRacun = racunFacade.findOtvoreniRacun(zastupnik);

        if (otvoreniRacun != null && radneRezervacije.isPutnikOdabran()
                && radneRezervacije.isPosluziteljOdabran()) {

            int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
            RezervacijaSjedala tijelo = new RezervacijaSjedala(
                idRezervacijeSjedala, null,
                radneRezervacije.getPutnikEmail(), null, null);

            Response odgovor = rezervacijeRestKlijent.potvrdi(
                id, idRezervacijeSjedala, radneRezervacije.getIdUlaznice(), tijelo);

            if (odgovor.getStatus() == 200) {
                Korisnici z = korisniciFacade.find(zastupnik);

                racunStavkaFacade.dodajStavku(otvoreniRacun,
                    radneRezervacije.getPutnikIme(), radneRezervacije.getPutnikPrezime(),
                    idRezervacijeSjedala, generirajIznos());

                radneRezervacije.getRezervacije()
                    .removeIf(r -> idRezervacijeSjedala.equals(r.id()));

                String imeZastupnika = z != null ? z.getIme() + " " + z.getPrezime() : zastupnik;
                String imePutnika = radneRezervacije.getPutnikIme() + " "
                    + radneRezervacije.getPutnikPrezime();
                WebSocketAvioTvrtke.send(idRezervacijeSjedala + "," + oznakaLeta + ","
                    + idLeta + "," + imePutnika + "," + imeZastupnika);
            }
            odgovor.close();
        }

        return Response.seeOther(URI.create("aviotvrtke/pregledRezervacija")).build();
    }

    @GET
    @Path("pregledLetovaInicijalizacija")
    @View("aviotvrtke/pregledLetovaInicijalizacija.jsp")
    public String pregledLetovaInicijalizacija() {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        pripremiLetove();
        return null;
    }

    @POST
    @Path("inicijalizirajLet")
    @View("aviotvrtke/pregledLetovaInicijalizacija.jsp")
    public String inicijalizirajLet(@FormParam("oznakaLeta") String oznakaLeta,
            @FormParam("odDatuma") String odDatuma, @FormParam("doDatuma") String doDatuma) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        boolean uspjeh = false;
        if (oznakaLeta != null && !oznakaLeta.isBlank() && odDatuma != null && !odDatuma.isBlank()
                && doDatuma != null && !doDatuma.isBlank()) {
            int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
            Response o = rezervacijeRestKlijent.avioTvrtkeInicijalizirajLet(
                id, radneRezervacije.getIdUlaznice(), oznakaLeta, odDatuma, doDatuma, prazanLet());
            uspjeh = o.getStatus() == 201;
            o.close();
        }
        pripremiLetove();
        model.put("poruka", uspjeh ? "Let je inicijaliziran u zadanom intervalu datuma."
            : "Inicijalizacija leta nije uspjela.");
        return null;
    }

    @GET
    @Path("pregledStanjaLeta")
    @View("aviotvrtke/pregledStanjaLeta.jsp")
    public String pregledStanjaLeta(@QueryParam("oznakaLeta") String oznakaLeta,
            @QueryParam("datum") String datum,
            @QueryParam("vrijemePolijetanja") String vrijemePolijetanja,
            @QueryParam("polazniAerodrom") String polazniAerodrom,
            @QueryParam("odredisniAerodrom") String odredisniAerodrom) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        pripremiStanjeLeta(oznakaLeta, datum, vrijemePolijetanja, polazniAerodrom,
            odredisniAerodrom);
        return null;
    }

    @POST
    @Path("zatvoriRedoveLeta/{idLeta}")
    @View("aviotvrtke/pregledStanjaLeta.jsp")
    public String zatvoriRedoveLeta(@PathParam("idLeta") String idLeta,
            @FormParam("odReda") String odReda, @FormParam("doReda") String doReda,
            @FormParam("oznakaLeta") String oznakaLeta, @FormParam("datum") String datum,
            @FormParam("vrijemePolijetanja") String vrijemePolijetanja,
            @FormParam("polazniAerodrom") String polazniAerodrom,
            @FormParam("odredisniAerodrom") String odredisniAerodrom) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        int status = -1;
        if (idLeta != null && !idLeta.isBlank() && odReda != null && !odReda.isBlank()
                && doReda != null && !doReda.isBlank()) {
            int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
            Response o = rezervacijeRestKlijent.avioTvrtkeZatvoriLet(
                id, idLeta, radneRezervacije.getIdUlaznice(), odReda, doReda, prazanLet());
            status = o.getStatus();
            o.close();
        }
        pripremiStanjeLeta(oznakaLeta, datum, vrijemePolijetanja, polazniAerodrom,
            odredisniAerodrom);
        String poruka;
        if (status == 200) {
            poruka = "Sjedala su zatvorena u zadanom intervalu redova.";
        } else if (status == Response.Status.CONFLICT.getStatusCode()) {
            poruka = "Zatvaranje nije moguće jer u zadanim redovima postoje kreirane ili "
                + "potvrđene rezervacije sjedala.";
        } else {
            poruka = "Zatvaranje sjedala nije uspjelo.";
        }
        model.put("poruka", poruka);
        return null;
    }

    @GET
    @Path("zatvaranjeSjedala")
    @View("aviotvrtke/zatvaranjeSjedala.jsp")
    public String zatvaranjeSjedala() {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        return null;
    }

    @POST
    @Path("zatvaranjeSjedala")
    @View("aviotvrtke/zatvaranjeSjedala.jsp")
    public String zatvaranjeSjedalaAkcija(@FormParam("odDatuma") String odDatuma,
            @FormParam("doDatuma") String doDatuma) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        boolean uspjeh = false;
        if (odDatuma != null && !odDatuma.isBlank() && doDatuma != null && !doDatuma.isBlank()) {
            int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
            Response o = rezervacijeRestKlijent.avioTvrtkeZatvori(
                id, radneRezervacije.getIdUlaznice(), odDatuma, doDatuma, prazanLet());
            uspjeh = o.getStatus() == 200;
            o.close();
        }
        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("odDatuma", odDatuma);
        model.put("doDatuma", doDatuma);
        model.put("poruka", uspjeh ? "Sjedala su zatvorena u zadanom intervalu datuma."
            : "Zatvaranje sjedala nije uspjelo.");
        return null;
    }

    @GET
    @Path("inicijalizacijaLetova")
    @View("aviotvrtke/inicijalizacijaLetova.jsp")
    public String inicijalizacijaLetova() {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        return null;
    }

    @POST
    @Path("inicijalizacijaLetova")
    @View("aviotvrtke/inicijalizacijaLetova.jsp")
    public String inicijalizacijaLetovaAkcija(@FormParam("odDatuma") String odDatuma,
            @FormParam("doDatuma") String doDatuma) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:aviotvrtke/odabirPosluzitelja";
        }
        boolean uspjeh = false;
        if (odDatuma != null && !odDatuma.isBlank() && doDatuma != null && !doDatuma.isBlank()) {
            int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
            Response o = rezervacijeRestKlijent.avioTvrtkeInicijaliziraj(
                id, radneRezervacije.getIdUlaznice(), odDatuma, doDatuma, prazanLet());
            uspjeh = o.getStatus() == 201;
            o.close();
        }
        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("odDatuma", odDatuma);
        model.put("doDatuma", doDatuma);
        model.put("poruka", uspjeh ? "Letovi su inicijalizirani u zadanom intervalu datuma."
            : "Inicijalizacija letova nije uspjela.");
        return null;
    }

    private boolean postojiRezervacija(Config config, int idx) {
        return config.getOptionalValue("rezervacije." + idx + ".adresa", String.class).isPresent();
    }

    private boolean pripadaKorisniku(Racun r, String korisnik) {
        return korisnik != null && r.getKorisnici() != null
            && korisnik.equals(r.getKorisnici().getKorisnik());
    }

    private String imePrezime(Racun r, String korisnik) {
        Korisnici k = r.getKorisnici();
        if (k != null) {
            return k.getIme() + " " + k.getPrezime();
        }
        return korisnik;
    }

    private void posaljiRacunUQueue(Racun racun, String korisnik) {
        List<edu.unizg.foi.nwtis.RacunStavka> stavke =
            racunStavkaFacade.pretvori(racunStavkaFacade.findByRacun(racun.getId()));

        edu.unizg.foi.nwtis.Racun zapis = new edu.unizg.foi.nwtis.Racun(
            racun.getId(),
            racun.getVrijeme() != null ? racun.getVrijeme().toLocalDateTime() : null,
            racun.getKorisnici() != null ? racun.getKorisnici().getKorisnik() : korisnik,
            racun.getBrojstavki(),
            racun.getIznos(),
            stavke);

        try (Jsonb jsonb = JsonbBuilder.create()) {
            posiljateljRedPoruka.novaPoruka(jsonb.toJson(zapis));
        } catch (Exception e) {
            System.out.println("Greška kod slanja računa u red poruka: " + e.getMessage());
        }
    }

    private String dajAvioTvrtkuKorisnika(String korisnik) {
        if (korisnik == null || korisnik.isBlank()) {
            return null;
        }
        try {
            Response o = korisniciRestKlijent.dajAviotvrtku(korisnik);
            try {
                if (o.getStatus() == 200) {
                    String t = ocistiString(o.readEntity(String.class));
                    return (t == null || t.isBlank()) ? null : t;
                }
                return null;
            } finally {
                o.close();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String ocistiString(String s) {
        if (s == null) {
            return null;
        }
        String r = s.trim();
        if (r.startsWith("OK")) {
            r = r.substring(2).trim();
        }
        if (r.length() >= 2 && r.startsWith("\"") && r.endsWith("\"")) {
            r = r.substring(1, r.length() - 1);
        }
        return r.trim();
    }

    private void dodajURadneRezervacije(String idRezervacije, String idLeta, String razred,
            String red, String oznakaSjedala, String oznakaLeta, String datum,
            String vrijemePolijetanja, String polazniAerodrom, String odredisniAerodrom,
            String putnik) {
        if (idRezervacije == null || idRezervacije.isBlank()) {
            return;
        }
        List<RezervacijaSjedala> lista = radneRezervacije.getRezervacije();

        boolean letVecPostoji = lista.stream().anyMatch(
            r -> r.sjedalo() != null && r.sjedalo().let() != null
                && idLeta.equals(r.sjedalo().let().id()));
        if (letVecPostoji) {
            return;
        }

        LetPodaci letPodaci = new LetPodaci(oznakaLeta, null, parsirajVrijeme(vrijemePolijetanja),
            polazniAerodrom, odredisniAerodrom, null);
        Let let = new Let(idLeta, letPodaci, parsirajDatum(datum));
        RazredSjedala razredSjedala = parsirajRazred(razred);
        int redBroj = parsirajInt(red);
        LetSjedalo letSjedalo = new LetSjedalo(let, razredSjedala, redBroj, oznakaSjedala);

        RezervacijaSjedala nova = new RezervacijaSjedala(idRezervacije, letSjedalo, putnik,
            LocalDateTime.now(), StatusRezervacijeSjedala.KREIRANA);
        lista.add(nova);
    }

    private LocalTime parsirajVrijeme(String s) {
        try {
            return (s == null || s.isBlank()) ? null : LocalTime.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parsirajDatum(String s) {
        try {
            return (s == null || s.isBlank()) ? null : LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private RazredSjedala parsirajRazred(String s) {
        try {
            return (s == null || s.isBlank()) ? null : RazredSjedala.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    private int parsirajInt(String s) {
        try {
            return (s == null || s.isBlank()) ? 0 : Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private List<Korisnik> parsirajKorisnike(String json) {
        if (json == null || json.isBlank()) return List.of();
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return Arrays.asList(jsonb.fromJson(json, Korisnik[].class));
        } catch (Exception e) {
            System.out.println("Greška kod parsiranja korisnika: " + e.getMessage());
            return List.of();
        }
    }

    private List<RezervacijaSjedala> parsirajRezervacije(String json) {
        if (json == null || json.isBlank()) return List.of();
        String cisti = json.startsWith("OK") ? json.substring(2).trim() : json;
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return Arrays.asList(jsonb.fromJson(cisti, RezervacijaSjedala[].class));
        } catch (Exception e) {
            System.out.println("Greška kod parsiranja rezervacija: " + e.getMessage());
            return List.of();
        }
    }

    private RezervacijaSjedala parsirajJednuRezervaciju(String json) {
        if (json == null || json.isBlank()) return null;
        String cisti = json.startsWith("OK") ? json.substring(2).trim() : json;
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(cisti, RezervacijaSjedala.class);
        } catch (Exception e) {
            System.out.println("Greška kod parsiranja rezervacije: " + e.getMessage());
            return null;
        }
    }

    private List<LetPodaci> parsirajLetove(String json) {
        if (json == null || json.isBlank()) return List.of();
        String cisti = json.startsWith("OK") ? json.substring(2).trim() : json;
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return Arrays.asList(jsonb.fromJson(cisti, LetPodaci[].class));
        } catch (Exception e) {
            System.out.println("Greška kod parsiranja letova: " + e.getMessage());
            return List.of();
        }
    }

    private Let parsirajLet(String json) {
        if (json == null || json.isBlank()) return null;
        String cisti = json.startsWith("OK") ? json.substring(2).trim() : json;
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(cisti, Let.class);
        } catch (Exception e) {
            System.out.println("Greška kod parsiranja leta: " + e.getMessage());
            return null;
        }
    }

    private void pripremiLetove() {
        List<LetPodaci> letovi = List.of();
        int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
        Response o = rezervacijeRestKlijent.avioTvrtkeIspisLetovi(id, radneRezervacije.getIdUlaznice());
        if (o.getStatus() == 200) {
            letovi = parsirajLetove(o.readEntity(String.class));
        }
        o.close();
        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("letovi", letovi);
    }

    private void pripremiStanjeLeta(String oznakaLeta, String datum, String vrijemePolijetanja,
            String polazniAerodrom, String odredisniAerodrom) {
        int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
        String idLeta = null;
        int[] stanje = null;
        if (oznakaLeta != null && datum != null && !datum.isBlank()) {
            Response o1 = rezervacijeRestKlijent.avioTvrtkeIspisLet(
                id, radneRezervacije.getIdUlaznice(), oznakaLeta, datum);
            if (o1.getStatus() == 200) {
                Let let = parsirajLet(o1.readEntity(String.class));
                if (let != null) {
                    idLeta = let.id();
                }
            }
            o1.close();
            if (idLeta != null) {
                Response o2 = rezervacijeRestKlijent.avioTvrtkeStanje(
                    id, idLeta, radneRezervacije.getIdUlaznice());
                if (o2.getStatus() == 200) {
                    stanje = parsirajStanje(o2.readEntity(String.class));
                }
                o2.close();
            }
        }

        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("oznakaLeta", oznakaLeta);
        model.put("datum", datum);
        model.put("vrijemePolijetanja", vrijemePolijetanja);
        model.put("polazniAerodrom", polazniAerodrom);
        model.put("odredisniAerodrom", odredisniAerodrom);
        model.put("idLeta", idLeta);
        model.put("imaStanje", stanje != null);
        if (stanje != null) {
            model.put("brojSlobodna", stanje[0]);
            model.put("brojKreirana", stanje[1]);
            model.put("brojPotvrdena", stanje[2]);
            model.put("brojZatvorena", stanje[3]);
            model.put("brojNevazeca", stanje[4]);
        }
    }

    private int[] parsirajStanje(String odgovor) {
        if (odgovor == null) {
            return null;
        }
        String s = odgovor.trim();
        if (s.startsWith("OK")) {
            s = s.substring(2).trim();
        }
        String[] d = s.split("\\s+");
        if (d.length < 5) {
            return null;
        }
        try {
            return new int[] {Integer.parseInt(d[0]), Integer.parseInt(d[1]),
                Integer.parseInt(d[2]), Integer.parseInt(d[3]), Integer.parseInt(d[4])};
        } catch (Exception e) {
            return null;
        }
    }

    private Let prazanLet() {
        return new Let("", null, null);
    }

    private boolean provjeriHead(Supplier<Response> poziv) {
        try {
            Response r = poziv.get();
            try {
                return r.getStatus() >= 200 && r.getStatus() < 300;
            } finally {
                r.close();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public double generirajIznos() {
        double iznos = 10.0 + rnd.nextDouble() * (300.0 - 10.0);
        return Math.round(iznos * 100.0) / 100.0;
    }
}
