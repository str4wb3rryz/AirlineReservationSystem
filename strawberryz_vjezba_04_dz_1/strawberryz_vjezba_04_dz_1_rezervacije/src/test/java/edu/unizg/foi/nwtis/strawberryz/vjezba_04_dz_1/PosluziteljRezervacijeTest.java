package edu.unizg.foi.nwtis.strawberryz.vjezba_04_dz_1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;
import edu.unizg.foi.nwtis.AvioTvrtka;
import edu.unizg.foi.nwtis.Avion;
import edu.unizg.foi.nwtis.Let;
import edu.unizg.foi.nwtis.LetPodaci;
import edu.unizg.foi.nwtis.LetSjedalo;
import edu.unizg.foi.nwtis.ProblemKodRezervacije;
import edu.unizg.foi.nwtis.RazredSjedala;
import edu.unizg.foi.nwtis.RezervacijaSjedala;
import edu.unizg.foi.nwtis.StatusRezervacijeSjedala;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PosluziteljRezervacijeTest {

  PosluziteljRezervacije posluziteljRezervacije;

  @TempDir
  Path tempDir;

  private String konfigDatoteka;
  private String kodZaKraj;
  private String testIdLeta = "b4b730fb";
  private String testIdUlaznice = "538abba9d8e124dd";

  @BeforeEach
  void setUp(TestInfo testInfo) throws Exception {
    posluziteljRezervacije = new PosluziteljRezervacije();

    konfigDatoteka = tempDir.toString() + java.io.File.separatorChar + "NWTiS_dz_1.txt";
    kodZaKraj = Integer.toHexString(konfigDatoteka.hashCode());

    kopirajTestneDatoteke();

    kreirajKonfiguraciju();

    boolean ucitano = posluziteljRezervacije.ucitajKonfiguraciju(konfigDatoteka);
    assertTrue(ucitano, "Konfiguracija bi se trebala učitati");
  }

  private void kopirajTestneDatoteke() throws Exception {
    String[] datoteke = {"aviotvrtke.csv", "avioni.csv", "letovi.csv", "A220-300.csv",
        "A319-100.csv", "A350-900.csv", "A380-800.csv", "Dash8-Q400.csv"};

    for (String naziv : datoteke) {
      Path cilj = tempDir.resolve(naziv);
      java.io.File izvor = new java.io.File(naziv);
      if (izvor.exists()) {
        Files.copy(izvor.toPath(), cilj, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      } else {
        try (var is = getClass().getClassLoader().getResourceAsStream(naziv)) {
          if (is != null) {
            Files.copy(is, cilj, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
          }
        }
      }
    }
  }


  private void kreirajKonfiguraciju() throws NeispravnaKonfiguracija {
    var tempKonf = KonfiguracijaApstraktna.dajKonfiguraciju(konfigDatoteka);
    tempKonf.spremiPostavku("datoteka", konfigDatoteka);
    tempKonf.spremiPostavku("mreznaVrata", "8010");
    tempKonf.spremiPostavku("mreznaVrataAdministracija", "8012");
    tempKonf.spremiPostavku("mreznaVrataAvioTvrtke", "8011");
    tempKonf.spremiPostavku("mreznaVrataKontrola", "8000");
    tempKonf.spremiPostavku("adresaKontrola", "localhost");
    tempKonf.spremiPostavku("datoteka_avioTvrtke", "aviotvrtke.csv");
    tempKonf.spremiPostavku("datoteka_avioni", "avioni.csv");
    tempKonf.spremiPostavku("datoteka_letovi", "letovi.csv");
    tempKonf.spremiPostavku("datumOd", "2026-05-01");
    tempKonf.spremiPostavku("datumDo", "2026-05-31");
    tempKonf.spremiPostavku("kodZaKraj", kodZaKraj);
    tempKonf.spremiPostavku("pauzaDretve", "100");
    tempKonf.spremiPostavku("maksBrojDretvi", "10");
    tempKonf.spremiPostavku("maksBrojCekaca", "20");
    tempKonf.spremiPostavku("odredivanjeSjedala",
        "edu.unizg.foi.nwtis.strawberryz.vjezba_04_dz_1.PopuniRavnomjernoOdPocetka");
    tempKonf.spremiKonfiguraciju();
  }


  private Let kreirajTestniLet() {
    var avion = new Avion("A220-300", "A220-300.csv", "Airbus A 220-300", new HashMap<>());
    var avioTvrtka = new AvioTvrtka("LH", "Lufthansa", new HashMap<>());
    avioTvrtka.dodajAvion(avion);
    var letPodaci = new LetPodaci("LH 2483", avioTvrtka, LocalTime.of(6, 55), "LHR", "MUC", avion);
    return Let.kreirajLet(letPodaci, LocalDate.of(2026, 5, 15));
  }

  private RezervacijaSjedala kreirajSlobodnuRezervaciju(Let let, int red, String oznaka) {
    String id = let.id() + ":" + red + oznaka;
    LetSjedalo sjedalo = new LetSjedalo(let, RazredSjedala.EKONOMSKI, red, oznaka);
    return new RezervacijaSjedala(id, sjedalo, null, LocalDateTime.now(),
        StatusRezervacijeSjedala.SLOBODNA);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (posluziteljRezervacije.executor != null) {
      posluziteljRezervacije.executor.shutdownNow();
      posluziteljRezervacije.executor.awaitTermination(500, TimeUnit.MILLISECONDS);
    }
    if (posluziteljRezervacije != null) {
      posluziteljRezervacije.kraj.set(true);
    }
    posluziteljRezervacije = null;
  }


  @Test
  @Order(1)
  final void testUcitajKonfiguraciju() {
    var neispravnaDatoteka = tempDir.toString() + java.io.File.separatorChar + "NWTiS_dz_1.123";
    var odgovor = new PosluziteljRezervacije().ucitajKonfiguraciju(neispravnaDatoteka);
    assertFalse(odgovor);


    odgovor = posluziteljRezervacije.ucitajKonfiguraciju(konfigDatoteka);
    assertTrue(odgovor);

    var dobijeno = posluziteljRezervacije.konfig.dajPostavku("datoteka");
    assertEquals(konfigDatoteka, dobijeno);
  }


  @Test
  @Order(2)
  final void testUcitajAlgoritam() {
    assertTrue(posluziteljRezervacije.ucitajAlgoritam());
    assertNotNull(posluziteljRezervacije.dajAlgoritam());
    assertEquals("edu.unizg.foi.nwtis.strawberryz.vjezba_04_dz_1.PopuniRavnomjernoOdPocetka",
        posluziteljRezervacije.dajAlgoritam().getClass().getName());
  }

  @Test
  @Order(3)
  final void testUcitajAlgoritamNeispravan() {
    var posluzitelj = new PosluziteljRezervacije();
    try {
      var konf = KonfiguracijaApstraktna.dajKonfiguraciju(konfigDatoteka);
      konf.spremiPostavku("odredivanjeSjedala", "nepostojeca.klasa.Ime");
      konf.spremiKonfiguraciju();
      posluzitelj.konfig = konf;
    } catch (Exception _) {
      fail("Pogreška kod pripreme konfiguracije");
    }
    assertFalse(posluzitelj.ucitajAlgoritam());
    assertNull(posluzitelj.dajAlgoritam());
  }


  @Test
  @Order(4)
  final void testUcitajPodatkeSustava() {
    assertTrue(Files.exists(tempDir.resolve("aviotvrtke.csv")), "aviotvrtke.csv ne postoji");
    assertTrue(Files.exists(tempDir.resolve("avioni.csv")), "avioni.csv ne postoji");
    assertTrue(Files.exists(tempDir.resolve("letovi.csv")), "letovi.csv ne postoji");
    assertTrue(Files.exists(tempDir.resolve("A220-300.csv")), "A220-300.csv ne postoji");


    boolean rezultat = posluziteljRezervacije.ucitajPodatkeSustava();
    assertTrue(rezultat, "ucitajPodatkeSustava bi trebao vratiti true");


    assertFalse(posluziteljRezervacije.avioTvrtke.isEmpty(), "avioTvrtke ne smije biti prazna");
    assertFalse(posluziteljRezervacije.avioni.isEmpty(), "avioni ne smije biti prazna");
    assertFalse(posluziteljRezervacije.podaciLetova.isEmpty(), "podaciLetova ne smije biti prazna");
    assertFalse(posluziteljRezervacije.letovi.isEmpty(), "letovi ne smije biti prazna");
    assertFalse(posluziteljRezervacije.rezervacije.isEmpty(), "rezervacije ne smije biti prazna");

    assertEquals(5, posluziteljRezervacije.avioTvrtke.size(), "Trebalo bi biti 5 avio tvrtki");


    assertEquals(5, posluziteljRezervacije.avioni.size(), "Trebalo bi biti 5 aviona");
  }


  @Test
  @Order(5)
  final void testUcitajAvioTvrtke() throws Exception {
    Path datoteka = tempDir.resolve("aviotvrtke.csv");
    posluziteljRezervacije.ucitajAvioTvrtke(datoteka);

    assertTrue(posluziteljRezervacije.avioTvrtke.containsKey("OU"));
    assertEquals("Croatia Airlines", posluziteljRezervacije.avioTvrtke.get("OU").naziv());
    assertTrue(posluziteljRezervacije.avioTvrtke.containsKey("LH"));
    assertEquals("Lufthansa", posluziteljRezervacije.avioTvrtke.get("LH").naziv());
    assertTrue(posluziteljRezervacije.avioTvrtke.containsKey("TK"));
    assertEquals("Turkish Airlines", posluziteljRezervacije.avioTvrtke.get("TK").naziv());
    assertTrue(posluziteljRezervacije.avioTvrtke.containsKey("AF"));
    assertEquals("Air France", posluziteljRezervacije.avioTvrtke.get("AF").naziv());
    assertTrue(posluziteljRezervacije.avioTvrtke.containsKey("BA"));
    assertEquals("British Airways", posluziteljRezervacije.avioTvrtke.get("BA").naziv());
  }

  @Test
  @Order(6)
  final void testUcitajAvione() throws Exception {
    Path datoteka = tempDir.resolve("avioni.csv");
    posluziteljRezervacije.ucitajAvione(datoteka);

    assertTrue(posluziteljRezervacije.avioni.containsKey("A220-300"));
    assertEquals("Airbus A 220-300", posluziteljRezervacije.avioni.get("A220-300").naziv());
    assertEquals("A220-300.csv", posluziteljRezervacije.avioni.get("A220-300").datoteka());

    assertTrue(posluziteljRezervacije.avioni.containsKey("A319-100"));
    assertEquals("Airbus A 319-100", posluziteljRezervacije.avioni.get("A319-100").naziv());
    assertEquals("A319-100.csv", posluziteljRezervacije.avioni.get("A319-100").datoteka());

    assertTrue(posluziteljRezervacije.avioni.containsKey("A350-900"));
    assertEquals("Airbus A350-900", posluziteljRezervacije.avioni.get("A350-900").naziv());
    assertEquals("A350-900.csv", posluziteljRezervacije.avioni.get("A350-900").datoteka());

    assertTrue(posluziteljRezervacije.avioni.containsKey("A380-800"));
    assertEquals("Airbus A380-800", posluziteljRezervacije.avioni.get("A380-800").naziv());
    assertEquals("A380-800.csv", posluziteljRezervacije.avioni.get("A380-800").datoteka());

    assertTrue(posluziteljRezervacije.avioni.containsKey("DH8D"));
    assertEquals("De Havilland Canada Dash 8-400",
        posluziteljRezervacije.avioni.get("DH8D").naziv());
    assertEquals("Dash8-Q400.csv", posluziteljRezervacije.avioni.get("DH8D").datoteka());
  }


  @Test
  @Order(7)
  final void testUcitajPodatkeLetova() throws Exception {

    Path avioTvrtkePath = tempDir.resolve("aviotvrtke.csv");
    Path avioniPath = tempDir.resolve("avioni.csv");
    posluziteljRezervacije.ucitajAvioTvrtke(avioTvrtkePath);
    posluziteljRezervacije.ucitajAvione(avioniPath);

    Path letoviPath = tempDir.resolve("letovi.csv");
    posluziteljRezervacije.ucitajPodatkeLetova(letoviPath);


    assertFalse(posluziteljRezervacije.podaciLetova.isEmpty());
  }


  @Test
  @Order(8)
  final void testParsirajCsvRed() {
    String linija =
        "A220-300,A220-300.csv,\"Airbus A 220-300\",\"Engines: 2, Maximum cruising speed: 871 km/h\"";
    List<String> rezultat = posluziteljRezervacije.parsirajCsvRed(linija);

    assertEquals(4, rezultat.size());
    assertEquals("A220-300", rezultat.get(0));
    assertEquals("A220-300.csv", rezultat.get(1));
    assertEquals("Airbus A 220-300", rezultat.get(2));
    assertTrue(rezultat.get(3).contains("Engines: 2"));
  }

  @Test
  @Order(9)
  final void testMapirajRazred() {
    assertEquals(RazredSjedala.POSLOVNI, posluziteljRezervacije.mapirajRazred("P"));
    assertEquals(RazredSjedala.POSLOVNI, posluziteljRezervacije.mapirajRazred("POSLOVNI"));
    assertEquals(RazredSjedala.EKONOMSKI, posluziteljRezervacije.mapirajRazred("E"));
    assertEquals(RazredSjedala.EKONOMSKI, posluziteljRezervacije.mapirajRazred("EKONOMSKI"));

    assertThrows(IllegalArgumentException.class,
        () -> posluziteljRezervacije.mapirajRazred("NEPOZNATO"));
  }

  @Test
  @Order(10)
  final void testOdrediBazuDatoteka() {
    Path baza = posluziteljRezervacije.odrediBazuDatoteka();
    assertNotNull(baza);
    assertEquals(tempDir, baza);
  }

  @Test
  @Order(11)
  final void testSpavanjeDretve() {
    long pocetak = System.currentTimeMillis();
    posluziteljRezervacije.spavanjeDretve();
    long kraj = System.currentTimeMillis();
    assertTrue(kraj - pocetak >= 90);
  }

  @Test
  @Order(12)
  final void testProvjeriUlaznicu() {
    boolean rezultat = posluziteljRezervacije.provjeriUlaznicu(testIdUlaznice);
    assertFalse(rezultat);
  }

  @Test
  @Order(13)
  final void testObradiPauzu() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(baos);

    posluziteljRezervacije.obradiPauzu(out, "10", "ERROR 29");
    out.flush();
    assertEquals("OK\n", baos.toString());
    baos.reset();

    posluziteljRezervacije.obradiPauzu(out, "abc", "ERROR 29");
    out.flush();
    assertEquals("ERROR 20\n", baos.toString());
  }

  @Test
  @Order(14)
  final void testUcitajSjedalaZaAvion() throws Exception {
    Let let = kreirajTestniLet();
    Path datoteka = tempDir.resolve("A220-300.csv");

    Map<Integer, Map<String, RezervacijaSjedala>> redovi =
        posluziteljRezervacije.ucitajSjedalaZaAvion(let, datoteka);

    assertFalse(redovi.isEmpty());
    assertTrue(redovi.containsKey(1));
    assertTrue(redovi.containsKey(7));
    assertTrue(redovi.containsKey(31));

    Map<String, RezervacijaSjedala> prviRed = redovi.get(1);
    assertTrue(prviRed.containsKey("A"));
    assertTrue(prviRed.containsKey("C"));
    assertTrue(prviRed.containsKey("D"));
    assertTrue(prviRed.containsKey("E"));
    assertTrue(prviRed.containsKey("F"));
    assertFalse(prviRed.containsKey("_"));
  }

  @Test
  @Order(15)
  final void testPopuniRavnomjernoOdPocetkaOdabirSjedala() throws ProblemKodRezervacije {
    var let = kreirajTestniLet();
    var red1 = new LinkedHashMap<String, RezervacijaSjedala>();
    red1.put("A", kreirajSlobodnuRezervaciju(let, 1, "A"));
    red1.put("C", kreirajSlobodnuRezervaciju(let, 1, "C"));
    red1.put("E", kreirajSlobodnuRezervaciju(let, 1, "E"));

    var redovi = new HashMap<Integer, Map<String, RezervacijaSjedala>>();
    redovi.put(1, red1);
    var algoritam = new PopuniRavnomjernoOdPocetka();
    assertTrue(algoritam.inicijaliziraj());
    algoritam.dodajSjedala(let.id(), redovi);

    assertEquals("A", algoritam.rezervirajSjedalo(let, "putnik1", RazredSjedala.EKONOMSKI).sjedalo()
        .oznakaSjedala());
    assertEquals("E", algoritam.rezervirajSjedalo(let, "putnik2", RazredSjedala.EKONOMSKI).sjedalo()
        .oznakaSjedala());
    assertEquals("C", algoritam.rezervirajSjedalo(let, "putnik3", RazredSjedala.EKONOMSKI).sjedalo()
        .oznakaSjedala());
  }

  @Test
  @Order(16)
  final void testPopuniPoStupcimaRavnomjernoOdPocetkaOdabirSjedala() throws ProblemKodRezervacije {
    var let = kreirajTestniLet();
    var red1 = new LinkedHashMap<String, RezervacijaSjedala>();
    var red2 = new LinkedHashMap<String, RezervacijaSjedala>();
    var red3 = new LinkedHashMap<String, RezervacijaSjedala>();
    for (var oznaka : List.of("A", "C", "D", "E", "F")) {
      red1.put(oznaka, kreirajSlobodnuRezervaciju(let, 1, oznaka));
      red2.put(oznaka, kreirajSlobodnuRezervaciju(let, 2, oznaka));
      red3.put(oznaka, kreirajSlobodnuRezervaciju(let, 3, oznaka));
    }

    var redovi = new HashMap<Integer, Map<String, RezervacijaSjedala>>();
    redovi.put(1, red1);
    redovi.put(2, red2);
    redovi.put(3, red3);
    var algoritam = new PopuniPoStupcimaRavnomjernoOdPocetka();
    assertTrue(algoritam.inicijaliziraj());
    algoritam.dodajSjedala(let.id(), redovi);

    assertEquals("A", algoritam.rezervirajSjedalo(let, "putnik1", RazredSjedala.EKONOMSKI).sjedalo()
        .oznakaSjedala());
    assertEquals("F", algoritam.rezervirajSjedalo(let, "putnik2", RazredSjedala.EKONOMSKI).sjedalo()
        .oznakaSjedala());
  }

  @Test
  @Order(17)
  final void testKomandaIspisKorisnik() {
    String odgovor = izvrsiKomanduKorisnik("ISPIS");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduKorisnik("ISPIS id");
    assertEquals("ERROR 20", odgovor);

    odgovor =
        izvrsiKomanduKorisnik(String.format("ISPIS %s \"OU 4436\" 2026-05-15", testIdUlaznice));
    assertEquals("ERROR 21", odgovor);
  }

  @Test
  @Order(18)
  final void testKomandaRezervirajKorisnik() {
    String odgovor = izvrsiKomanduKorisnik("REZERVIRAJ");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduKorisnik("REZERVIRAJ id");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduKorisnik("REZERVIRAJ id let putnik");
    assertEquals("ERROR 20", odgovor);
    odgovor = izvrsiKomanduKorisnik(
        String.format("REZERVIRAJ %s %s putnik@test.com P", testIdUlaznice, testIdLeta));
    assertEquals("ERROR 21", odgovor);
  }

  @Test
  @Order(19)
  final void testKomandaPotvrdiKorisnik() {
    String odgovor = izvrsiKomanduKorisnik("POTVRDI");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduKorisnik("POTVRDI id");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduKorisnik("POTVRDI id rez");
    assertEquals("ERROR 20", odgovor);

    odgovor =
        izvrsiKomanduKorisnik(String.format("POTVRDI %s rez123 putnik@test.com", testIdUlaznice));
    assertEquals("ERROR 21", odgovor);
  }

  @Test
  @Order(20)
  final void testKomandaRezervirajGrupaKorisnik() {
    String odgovor = izvrsiKomanduKorisnik("REZERVIRAJ GRUPA id let putnik E");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduKorisnik("REZERVIRAJ GRUPA id let [] E");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduKorisnik(
        String.format("REZERVIRAJ GRUPA %s %s [putnik1@test.com] E", testIdUlaznice, testIdLeta));
    assertEquals("ERROR 21", odgovor);
  }

  @Test
  @Order(21)
  final void testKomandaPauzaKorisnik() {
    String odgovor = izvrsiKomanduKorisnik("PAUZA");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduKorisnik("PAUZA abc");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduKorisnik("PAUZA 1");
    assertEquals("OK", odgovor);
  }

  @Test
  @Order(22)
  final void testKomandaPingKorisnik() {
    String odgovor = izvrsiKomanduKorisnik("PING");
    assertEquals("OK", odgovor);
  }

  @Test
  @Order(23)
  final void testKomandaIspisAvioTvrtke() {
    String odgovor = izvrsiKomanduAvioTvrtka("ISPIS");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduAvioTvrtka("ISPIS id");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduAvioTvrtka(String.format("ISPIS %s %s", testIdUlaznice, testIdLeta));
    assertEquals("ERROR 21", odgovor);
  }

  @Test
  @Order(24)
  final void testKomandaZatvoriRedove() {

    String odgovor = izvrsiKomanduAvioTvrtka("ZATVORI");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduAvioTvrtka("ZATVORI id avio let");
    assertEquals("ERROR 20", odgovor);


    odgovor =
        izvrsiKomanduAvioTvrtka(String.format("ZATVORI %s OU %s 1 10", testIdUlaznice, testIdLeta));
    assertEquals("ERROR 21", odgovor);
  }

  @Test
  @Order(25)
  final void testKomandaZatvoriDatume() {

    String odgovor = izvrsiKomanduAvioTvrtka("ZATVORI id avio");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduAvioTvrtka(
        String.format("ZATVORI %s OU 2026-05-01 2026-05-31", testIdUlaznice));
    assertEquals("ERROR 21", odgovor);
  }

  @Test
  @Order(26)
  final void testKomandaInicijaliziraj() {

    String odgovor = izvrsiKomanduAvioTvrtka("INICIJALIZIRAJ");
    assertEquals("ERROR 20", odgovor);


    odgovor = izvrsiKomanduAvioTvrtka(
        String.format("INICIJALIZIRAJ %s OU 2026-05-01 2026-05-31", testIdUlaznice));
    assertEquals("ERROR 21", odgovor);
  }

  @Test
  @Order(27)
  final void testKomandaPingAvioTvrtke() {
    String odgovor = izvrsiKomanduAvioTvrtka("PING");
    assertEquals("OK", odgovor);
  }


  @Test
  @Order(28)
  final void testKomandaRezervacijePonisti() {

    String odgovor = izvrsiKomanduAdmin("REZERVACIJE");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduAdmin("PONIŠTI");
    assertEquals("ERROR 20", odgovor);


    odgovor = izvrsiKomanduAdmin("REZERVACIJE PONIŠTI");
    assertEquals("OK", odgovor);
  }

  @Test
  @Order(29)
  final void testKomandaPingAdmin() {
    String odgovor = izvrsiKomanduAdmin("PING");
    assertEquals("OK", odgovor);
  }

  @Test
  @Order(30)
  final void testKomandaKrajAdmin() {

    String odgovor = izvrsiKomanduAdmin("KRAJ");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduAdmin("KRAJ pogresan_kod");
    assertEquals("ERROR 28", odgovor);
  }


  @Test
  @Order(31)
  final void testError20_NeispravanFormat() {
    String odgovor = izvrsiKomanduKorisnik("");
    assertEquals("ERROR 20", odgovor);

    odgovor = izvrsiKomanduKorisnik("NEPOZNATA KOMANDA");
    assertEquals("ERROR 20", odgovor);
  }

  @Test
  @Order(32)
  final void testError28_NeispravanKodZaKraj() {
    String odgovor = izvrsiKomanduAdmin("KRAJ pogresan_kod");
    assertEquals("ERROR 28", odgovor);
  }


  private String izvrsiKomanduKorisnik(String komanda) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(baos);

    posluziteljRezervacije.komandeKorisnika(out, komanda);
    out.flush();

    String odgovor = baos.toString().trim();
    return odgovor.isEmpty() ? "ERROR" : odgovor;
  }

  private String izvrsiKomanduAvioTvrtka(String komanda) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(baos);

    posluziteljRezervacije.komandeAvioTvrtke(out, komanda);
    out.flush();

    String odgovor = baos.toString().trim();
    return odgovor.isEmpty() ? "ERROR" : odgovor;
  }


  private String izvrsiKomanduAdmin(String komanda) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(baos);

    posluziteljRezervacije.komandeAdministracija(out, komanda);
    out.flush();

    String odgovor = baos.toString().trim();
    return odgovor.isEmpty() ? "ERROR" : odgovor;
  }

}
