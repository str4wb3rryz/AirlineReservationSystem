package edu.unizg.foi.nwtis.strawberryz.vjezba_07_dz_2.mikroservisi.resource;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import edu.unizg.foi.nwtis.strawberryz.vjezba_07_dz_2.mikroservisi.pomocnici.KlijentMrezneUticnice;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("rezervacije")
public class RezervacijeResource {

  private List<String> adresa = new ArrayList<>();

  private List<String> mreznaVrata = new ArrayList<>();

  private List<String> mreznaVrataAvioTvrtke = new ArrayList<>();

  private List<String> mreznaVrataAdministracija = new ArrayList<>();

  private List<String> kodZaKraj = new ArrayList<>();

  @Inject
  KlijentMrezneUticnice klijentMrezneUticnice;

  @Inject
  @ConfigProperty(name = "mikroservisi.kontrola/mp-rest/url")
  private String mikroservisKontrolaUrl;

  @Inject
  @ConfigProperty(name = "servisi.korisnici/mp-rest/url")
  private String servisKorisniciUrl;

  @PostConstruct
  private void ucitajConfig() {
    Config config = ConfigProvider.getConfig();
    try {
      for (int i = 0;; i++) {
        this.adresa.add(config.getValue("rezervacije." + i + ".adresa", String.class));
        this.mreznaVrata
            .add(config.getValue("rezervacije." + i + ".mreznaVrata", String.class));
        this.mreznaVrataAvioTvrtke
            .add(config.getValue("rezervacije." + i + ".mreznaVrataAvioTvrtke", String.class));
        this.mreznaVrataAdministracija
            .add(config.getValue("rezervacije." + i + ".mreznaVrataAdministracija",
                String.class));
        this.kodZaKraj
            .add(config.getValue("rezervacije." + i + ".kodZaKraj", String.class));
      }
    } catch (Exception _) {
    }
  }

  private String dajEmailKorisnika(String idUlaznice) {
    Client client = ClientBuilder.newClient();
    var odgovor = client
        .target(this.mikroservisKontrolaUrl + "/ulaznica/korisnik/" + idUlaznice)
        .request(MediaType.APPLICATION_JSON)
        .get();
    if (odgovor.getStatus() != Response.Status.OK.getStatusCode()) {
      return null;
    }
    String tijelo = odgovor.readEntity(String.class);
    if (tijelo == null) {
      return null;
    }
    if (tijelo.startsWith("OK ")) {
      return tijelo.substring(3).trim();
    }
    return tijelo.trim();
  }

  private String dajAvioTvrtku(String email) {
    Client client = ClientBuilder.newClient();
    var odgovor = client
        .target(this.servisKorisniciUrl + "/email/" + email + "/aviotvrtka")
        .request(MediaType.APPLICATION_JSON)
        .get();
    if (odgovor.getStatus() != Response.Status.OK.getStatusCode()) {
      return null;
    }
    return odgovor.readEntity(String.class);
  }

  private boolean jeValidanId(int id) {
    return id >= 0 && id < this.adresa.size();
  }

  @Path("{id}/ping")
  @HEAD
  @Operation(summary = "Provjera rada PoslužiteljRezervacije slanjem PING komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji poslužitelj"),
      @APIResponse(responseCode = "503", description = "Nedostupan servis")})
  @Counted(name = "rezervacijePingCount", description = "Broj poziva ping - HEAD")
  @Timed(name = "rezervacijePingTime", description = "Vrijeme izvršavanja ping - HEAD")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskePing")
  public Response ping(@PathParam("id") int id) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "PING", this.adresa.get(id), Integer.parseInt(this.mreznaVrata.get(id)));
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @Path("{id}/pauza/{milisek}")
  @HEAD
  @Operation(summary = "Pokreće pauzu slanjem PAUZA komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji poslužitelj"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijePauzaCount", description = "Broj poziva pauza - HEAD")
  @Timed(name = "rezervacijePauzaTime", description = "Vrijeme izvršavanja pauza - HEAD")
  @Timeout(10000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "metodaKodPogreskePauza")
  public Response pauza(@PathParam("id") int id, @PathParam("milisek") String milisek) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "PAUZA " + milisek,
        this.adresa.get(id), Integer.parseInt(this.mreznaVrata.get(id)));
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @Path("{id}/{idRezervacijeSjedala}")
  @GET
  @Operation(summary = "Dohvaća rezervaciju sjedala slanjem ISPIS komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno dohvaćeno"),
      @APIResponse(responseCode = "404", description = "Ne postoji rezervacija"),
      @APIResponse(responseCode = "409", description = "Neispravni podaci"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeIspisJedneCount", description = "Broj poziva ispis jedne rezervacije - GET")
  @Timed(name = "rezervacijeIspisJedneTime", description = "Vrijeme izvršavanja ispisa jedne rezervacije - GET")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeIspisSjedala")
  public Response ispisSjedala(
      @PathParam("id") int id,
      @PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
      @HeaderParam("idUlaznice") String idUlaznice) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    List<Integer> redoslijed = new ArrayList<>();
    redoslijed.add(id);
    for (int i = 0; i < this.adresa.size(); i++) {
      if (i != id) {
        redoslijed.add(i);
      }
    }
    for (int indeks : redoslijed) {
      var odgovor = klijentMrezneUticnice.posaljiKomandu(
          "ISPIS " + idUlaznice + " " + idRezervacijeSjedala,
          this.adresa.get(indeks), Integer.parseInt(this.mreznaVrata.get(indeks)));
      if (odgovor == null) {
        continue;
      }
      if (odgovor.startsWith("OK")) {
        return Response.ok().entity(odgovor).type(MediaType.APPLICATION_JSON).build();
      }
      if (odgovor.startsWith("ERROR 20")) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      if (odgovor.startsWith("ERROR 21")) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @Path("{id}/ispis/putnik")
  @GET
  @Operation(summary = "Dohvaća rezervacije putnika u zadanom razdoblju slanjem ISPIS komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno dohvaćeno"),
      @APIResponse(responseCode = "404", description = "Ne postoje rezervacije"),
      @APIResponse(responseCode = "409", description = "Neispravni podaci"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeIspisPutnikCount", description = "Broj poziva ispis putnik - GET")
  @Timed(name = "rezervacijeIspisPutnikTime", description = "Vrijeme izvršavanja ispis putnik - GET")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeIspisPutnik")
  public Response ispisPutnik(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("putnik") String putnik,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ISPIS " + idUlaznice + " " + putnik + " " + odDatuma + " " + doDatuma,
        this.adresa.get(id), Integer.parseInt(this.mreznaVrata.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().entity(odgovor).type(MediaType.APPLICATION_JSON).build();
    }
    if (odgovor.startsWith("ERROR 20")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    if (odgovor.startsWith("ERROR 21") || odgovor.startsWith("ERROR 24")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("{id}/ispis/let")
  @GET
  @Operation(summary = "Dohvaća id leta slanjem ISPIS komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno dohvaćeno"),
      @APIResponse(responseCode = "404", description = "Ne postoji let"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeIspisLetCount", description = "Broj poziva ispis let - GET")
  @Timed(name = "rezervacijeIspisLetTime", description = "Vrijeme izvršavanja ispis let - GET")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeIspisLet")
  public Response ispisLet(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("oznakaLeta") String oznakaLeta,
      @HeaderParam("datum") String datum) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ISPIS " + idUlaznice + " \"" + oznakaLeta + "\" " + datum,
        this.adresa.get(id), Integer.parseInt(this.mreznaVrata.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().entity(odgovor).type(MediaType.APPLICATION_JSON).build();
    }
    if (odgovor.startsWith("ERROR 24")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("{id}/rezerviraj")
  @POST
  @Operation(summary = "Rezervira sjedalo slanjem REZERVIRAJ komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "201", description = "Uspješno rezervirano"),
      @APIResponse(responseCode = "404", description = "Ne postoji let ili putnik"),
      @APIResponse(responseCode = "409", description = "Nema slobodnih sjedala"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeRezervacijaCount", description = "Broj poziva rezerviraj - POST")
  @Timed(name = "rezervacijeRezervacijaTime", description = "Vrijeme izvršavanja rezerviraj - POST")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeRezerviraj")
  public Response rezerviraj(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("idLeta") String idLeta,
      @HeaderParam("putnik") String putnik,
      @HeaderParam("razred") String razred) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "REZERVIRAJ " + idUlaznice + " " + idLeta + " " + putnik + " " + razred,
        this.adresa.get(id), Integer.parseInt(this.mreznaVrata.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.status(Response.Status.CREATED).entity(odgovor).build();
    }
    if (odgovor.startsWith("ERROR 20") || odgovor.startsWith("ERROR 23")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    if (odgovor.startsWith("ERROR 21")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    if (odgovor.startsWith("ERROR 22")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("{id}/{idRezervacijeSjedala}")
  @PUT
  @Operation(summary = "Potvrđuje rezervaciju sjedala slanjem POTVRDI komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno potvrđeno"),
      @APIResponse(responseCode = "404", description = "Ne postoji rezervacija"),
      @APIResponse(responseCode = "409", description = "Neispravni podaci"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijePotvrdiCount", description = "Broj poziva potvrdi - PUT")
  @Timed(name = "rezervacijePotvrdiTime", description = "Vrijeme izvršavanja potvrdi - PUT")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskePotvrdi")
  public Response potvrdi(
      @PathParam("id") int id,
      @PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("putnik") String putnik) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    List<Integer> redoslijed = new ArrayList<>();
    redoslijed.add(id);
    for (int i = 0; i < this.adresa.size(); i++) {
      if (i != id) {
        redoslijed.add(i);
      }
    }
    for (int indeks : redoslijed) {
      var odgovor = klijentMrezneUticnice.posaljiKomandu(
          "POTVRDI " + idUlaznice + " " + idRezervacijeSjedala + " " + putnik,
          this.adresa.get(indeks), Integer.parseInt(this.mreznaVrata.get(indeks)));
      if (odgovor == null) {
        continue;
      }
      if (odgovor.startsWith("OK")) {
        return Response.ok().build();
      }
      if (odgovor.startsWith("ERROR 20") || odgovor.startsWith("ERROR 23")) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      if (odgovor.startsWith("ERROR 21")) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @Path("{id}/rezerviraj/grupa")
  @POST
  @Operation(summary = "Rezervira sjedala za grupu putnika slanjem REZERVIRAJ GRUPA komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "201", description = "Uspješno rezervirano"),
      @APIResponse(responseCode = "404", description = "Ne postoji let"),
      @APIResponse(responseCode = "409", description = "Nema slobodnih sjedala"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeRezervacijaGrupaCount",
      description = "Broj poziva rezerviraj/grupa - POST")
  @Timed(name = "rezervacijeRezervacijaGrupaTime",
      description = "Vrijeme izvršavanja rezerviraj/grupa - POST")
  @Timeout(5000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeRezervirajGrupa")
  public Response rezervirajGrupa(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("idLeta") String idLeta,
      @HeaderParam("putnici") String putnici,
      @HeaderParam("razred") String razred) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "REZERVIRAJ GRUPA " + idUlaznice + " " + idLeta
            + " [" + putnici + "] " + razred,
        this.adresa.get(id), Integer.parseInt(this.mreznaVrata.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.status(Response.Status.CREATED).entity(odgovor).build();
    }
    if (odgovor.startsWith("ERROR 20") || odgovor.startsWith("ERROR 23")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    if (odgovor.startsWith("ERROR 21") || odgovor.startsWith("ERROR 22")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("{id}/potvrdi/grupa")
  @PUT
  @Operation(summary = "Potvrđuje rezervacije za grupu putnika slanjem POTVRDI za svakoga")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno potvrđeno"),
      @APIResponse(responseCode = "404", description = "Ne postoji rezervacija"),
      @APIResponse(responseCode = "409", description = "Neispravni podaci"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijePotvrdiGrupaCount",
      description = "Broj poziva potvrdi/grupa - PUT")
  @Timed(name = "rezervacijePotvrdiGrupaTime",
      description = "Vrijeme izvršavanja potvrdi/grupa - PUT")
  @Timeout(5000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskePotvrdiGrupa")
  public Response potvrdiGrupa(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("rezervacijeSjedala") String rezervacijeSjedala,
      @HeaderParam("putnici") String putnici) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    String[] listaRezervacija = rezervacijeSjedala.trim().split(" ");
    String[] listaPutnika = putnici.trim().split(" ");
    if (listaRezervacija.length != listaPutnika.length) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    for (int i = 0; i < listaRezervacija.length; i++) {
      var odgovor = klijentMrezneUticnice.posaljiKomandu(
          "POTVRDI " + idUlaznice + " " + listaRezervacija[i] + " " + listaPutnika[i],
          this.adresa.get(id), Integer.parseInt(this.mreznaVrata.get(id)));
      if (odgovor == null || !odgovor.startsWith("OK")) {
        return Response.status(Response.Status.CONFLICT).build();
      }
    }
    return Response.ok().build();
  }

  @Path("aviotvrtke/{id}/ping")
  @HEAD
  @Operation(summary = "Provjera rada PoslužiteljRezervacije (avio tvrtke) slanjem PING komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji poslužitelj"),
      @APIResponse(responseCode = "503", description = "Nedostupan servis")})
  @Counted(name = "rezervacijeAvioTvrtkePingCount",
      description = "Broj poziva aviotvrtke ping - HEAD")
  @Timed(name = "rezervacijeAvioTvrtkePingTime",
      description = "Vrijeme izvršavanja aviotvrtke ping - HEAD")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAvioTvrtkePing")
  public Response avioTvrtkePing(@PathParam("id") int id) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "PING", this.adresa.get(id),
        Integer.parseInt(this.mreznaVrataAvioTvrtke.get(id)));
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @Path("aviotvrtke/{id}/ispis/putnik")
  @GET
  @Operation(summary = "Dohvaća rezervacije putnika avio tvrtke zastupnika slanjem ISPIS komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno dohvaćeno"),
      @APIResponse(responseCode = "401", description = "Neovlašteni pristup"),
      @APIResponse(responseCode = "404", description = "Ne postoje rezervacije"),
      @APIResponse(responseCode = "409", description = "Neispravni podaci"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeAvioTvrtkeIspisPutnikCount",
      description = "Broj poziva aviotvrtke ispis putnik - GET")
  @Timed(name = "rezervacijeAvioTvrtkeIspisPutnikTime",
      description = "Vrijeme izvršavanja aviotvrtke ispis putnik - GET")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAvioTvrtkeIspisPutnik")
  public Response avioTvrtkeIspisPutnik(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("putnik") String putnik,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    String email = dajEmailKorisnika(idUlaznice);
    if (email == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String avioTvrtka = dajAvioTvrtku(email);
    if (avioTvrtka == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ISPIS " + idUlaznice + " " + avioTvrtka + " " + putnik
            + " " + odDatuma + " " + doDatuma,
        this.adresa.get(id),
        Integer.parseInt(this.mreznaVrataAvioTvrtke.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().entity(odgovor).type(MediaType.APPLICATION_JSON).build();
    }
    if (odgovor.startsWith("ERROR 20")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    if (odgovor.startsWith("ERROR 21") || odgovor.startsWith("ERROR 24")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("aviotvrtke/{id}/ispis/letovi")
  @GET
  @Operation(summary = "Dohvaća letove avio tvrtke zastupnika slanjem ISPIS LETOVI komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno dohvaćeno"),
      @APIResponse(responseCode = "401", description = "Neovlašteni pristup"),
      @APIResponse(responseCode = "404", description = "Ne postoje letovi"),
      @APIResponse(responseCode = "409", description = "Neispravni podaci"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeAvioTvrtkeIspisLetoviCount",
      description = "Broj poziva aviotvrtke ispis letovi - GET")
  @Timed(name = "rezervacijeAvioTvrtkeIspisLetoviTime",
      description = "Vrijeme izvršavanja aviotvrtke ispis letovi - GET")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAvioTvrtkeIspisLetovi")
  public Response avioTvrtkeIspisLetovi(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    String email = dajEmailKorisnika(idUlaznice);
    if (email == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String avioTvrtka = dajAvioTvrtku(email);
    if (avioTvrtka == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ISPIS LETOVI " + idUlaznice + " " + avioTvrtka,
        this.adresa.get(id),
        Integer.parseInt(this.mreznaVrataAvioTvrtke.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().entity(odgovor).type(MediaType.APPLICATION_JSON).build();
    }
    if (odgovor.startsWith("ERROR 21") || odgovor.startsWith("ERROR 24")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("aviotvrtke/{id}/stanje/{idLeta}")
  @GET
  @Operation(summary = "Dohvaća stanje sjedala na letu slanjem STANJE komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno dohvaćeno"),
      @APIResponse(responseCode = "404", description = "Ne postoji let"),
      @APIResponse(responseCode = "409", description = "Neispravni podaci"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeAvioTvrtkeStanjeCount",
      description = "Broj poziva aviotvrtke stanje - GET")
  @Timed(name = "rezervacijeAvioTvrtkeStanjeTime",
      description = "Vrijeme izvršavanja aviotvrtke stanje - GET")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAvioTvrtkeStanje")
  public Response avioTvrtkeStanje(
      @PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "STANJE " + idUlaznice + " " + idLeta,
        this.adresa.get(id),
        Integer.parseInt(this.mreznaVrataAvioTvrtke.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().entity(odgovor).type(MediaType.APPLICATION_JSON).build();
    }
    if (odgovor.startsWith("ERROR 20")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    if (odgovor.startsWith("ERROR 21") || odgovor.startsWith("ERROR 22")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("aviotvrtke/{id}/zatvori/{idLeta}")
  @PUT
  @Operation(summary = "Zatvara sjedala na letu slanjem ZATVORI komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno zatvoreno"),
      @APIResponse(responseCode = "401", description = "Neovlašteni pristup"),
      @APIResponse(responseCode = "404", description = "Ne postoji let"),
      @APIResponse(responseCode = "409", description = "Neispravni podaci"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeAvioTvrtkeZatvoriLetCount",
      description = "Broj poziva aviotvrtke zatvori let - PUT")
  @Timed(name = "rezervacijeAvioTvrtkeZatvoriLetTime",
      description = "Vrijeme izvršavanja aviotvrtke zatvori let - PUT")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAvioTvrtkeZatvoriLet")
  public Response avioTvrtkeZatvoriLet(
      @PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("odReda") String odReda,
      @HeaderParam("doReda") String doReda) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    String email = dajEmailKorisnika(idUlaznice);
    if (email == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String avioTvrtka = dajAvioTvrtku(email);
    if (avioTvrtka == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ZATVORI " + idUlaznice + " " + avioTvrtka + " " + idLeta + " " + odReda + " " + doReda,
        this.adresa.get(id),
        Integer.parseInt(this.mreznaVrataAvioTvrtke.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    if (odgovor.startsWith("ERROR 20")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    if (odgovor.startsWith("ERROR 21") || odgovor.startsWith("ERROR 22")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("aviotvrtke/{id}/zatvori")
  @PUT
  @Operation(summary = "Zatvara sjedala na letovima avio tvrtke u zadanom razdoblju")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno zatvoreno"),
      @APIResponse(responseCode = "401", description = "Neovlašteni pristup"),
      @APIResponse(responseCode = "404", description = "Ne postoje letovi"),
      @APIResponse(responseCode = "409", description = "Neispravni podaci"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeAvioTvrtkeZatvoriCount",
      description = "Broj poziva aviotvrtke zatvori - PUT")
  @Timed(name = "rezervacijeAvioTvrtkeZatvoriTime",
      description = "Vrijeme izvršavanja aviotvrtke zatvori - PUT")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAvioTvrtkeZatvori")
  public Response avioTvrtkeZatvori(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    String email = dajEmailKorisnika(idUlaznice);
    if (email == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String avioTvrtka = dajAvioTvrtku(email);
    if (avioTvrtka == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ZATVORI " + idUlaznice + " " + avioTvrtka + " " + odDatuma + " " + doDatuma,
        this.adresa.get(id),
        Integer.parseInt(this.mreznaVrataAvioTvrtke.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    if (odgovor.startsWith("ERROR 20")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    if (odgovor.startsWith("ERROR 21") || odgovor.startsWith("ERROR 24")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("aviotvrtke/{id}/inicijaliziraj/let")
  @POST
  @Operation(summary = "Inicijalizira letove za oznaku leta slanjem INICIJALIZIRAJ komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "201", description = "Uspješno inicijalizirano"),
      @APIResponse(responseCode = "401", description = "Neovlašteni pristup"),
      @APIResponse(responseCode = "404", description = "Ne postoji let"),
      @APIResponse(responseCode = "409", description = "Već inicijalizirano"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeAvioTvrtkeInicijalizirajLetCount",
      description = "Broj poziva aviotvrtke inicijaliziraj/let - POST")
  @Timed(name = "rezervacijeAvioTvrtkeInicijalizirajLetTime",
      description = "Vrijeme izvršavanja aviotvrtke inicijaliziraj/let - POST")
  @Timeout(5000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAvioTvrtkeInicijalizirajLet")
  public Response avioTvrtkeInicijalizirajLet(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("oznakaLeta") String oznakaLeta,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    String email = dajEmailKorisnika(idUlaznice);
    if (email == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String avioTvrtka = dajAvioTvrtku(email);
    if (avioTvrtka == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
    	"INICIJALIZIRAJ " + idUlaznice + " " + avioTvrtka + " \"" + oznakaLeta + "\" " + odDatuma + " " + doDatuma,
        this.adresa.get(id),
        Integer.parseInt(this.mreznaVrataAvioTvrtke.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.status(Response.Status.CREATED).entity(odgovor).build();
    }
    if (odgovor.startsWith("ERROR 20")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    if (odgovor.startsWith("ERROR 21") || odgovor.startsWith("ERROR 22")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("aviotvrtke/{id}/inicijaliziraj")
  @POST
  @Operation(summary = "Inicijalizira sve letove avio tvrtke slanjem INICIJALIZIRAJ komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "201", description = "Uspješno inicijalizirano"),
      @APIResponse(responseCode = "401", description = "Neovlašteni pristup"),
      @APIResponse(responseCode = "404", description = "Ne postoje letovi"),
      @APIResponse(responseCode = "409", description = "Već inicijalizirano"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeAvioTvrtkeInicijalizirajCount",
      description = "Broj poziva aviotvrtke inicijaliziraj - POST")
  @Timed(name = "rezervacijeAvioTvrtkeInicijalizirajTime",
      description = "Vrijeme izvršavanja aviotvrtke inicijaliziraj - POST")
  @Timeout(5000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAvioTvrtkeInicijaliziraj")
  public Response avioTvrtkeInicijaliziraj(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    String email = dajEmailKorisnika(idUlaznice);
    if (email == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String avioTvrtka = dajAvioTvrtku(email);
    if (avioTvrtka == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "INICIJALIZIRAJ " + idUlaznice + " " + avioTvrtka + " " + odDatuma + " " + doDatuma,
        this.adresa.get(id),
        Integer.parseInt(this.mreznaVrataAvioTvrtke.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.status(Response.Status.CREATED).entity(odgovor).build();
    }
    if (odgovor.startsWith("ERROR 20")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    if (odgovor.startsWith("ERROR 21") || odgovor.startsWith("ERROR 22")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("aviotvrtke/{id}/ispis/{idLeta}")
  @GET
  @Operation(summary = "Dohvaća rezervacije sjedala leta slanjem ISPIS komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno dohvaćeno"),
      @APIResponse(responseCode = "404", description = "Ne postoje rezervacije"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeAvioTvrtkeIspisLetaCount",
      description = "Broj poziva aviotvrtke ispis leta - GET")
  @Timed(name = "rezervacijeAvioTvrtkeIspisLetaTime",
      description = "Vrijeme izvršavanja aviotvrtke ispis leta - GET")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAvioTvrtkeIspisLeta")
  public Response avioTvrtkeIspisLeta(
      @PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ISPIS " + idUlaznice + " " + idLeta,
        this.adresa.get(id),
        Integer.parseInt(this.mreznaVrataAvioTvrtke.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().entity(odgovor).type(MediaType.APPLICATION_JSON).build();
    }
    if (odgovor.startsWith("ERROR 24")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }


  @Path("admin/{id}/ping")
  @HEAD
  @Operation(summary = "Provjera rada PoslužiteljRezervacije (admin) slanjem PING komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji poslužitelj"),
      @APIResponse(responseCode = "503", description = "Nedostupan servis")})
  @Counted(name = "rezervacijeAdminPingCount",
      description = "Broj poziva admin ping - HEAD")
  @Timed(name = "rezervacijeAdminPingTime",
      description = "Vrijeme izvršavanja admin ping - HEAD")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAdminPing")
  public Response adminPing(@PathParam("id") int id) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "PING", this.adresa.get(id),
        Integer.parseInt(this.mreznaVrataAdministracija.get(id)));
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @Path("admin/{id}/kraj")
  @HEAD
  @Operation(summary = "Kraj rada PoslužiteljRezervacije slanjem KRAJ komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji poslužitelj"),
      @APIResponse(responseCode = "503", description = "Nedostupan servis")})
  @Counted(name = "rezervacijeAdminKrajCount",
      description = "Broj poziva admin kraj - HEAD")
  @Timed(name = "rezervacijeAdminKrajTime",
      description = "Vrijeme izvršavanja admin kraj - HEAD")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAdminKraj")
  public Response adminKraj(@PathParam("id") int id) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "KRAJ " + this.kodZaKraj.get(id).trim(),
        this.adresa.get(id),
        Integer.parseInt(this.mreznaVrataAdministracija.get(id)));
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @Path("admin/{id}/rezervacije")
  @DELETE
  @Operation(summary = "Briše sve rezervacije slanjem REZERVACIJE PONIŠTI komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno obrisano"),
      @APIResponse(responseCode = "404", description = "Nema rezervacija"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "rezervacijeAdminObrisiCount",
      description = "Broj poziva admin obrisi rezervacije - DELETE")
  @Timed(name = "rezervacijeAdminObrisiTime",
      description = "Vrijeme izvršavanja admin obrisi rezervacije - DELETE")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAdminObrisiRezervacije")
  public Response adminObrisiRezervacije(@PathParam("id") int id) {
    if (!jeValidanId(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "REZERVACIJE PONIŠTI",
        this.adresa.get(id),
        Integer.parseInt(this.mreznaVrataAdministracija.get(id)));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    if (odgovor.startsWith("ERROR")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }


  public Response metodaKodPogreskePing(@PathParam("id") int id) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskePauza(
      @PathParam("id") int id, @PathParam("milisek") String milisek) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeIspisSjedala(
      @PathParam("id") int id,
      @PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
      @HeaderParam("idUlaznice") String idUlaznice) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeIspisPutnik(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("putnik") String putnik,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }

  public Response metodaKodPogreskeIspisLet(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("oznakaLeta") String oznakaLeta,
      @HeaderParam("datum") String datum) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeRezerviraj(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("idLeta") String idLeta,
      @HeaderParam("putnik") String putnik,
      @HeaderParam("razred") String razred) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskePotvrdi(
      @PathParam("id") int id,
      @PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("putnik") String putnik) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeRezervirajGrupa(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("idLeta") String idLeta,
      @HeaderParam("putnici") String putnici,
      @HeaderParam("razred") String razred) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskePotvrdiGrupa(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("rezervacijeSjedala") String rezervacijeSjedala,
      @HeaderParam("putnici") String putnici) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeAvioTvrtkePing(@PathParam("id") int id) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeAvioTvrtkeIspisPutnik(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("putnik") String putnik,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }

  public Response metodaKodPogreskeAvioTvrtkeIspisLetovi(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeAvioTvrtkeStanje(
      @PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }

  public Response metodaKodPogreskeAvioTvrtkeZatvoriLet(
      @PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("odReda") String odReda,
      @HeaderParam("doReda") String doReda) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }

  public Response metodaKodPogreskeAvioTvrtkeZatvori(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeAvioTvrtkeInicijalizirajLet(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("oznakaLeta") String oznakaLeta,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeAvioTvrtkeInicijaliziraj(
      @PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }

 
  public Response metodaKodPogreskeAvioTvrtkeIspisLeta(
      @PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }

 
  public Response metodaKodPogreskeAdminPing(@PathParam("id") int id) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeAdminKraj(@PathParam("id") int id) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeAdminObrisiRezervacije(@PathParam("id") int id) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }
}