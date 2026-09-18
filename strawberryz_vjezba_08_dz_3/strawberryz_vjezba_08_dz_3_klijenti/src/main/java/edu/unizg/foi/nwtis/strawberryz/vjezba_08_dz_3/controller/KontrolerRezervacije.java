package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.controller;

import java.io.StringReader;
import java.net.URI;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.AvioTvrtka;
import edu.unizg.foi.nwtis.Korisnik;
import edu.unizg.foi.nwtis.Let;
import edu.unizg.foi.nwtis.LetPodaci;
import edu.unizg.foi.nwtis.RezervacijaSjedala;
import edu.unizg.foi.nwtis.StatusRezervacijeSjedala;
import edu.unizg.foi.nwtis.Ulaznica;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.RadneRezervacije;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Racun;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Racunstavka;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici.RacunFacade;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici.RacunStavkaFacade;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jms.PosiljateljJmsRedPoruka;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.mikroservisi.klijenti.AvioTvrtkeRestKlijent;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.rest.KontrolaResource;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.rest.RezervacijeResource;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.ws.WebSocketRezervacije;

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
@Path("rezervacije")
@RequestScoped
public class KontrolerRezervacije {

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
    private RadneRezervacije radneRezervacije;

    @Inject
    @RestClient
    private RezervacijeResource rezervacijeRestKlijent;

    @Inject
    @RestClient
    private AvioTvrtkeRestKlijent avioTvrtkeRestKlijent;

    @Inject
    @RestClient
    private KontrolaResource kontrolaResource;

    @Inject
    private RacunFacade racunFacade;

    @Inject
    private RacunStavkaFacade racunstavkaFacade;

    @Inject
    private KorisniciFacade korisniciFacade;

    @Inject
    private PosiljateljJmsRedPoruka posiljateljRedPoruka;

    @GET
    @Path("pocetak")
    @View("rezervacije/index.jsp")
    public void pocetak() {
        if (this.securityContext.getCallerPrincipal() != null) {
            var ime = this.securityContext.getCallerPrincipal().getName();
            model.put("korisnik", ime);
        }
        osigurajUlaznicu();
        model.put("posluziteljOdabran", radneRezervacije.isPosluziteljOdabran());
        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
    }

    @GET
    @Path("odabirPosluzitelja")
    @View("rezervacije/odabirPosluzitelja.jsp")
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
            boolean aktivan = provjeriHead(() -> rezervacijeRestKlijent.ping(id));
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
        }
        return "redirect:rezervacije/pocetak";
    }

    @GET
    @Path("pregledRacuna")
    @View("rezervacije/pregledRacuna.jsp")
    public String pregledRacuna() {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:rezervacije/odabirPosluzitelja";
        }

        String korisnik = this.securityContext.getCallerPrincipal() != null
            ? this.securityContext.getCallerPrincipal().getName() : null;

        List<RacunPrikaz> racuni = new ArrayList<>();
        boolean imaOtvoren = false;

        if (korisnik != null) {
            for (Racun r : racunFacade.findByKorisnik(korisnik)) {
                long stvarni = racunFacade.stvarniBrojStavki(r.getId());
                boolean otvoren = r.getIznos() == 0.0;
                boolean uskladjen = r.getBrojstavki() == stvarni;
                if (otvoren) {
                    imaOtvoren = true;
                }
                racuni.add(new RacunPrikaz(r.getId(), r.getVrijeme(), imePrezime(r, korisnik),
                    r.getBrojstavki(), stvarni, r.getIznos(), otvoren, uskladjen));
            }
        }

        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("korisnik", korisnik);
        model.put("racuni", racuni);
        model.put("mozeOtvoritiNovi", korisnik != null && !imaOtvoren);
        return null;
    }

    @GET
    @Path("pregledStavkiRacuna/{id}")
    @View("rezervacije/pregledStavkiRacuna.jsp")
    public String pregledStavkiRacuna(@PathParam("id") int id) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:rezervacije/odabirPosluzitelja";
        }
        String korisnik = this.securityContext.getCallerPrincipal() != null
            ? this.securityContext.getCallerPrincipal().getName() : null;

        Racun r = racunFacade.find(id);
        if (r == null || !pripadaKorisniku(r, korisnik)) {
            return "redirect:rezervacije/pregledRacuna";
        }

        long stvarni = racunFacade.stvarniBrojStavki(id);
        boolean otvoren = r.getIznos() == 0.0;
        boolean uskladjen = r.getBrojstavki() == stvarni;

        model.put("racun", new RacunPrikaz(r.getId(), r.getVrijeme(), imePrezime(r, korisnik),
            r.getBrojstavki(), stvarni, r.getIznos(), otvoren, uskladjen));
        model.put("stavke", racunstavkaFacade.findByRacun(id));
        model.put("mozeUskladiti", otvoren && !uskladjen);
        return null;
    }

    @GET
    @Path("uskladiRacun/{id}")
    public String uskladiRacun(@PathParam("id") int id) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:rezervacije/odabirPosluzitelja";
        }
        String korisnik = this.securityContext.getCallerPrincipal().getName();
        Racun r = racunFacade.find(id);
        if (r != null && pripadaKorisniku(r, korisnik) && r.getIznos() == 0.0) {
            racunFacade.uskladiBrojStavki(r);
        }
        return "redirect:rezervacije/pregledStavkiRacuna/" + id;
    }

    @GET
    @Path("otvoriRacun")
    public String otvoriRacun() {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:rezervacije/odabirPosluzitelja";
        }
        String korisnik = this.securityContext.getCallerPrincipal().getName();
        if (!racunFacade.imaOtvorenihRacuna(korisnik)) {
            Korisnici k = korisniciFacade.find(korisnik);
            if (k != null) {
                racunFacade.otvoriNoviRacun(k);
            }
        }
        return "redirect:rezervacije/pregledRacuna";
    }

    @GET
    @Path("zatvoriRacun/{id}")
    public String zatvoriRacun(@PathParam("id") int id) {
        osigurajUlaznicu();
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:rezervacije/odabirPosluzitelja";
        }
        String korisnik = this.securityContext.getCallerPrincipal().getName();
        Racun r = racunFacade.find(id);
        if (r != null && pripadaKorisniku(r, korisnik)) {
            long stvarni = racunFacade.stvarniBrojStavki(id);
            if (r.getIznos() == 0.0 && r.getBrojstavki() == stvarni) {
                Racun zatvoren = racunFacade.zatvoriRacun(r);
                posaljiRacunUQueue(zatvoren, korisnik);
                radneRezervacije.setRezervacije(new ArrayList<>());
            }
        }
        return "redirect:rezervacije/pregledRacuna";
    }

    @GET
    @Path("pregledRezervacija")
    @View("rezervacije/pregledRezervacija.jsp")
    public String pregledRezervacija(@QueryParam("odDatuma") String odDatuma,
                                      @QueryParam("doDatuma") String doDatuma) {
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:rezervacije/odabirPosluzitelja";
        }
        osigurajUlaznicu();

        String korisnik = this.securityContext.getCallerPrincipal() != null
            ? this.securityContext.getCallerPrincipal().getName() : null;
        model.put("korisnik", korisnik);
        model.put("odDatuma", odDatuma);
        model.put("doDatuma", doDatuma);

        List<RezervacijaSjedala> lista = List.of();

        if (korisnik != null && odDatuma != null && !odDatuma.isBlank()
                && doDatuma != null && !doDatuma.isBlank()) {
            int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
            String email = dajEmailKorisnika(radneRezervacije.getIdUlaznice());
            String putnik = email != null ? email : korisnik;
            try (Response odgovor = rezervacijeRestKlijent.ispisPutnik(
                    id, radneRezervacije.getIdUlaznice(), putnik, odDatuma, doDatuma)) {
                if (odgovor.getStatus() == 200) {
                    lista = parsirajRezervacije(odgovor.readEntity(String.class));
                }
            } catch (Exception e) {
                System.out.println("Greška kod dohvata rezervacija putnika: " + e.getMessage());
            }
        }

        model.put("rezervacije", lista);
        model.put("otvorenRacun", korisnik != null && racunFacade.imaOtvorenihRacuna(korisnik));
        return null;
    }

    @POST
    @Path("potvrdi/{idRezervacijeSjedala}")
    public Response potvrdiRezervaciju(
            @PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
            @FormParam("oznakaLeta") String oznakaLeta,
            @FormParam("idLeta") String idLeta,
            @FormParam("odDatuma") String odDatuma,
            @FormParam("doDatuma") String doDatuma) {

        osigurajUlaznicu();
        String korisnik = this.securityContext.getCallerPrincipal().getName();
        Racun otvoreniRacun = racunFacade.findOtvoreniRacun(korisnik);

        if (otvoreniRacun != null && radneRezervacije.isPosluziteljOdabran()) {
            int id = Integer.parseInt(radneRezervacije.getPosluzitelj());

            String email = dajEmailKorisnika(radneRezervacije.getIdUlaznice());
            String putnik = email != null ? email : korisnik;

            RezervacijaSjedala tijelo = new RezervacijaSjedala(
            	    idRezervacijeSjedala,
            	    null,
            	    putnik,
            	    null,
            	    StatusRezervacijeSjedala.KREIRANA);

            Response odgovor = rezervacijeRestKlijent.potvrdi(
                id, idRezervacijeSjedala, radneRezervacije.getIdUlaznice(), tijelo);

            if (odgovor.getStatus() == 200) {
                Korisnici k = korisniciFacade.find(korisnik);
                String ime     = k != null ? k.getIme()     : korisnik;
                String prezime = k != null ? k.getPrezime() : "";

                racunstavkaFacade.dodajStavku(otvoreniRacun, ime, prezime,
                    idRezervacijeSjedala, generirajIznos());

                WebSocketRezervacije.send(
                    idRezervacijeSjedala + "," + oznakaLeta + "," + idLeta
                    + "," + ime + " " + prezime);
            }
            odgovor.close();
        }

        return Response.seeOther(URI.create(
            "rezervacije/pregledRezervacija?odDatuma=" + odDatuma
            + "&doDatuma=" + doDatuma)).build();
    }

    @GET
    @Path("pregledAvioTvrtki")
    @View("rezervacije/pregledAvioTvrtki.jsp")
    public String pregledAvioTvrtki() {
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:rezervacije/odabirPosluzitelja";
        }
        osigurajUlaznicu();

        List<AvioTvrtka> tvrtke = List.of();
        Response odgovor = avioTvrtkeRestKlijent.dajSveAvioTvrtke();
        if (odgovor.getStatus() == 200) {
            tvrtke = parsirajAvioTvrtke(odgovor.readEntity(String.class));
        }
        odgovor.close();

        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("avioTvrtke", tvrtke);
        return null;
    }

    @GET
    @Path("pregledLetovaAvioTvrtke/{avioTvrtka}")
    @View("rezervacije/pregledLetovaAvioTvrtke.jsp")
    public String pregledLetovaAvioTvrtke(@PathParam("avioTvrtka") String avioTvrtka) {
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:rezervacije/odabirPosluzitelja";
        }
        osigurajUlaznicu();

        List<LetPodaci> letovi = List.of();
        int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
        Response odgovor = rezervacijeRestKlijent.ispisLetovi(
            id, avioTvrtka, radneRezervacije.getIdUlaznice());
        if (odgovor.getStatus() == 200) {
            letovi = parsirajLetove(odgovor.readEntity(String.class));
        }
        odgovor.close();

        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("avioTvrtka", avioTvrtka);
        model.put("letovi", letovi);
        return null;
    }

    @GET
    @Path("pregledLeta")
    @View("rezervacije/pregledLeta.jsp")
    public String pregledLeta(@QueryParam("oznakaLeta") String oznakaLeta,
            @QueryParam("datum") String datum,
            @QueryParam("vrijemePolijetanja") String vrijemePolijetanja,
            @QueryParam("polazniAerodrom") String polazniAerodrom,
            @QueryParam("odredisniAerodrom") String odredisniAerodrom) {
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:rezervacije/odabirPosluzitelja";
        }
        osigurajUlaznicu();

        String idLeta = null;
        if (oznakaLeta != null && datum != null && !datum.isBlank()) {
            int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
            Response odgovor = rezervacijeRestKlijent.ispisLet(
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
        return null;
    }

    @GET
    @Path("pregledSjedala/{idLeta}")
    @View("rezervacije/pregledSjedala.jsp")
    public String pregledSjedala(@PathParam("idLeta") String idLeta) {
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return "redirect:rezervacije/odabirPosluzitelja";
        }
        osigurajUlaznicu();

        List<RezervacijaSjedala> sjedala = List.of();
        int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
        Response odgovor = rezervacijeRestKlijent.ispisLeta(
            id, idLeta, radneRezervacije.getIdUlaznice());
        if (odgovor.getStatus() == 200) {
            sjedala = parsirajRezervacije(odgovor.readEntity(String.class));
        }
        odgovor.close();

        String korisnik = this.securityContext.getCallerPrincipal() != null
            ? this.securityContext.getCallerPrincipal().getName() : null;

        model.put("posluzitelj", radneRezervacije.getPosluzitelj());
        model.put("idLeta", idLeta);
        model.put("sjedala", sjedala);
        model.put("otvorenRacun", korisnik != null && racunFacade.imaOtvorenihRacuna(korisnik));
        return null;
    }

    @POST
    @Path("rezerviraj/{idLeta}")
    public Response rezervirajSjedalo(@PathParam("idLeta") String idLeta,
            @FormParam("razred") String razred) {
        if (!radneRezervacije.isPosluziteljOdabran()) {
            return Response.seeOther(URI.create("rezervacije/odabirPosluzitelja")).build();
        }
        osigurajUlaznicu();

        String korisnik = this.securityContext.getCallerPrincipal().getName();
        if (racunFacade.imaOtvorenihRacuna(korisnik)) {
            int id = Integer.parseInt(radneRezervacije.getPosluzitelj());
            String email = dajEmailKorisnika(radneRezervacije.getIdUlaznice());
            String putnik = email != null ? email : korisnik;
            Response odgovor = rezervacijeRestKlijent.rezerviraj(
                id, radneRezervacije.getIdUlaznice(), idLeta, razred, putnik);
            odgovor.close();
        }

        return Response.seeOther(URI.create("rezervacije/pregledSjedala/" + idLeta)).build();
    }

    private String dajEmailKorisnika(String idUlaznice) {
        if (idUlaznice == null || idUlaznice.isBlank()) {
            return null;
        }
        try (Response odgovor = kontrolaResource.ulaznicaKorisnik(idUlaznice)) {
            if (odgovor.getStatus() == Response.Status.OK.getStatusCode() && odgovor.hasEntity()) {
                Korisnik k = odgovor.readEntity(Korisnik.class);
                if (k != null && k.email() != null && !k.email().isBlank()) {
                    return k.email();
                }
            }
        } catch (Exception _) {
        }
        return null;
    }

    private boolean postojiRezervacija(Config config, int idx) {
        return config.getOptionalValue("rezervacije." + idx + ".adresa", String.class).isPresent();
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

    private boolean provjeriHead(java.util.function.Supplier<Response> poziv) {
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
            racunstavkaFacade.pretvori(racunstavkaFacade.findByRacun(racun.getId()));

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

    private List<AvioTvrtka> parsirajAvioTvrtke(String json) {
        if (json == null || json.isBlank()) return List.of();
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return Arrays.asList(jsonb.fromJson(json, AvioTvrtka[].class));
        } catch (Exception e) {
            System.out.println("Greška kod parsiranja avio tvrtki: " + e.getMessage());
            return List.of();
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

    public double generirajIznos() {
        double iznos = 10.0 + rnd.nextDouble() * (300.0 - 10.0);
        return Math.round(iznos * 100.0) / 100.0;
    }
}
