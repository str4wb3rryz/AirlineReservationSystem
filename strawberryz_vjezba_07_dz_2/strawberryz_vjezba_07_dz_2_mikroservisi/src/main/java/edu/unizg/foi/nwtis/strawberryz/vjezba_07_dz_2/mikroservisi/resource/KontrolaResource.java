package edu.unizg.foi.nwtis.strawberryz.vjezba_07_dz_2.mikroservisi.resource;

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
import edu.unizg.foi.nwtis.Korisnik;
import edu.unizg.foi.nwtis.strawberryz.vjezba_07_dz_2.mikroservisi.klijenti.KorisniciRestKlijent;
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
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;


@Path("kontrola")
public class KontrolaResource {

  @Inject
  @ConfigProperty(name = "kontrola.adresa")
  private String adresa;

  @Inject
  @ConfigProperty(name = "kontrola.mreznaVrata")
  private String mreznaVrata;

  @Inject
  @ConfigProperty(name = "kontrola.mreznaVrataAdministracija")
  private String mreznaVrataAdministracija;

  @Inject
  @ConfigProperty(name = "kontrola.kodZaKraj")
  private String kodZaKraj;

  @Inject
  KlijentMrezneUticnice klijentMrezneUticnice;

  @Inject
  KorisniciRestKlijent korisniciRestKlijent;

  @Inject
  @ConfigProperty(name = "servisi.adresa")
  private String servisAdresa;

  @Inject
  @ConfigProperty(name = "servisi.mreznaVrata")
  private String servisMreznaVrata;


  private String kAdresa;


  private String kMreznaVrataAdministracija;

  private String kKodZaKraj;


  private String servisUrl;


  @PostConstruct
  private void ucitajConfig() {
    Config config = ConfigProvider.getConfig();
    this.kAdresa = config.getValue("kontrola.adresa", String.class);
    this.kMreznaVrataAdministracija =
        config.getValue("kontrola.mreznaVrataAdministracija", String.class);
    this.kKodZaKraj = config.getValue("kontrola.kodZaKraj", String.class);
    this.servisUrl =
        "http://" + this.servisAdresa + ":" + this.servisMreznaVrata + "/api/v1/korisnici";
  }


  @Path("ping")
  @HEAD
  @Operation(summary = "Provjera rada poslužitelja kontrola slanjem PING komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji poslužitelj kontrola"),
      @APIResponse(responseCode = "503", description = "Nedostupan servis")})
  @Counted(name = "kontrolaPingCount", description = "Broj poziva ping - HEAD")
  @Timed(name = "kontrolaPingTime", description = "Vrijeme izvršavanja ping - HEAD")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskePing")
  public Response ping() {
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "PING", this.adresa, Integer.parseInt(this.mreznaVrata));
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }


  @Path("pauza/{milisek}")
  @HEAD
  @Operation(summary = "Pokreće pauzu slanjem PAUZA komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji poslužitelj kontrola"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "kontrolaPauzaCount", description = "Broj poziva pauza - HEAD")
  @Timed(name = "kontrolaPauzaTime", description = "Vrijeme izvršavanja pauza - HEAD")
  @Timeout(10000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "metodaKodPogreskePauza")
  public Response pauza(@PathParam("milisek") String milisek) {
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "PAUZA " + milisek, this.adresa, Integer.parseInt(this.mreznaVrata));
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }


  @Path("korisnik/dodaj/{korisnik}")
  @POST
  @Operation(summary = "Dodaj korisnika slanjem KORISNIK DODAJ komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "201", description = "Uspješno kreiran"),
      @APIResponse(responseCode = "404", description = "Ne postoji korisnik"),
      @APIResponse(responseCode = "409", description = "Korisnik već postoji"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "kontrolaKorisnikDodajCount", description = "Broj poziva korisnik/dodaj - POST")
  @Timed(name = "kontrolaKorisnikDodajTime", description = "Vrijeme izvršavanja korisnik/dodaj - POST")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeKorisnikDodaj")
  public Response korisnikDodaj(@PathParam("korisnik") String korisnik) {
    Client client = ClientBuilder.newClient();
    WebTarget webTarget = client.target(this.servisUrl);
    var odgovorWS = webTarget.path(korisnik).request(MediaType.APPLICATION_JSON).get();
    if (odgovorWS.getStatus() != Response.Status.OK.getStatusCode()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (!odgovorWS.hasEntity()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Korisnik oKorisnik = odgovorWS.readEntity(Korisnik.class);
    String komanda = "KORISNIK DODAJ \"" + oKorisnik.ime() + "\" \""
        + oKorisnik.prezime() + "\" " + oKorisnik.lozinka() + " " + oKorisnik.email();
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        komanda, this.adresa, Integer.parseInt(this.mreznaVrata));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.status(Response.Status.CREATED).build();
    }
    if (odgovor.startsWith("ERROR 11")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }


  @Path("korisnik/provjeri/{korisnik}")
  @GET
  @Operation(summary = "Provjeri korisnika slanjem KORISNIK PROVJERI komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno provjeren"),
      @APIResponse(responseCode = "404", description = "Ne postoji korisnik"),
      @APIResponse(responseCode = "409", description = "Konflikt"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "kontrolaKorisnikProvjeriCount",
      description = "Broj poziva korisnik/provjeri - GET")
  @Timed(name = "kontrolaKorisnikProvjeriTime",
      description = "Vrijeme izvršavanja korisnik/provjeri - GET")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeKorisnikProvjeri")
  public Response korisnikProvjeri(@PathParam("korisnik") String korisnik) {
    var odgovorWS = this.korisniciRestKlijent.dajKorisnika(korisnik);
    if (odgovorWS.getStatus() != Response.Status.OK.getStatusCode()
        || !odgovorWS.hasEntity()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Korisnik oKorisnik = odgovorWS.readEntity(Korisnik.class);
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "KORISNIK PROVJERI " + oKorisnik.email(),
        this.adresa, Integer.parseInt(this.mreznaVrata));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().entity("Korisnik postoji").build();
    }
    if (odgovor.startsWith("ERROR 12")) {
      return Response.status(Response.Status.NOT_FOUND).entity("Korisnik ne postoji").build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }


  @Path("ulaznica/dodaj/{korisnik}")
  @POST
  @Operation(summary = "Kreira ulaznicu slanjem ULAZNICA DODAJ komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "201", description = "Uspješno kreiran"),
      @APIResponse(responseCode = "404", description = "Ne postoji korisnik"),
      @APIResponse(responseCode = "409", description = "Ulaznica već postoji"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "kontrolaUlaznicaDodajCount",
      description = "Broj poziva ulaznica/dodaj - POST")
  @Timed(name = "kontrolaUlaznicaDodajTime",
      description = "Vrijeme izvršavanja ulaznica/dodaj - POST")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeUlaznicaDodaj")
  public Response ulaznicaDodaj(@PathParam("korisnik") String korisnik) {
    Client client = ClientBuilder.newClient();
    WebTarget webTarget = client.target(this.servisUrl);
    var odgovorWS = webTarget.path(korisnik).request(MediaType.APPLICATION_JSON).get();
    if (odgovorWS.getStatus() != Response.Status.OK.getStatusCode()
        || !odgovorWS.hasEntity()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Korisnik oKorisnik = odgovorWS.readEntity(Korisnik.class);
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ULAZNICA DODAJ " + oKorisnik.email() + " " + oKorisnik.lozinka(),
        this.adresa, Integer.parseInt(this.mreznaVrata));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.status(Response.Status.CREATED).entity(odgovor).build();
    }
    if (odgovor.startsWith("ERROR 12")) {
      return Response.status(Response.Status.NOT_FOUND).entity("Korisnik za izradu ulaznice ne postoji").build();
    }
    if (odgovor.startsWith("ERROR 13")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }


  @Path("ulaznica/provjeri/{id}")
  @GET
  @Operation(summary = "Provjeri ulaznicu slanjem ULAZNICA PROVJERI komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno provjeren"),
      @APIResponse(responseCode = "404", description = "Ne postoji ulaznica"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "kontrolaUlaznicaProvjeriCount",
      description = "Broj poziva ulaznica/provjeri - GET")
  @Timed(name = "kontrolaUlaznicaProvjeriTime",
      description = "Vrijeme izvršavanja ulaznica/provjeri - GET")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeUlaznicaProvjeri")
  public Response ulaznicaProvjeri(@PathParam("id") String id) {
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ULAZNICA PROVJERI " + id, this.adresa, Integer.parseInt(this.mreznaVrata));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().entity(odgovor).build();
    }
    if (odgovor.startsWith("ERROR 14")) {
      return Response.status(Response.Status.NOT_FOUND).entity("Ulaznica ne postoji").build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }


  @Path("ulaznica/korisnik/{id}")
  @GET
  @Operation(summary = "Dohvaća email korisnika slanjem ULAZNICA KORISNIK komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno dohvaćen"),
      @APIResponse(responseCode = "404", description = "Ne postoji ulaznica"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "kontrolaUlaznicaKorisnikCount",
      description = "Broj poziva ulaznica/korisnik - GET")
  @Timed(name = "kontrolaUlaznicaKorisnikTime",
      description = "Vrijeme izvršavanja ulaznica/korisnik - GET")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeUlaznicaKorisnik")
  public Response ulaznicaKorisnik(@PathParam("id") String id) {
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ULAZNICA KORISNIK " + id, this.adresa, Integer.parseInt(this.mreznaVrata));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().entity(odgovor).build();
    }
    if (odgovor.startsWith("ERROR 14")) {
      return Response.status(Response.Status.NOT_FOUND).entity("Korisnik s ulaznicom ne postoji").build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("ulaznica/ponisti/{id}")
  @PUT
  @Operation(summary = "Poništava ulaznicu slanjem ULAZNICA PONIŠTI id komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno poništeno"),
      @APIResponse(responseCode = "404", description = "Ne postoji ulaznica"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "kontrolaUlaznicaPonistiCount",
      description = "Broj poziva ulaznica/ponisti - PUT")
  @Timed(name = "kontrolaUlaznicaPonistiTime",
      description = "Vrijeme izvršavanja ulaznica/ponisti - PUT")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeUlaznicaPonisti")
  public Response ulaznicaPonisti(@PathParam("id") String id) {
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ULAZNICA PONIŠTI " + id, this.adresa, Integer.parseInt(this.mreznaVrata));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().entity(odgovor).build();
    }
    if (odgovor.startsWith("ERROR 14")) {
      return Response.status(Response.Status.NOT_FOUND).entity("Ulaznica ne postoji").build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }


  @Path("admin/ping")
  @HEAD
  @Operation(summary = "Provjera rada poslužitelja kontrola (admin) slanjem PING komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji poslužitelj"),
      @APIResponse(responseCode = "503", description = "Nedostupan servis")})
  @Counted(name = "kontrolaAdminPingCount", description = "Broj poziva admin/ping - HEAD")
  @Timed(name = "kontrolaAdminPingTime", description = "Vrijeme izvršavanja admin/ping - HEAD")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAdminPing")
  public Response adminPing() {
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "PING", this.adresa, Integer.parseInt(this.mreznaVrataAdministracija));
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }


  @Path("admin/kraj")
  @HEAD
  @Operation(summary = "Kraj rada poslužitelja kontrola slanjem KRAJ komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji poslužitelj"),
      @APIResponse(responseCode = "503", description = "Nedostupan servis")})
  @Counted(name = "kontrolaAdminKrajCount", description = "Broj poziva admin/kraj - HEAD")
  @Timed(name = "kontrolaAdminKrajTime", description = "Vrijeme izvršavanja admin/kraj - HEAD")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAdminKraj")
  public Response adminKraj() {
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "KRAJ " + this.kKodZaKraj.trim(),
        this.kAdresa, Integer.parseInt(this.kMreznaVrataAdministracija));
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @Path("admin/ulaznica")
  @PUT
  @Operation(summary = "Poništava sve ulaznice slanjem ULAZNICA PONIŠTI komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno poništeno"),
      @APIResponse(responseCode = "404", description = "Nema ulaznica"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "kontrolaAdminUlaznicaCount",
      description = "Broj poziva admin/ulaznica - PUT")
  @Timed(name = "kontrolaAdminUlaznicaTime",
      description = "Vrijeme izvršavanja admin/ulaznica - PUT")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAdminUlaznica")
  public Response adminUlaznicaPonisti() {
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ULAZNICA PONIŠTI",
        this.kAdresa, Integer.parseInt(this.kMreznaVrataAdministracija));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok().entity(odgovor).build();
    }
    if (odgovor.startsWith("ERROR 14")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }


  @Path("admin/registriraj")
  @POST
  @Operation(summary = "Registrira poslužitelja slanjem REGISTRIRAJ komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "201", description = "Uspješno registriran"),
      @APIResponse(responseCode = "409", description = "Već registriran"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "kontrolaAdminRegistrirajCount",
      description = "Broj poziva admin/registriraj - POST")
  @Timed(name = "kontrolaAdminRegistrirajTime",
      description = "Vrijeme izvršavanja admin/registriraj - POST")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAdminRegistriraj")
  public Response adminRegistriraj(
      @HeaderParam("adresa") String regAdresa,
      @HeaderParam("mreznaVrata") String regMreznaVrata,
      @HeaderParam("mreznaVrataAdministracija") String regMreznaVrataAdministracija,
      @HeaderParam("kodZaKraj") String regKodZaKraj) {
    String komanda = "REGISTRIRAJ " + regAdresa + " " + regMreznaVrata
        + " " + regMreznaVrataAdministracija + " " + regKodZaKraj;
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        komanda, this.kAdresa, Integer.parseInt(this.kMreznaVrataAdministracija));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.status(Response.Status.CREATED).build();
    }
    if (odgovor.startsWith("ERROR")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }


  @Path("admin/zabrana")
  @POST
  @Operation(summary = "Dodaje adresu na listu zabrana slanjem ZABRANA komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "201", description = "Uspješno dodano"),
      @APIResponse(responseCode = "409", description = "Adresa već zabranjena"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "kontrolaAdminZabranaCount",
      description = "Broj poziva admin/zabrana - POST")
  @Timed(name = "kontrolaAdminZabranaTime",
      description = "Vrijeme izvršavanja admin/zabrana - POST")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAdminZabrana")
  public Response adminZabrana(@HeaderParam("adresa") String zabranjenoAdresa) {
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "ZABRANA " + zabranjenoAdresa,
        this.kAdresa, Integer.parseInt(this.kMreznaVrataAdministracija));
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.status(Response.Status.CREATED).build();
    }
    if (odgovor.startsWith("ERROR")) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }


  @Path("admin/dozvola")
  @DELETE
  @Operation(summary = "Briše adresu s liste zabrana slanjem DOZVOLA komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Uspješno obrisano"),
      @APIResponse(responseCode = "404", description = "Adresa nije zabranjena"),
      @APIResponse(responseCode = "500", description = "Greška na poslužitelju")})
  @Counted(name = "kontrolaAdminDozvolaCount",
      description = "Broj poziva admin/dozvola - DELETE")
  @Timed(name = "kontrolaAdminDozvolaTime",
      description = "Vrijeme izvršavanja admin/dozvola - DELETE")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "metodaKodPogreskeAdminDozvola")
  public Response adminDozvola(@HeaderParam("adresa") String dozvoljenaAdresa) {
    var odgovor = klijentMrezneUticnice.posaljiKomandu(
        "DOZVOLA " + dozvoljenaAdresa,
        this.kAdresa, Integer.parseInt(this.kMreznaVrataAdministracija));
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


  public Response metodaKodPogreskePing() {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskePauza(@PathParam("milisek") String milisek) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeKorisnikDodaj(@PathParam("korisnik") String korisnik) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeKorisnikProvjeri(@PathParam("korisnik") String korisnik) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeUlaznicaDodaj(@PathParam("korisnik") String korisnik) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }

 
  public Response metodaKodPogreskeUlaznicaProvjeri(@PathParam("id") String id) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeUlaznicaKorisnik(@PathParam("id") String id) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }

  
  public Response metodaKodPogreskeUlaznicaPonisti(@PathParam("id") String id) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeAdminPing() {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeAdminKraj() {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeAdminUlaznica() {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }

 
  public Response metodaKodPogreskeAdminRegistriraj(
      @HeaderParam("adresa") String regAdresa,
      @HeaderParam("mreznaVrata") String regMreznaVrata,
      @HeaderParam("mreznaVrataAdministracija") String regMreznaVrataAdministracija,
      @HeaderParam("kodZaKraj") String regKodZaKraj) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }

  public Response metodaKodPogreskeAdminZabrana(
      @HeaderParam("adresa") String zabranjenoAdresa) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }


  public Response metodaKodPogreskeAdminDozvola(
      @HeaderParam("adresa") String dozvoljenaAdresa) {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }
}