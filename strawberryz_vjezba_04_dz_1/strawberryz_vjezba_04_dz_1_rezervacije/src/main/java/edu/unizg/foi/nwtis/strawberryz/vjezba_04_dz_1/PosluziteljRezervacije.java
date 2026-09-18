package edu.unizg.foi.nwtis.strawberryz.vjezba_04_dz_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import edu.unizg.foi.nwtis.AvioTvrtka;
import edu.unizg.foi.nwtis.Avion;
import edu.unizg.foi.nwtis.Let;
import edu.unizg.foi.nwtis.LetPodaci;
import edu.unizg.foi.nwtis.LetSjedalo;
import edu.unizg.foi.nwtis.OdredivanjeSjedala;
import edu.unizg.foi.nwtis.ProblemKodRezervacije;
import edu.unizg.foi.nwtis.RazredSjedala;
import edu.unizg.foi.nwtis.RezervacijaSjedala;
import edu.unizg.foi.nwtis.StatusRezervacijeSjedala;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

/**
 * Poslužitelj za upravljanje rezervacijama, letovima i rasporedom sjedala.
 */
public class PosluziteljRezervacije {

  /** Odgovor za neispravnu naredbu. */
  private static final String ERROR_20 = "ERROR 20\n";

  /** Konfiguracija poslužitelja. */
  Konfiguracija konfig = null;

  /** Izvršitelj dretvi. */
  ExecutorService executor = null;

  /** Mrežna vrata za korisnike. */
  int mreznaVrata = 0;

  /** Mrežna vrata za avio tvrtke. */
  int mreznaVrataAvioTvrtke = 0;

  /** Mrežna vrata za administraciju. */
  int mreznaVrataAdmin = 0;

  /** Adresa kontrolnog poslužitelja. */
  String adresaKontrola = "";

  /** Mrežna vrata kontrolnog poslužitelja. */
  int mreznaVrataKontrola = 0;

  /** Datoteka s avio tvrtkama. */
  String datotekaAvioTvrtke = "";

  /** Datoteka s avionima. */
  String datotekaAvioni = "";

  /** Datoteka s podacima o letovima. */
  String datotekaLetovi = "";

  /** Početni datum inicijalizacije letova. */
  LocalDate datumOd;

  /** Završni datum inicijalizacije letova. */
  LocalDate datumDo;

  /** Kod za gašenje poslužitelja. */
  String kodZaKraj = "";

  /** Pauza dretve u milisekundama. */
  int pauzaDretve = 0;

  /** Maksimalan broj dretvi. */
  int maksBrojDretvi = 0;

  /** Maksimalan broj čekajućih zahtjeva. */
  int maksBrojCekaca = 0;

  /** Naziv klase algoritma za određivanje sjedala. */
  String odredivanjeSjedala = "";

  /** Algoritam za određivanje sjedala. */
  OdredivanjeSjedala algoritam = null;

  /** Oznaka završetka rada. */
  AtomicBoolean kraj = new AtomicBoolean(false);

  /** Oznaka pauze za ostale poslužitelje. */
  AtomicBoolean pauzaOstali = new AtomicBoolean(false);

  /** Broj aktivnih zahtjeva. */
  AtomicInteger aktivniZahtjevi = new AtomicInteger(0);

  /** Semafor za ograničenje broja dretvi. */
  Semaphore semaforDretvi = new Semaphore(10);

  /** Regularni izraz za razdvajanje naredbi uz podršku za navodnike. */
  private static final Pattern regex = Pattern.compile(" (?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");

  /** Rezervacije po letu. */
  Map<String, List<RezervacijaSjedala>> rezervacije = new ConcurrentHashMap<>();

  /** Evidencija avio tvrtki. */
  Map<String, AvioTvrtka> avioTvrtke = new ConcurrentHashMap<>();

  /** Evidencija aviona. */
  Map<String, Avion> avioni = new ConcurrentHashMap<>();

  /** Podaci o letovima. */
  Map<String, LetPodaci> podaciLetova = new ConcurrentHashMap<>();

  /** Evidencija inicijaliziranih letova. */
  Map<String, Let> letovi = new ConcurrentHashMap<>();

  /**
   * Glavna metoda programa.
   *
   * @param args argumenti komandne linije
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      return;
    }
    var program = new PosluziteljRezervacije();
    var nazivDatoteke = args[0];
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      program.kraj.set(true);
      program.executor.shutdown();
      try {
        if (!program.executor.awaitTermination(2L * program.pauzaDretve, TimeUnit.MILLISECONDS)) {
          program.executor.shutdownNow();
        }
      } catch (InterruptedException _) {
        program.executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }));
    program.pripremiKreni(nazivDatoteke);
  }

  /**
   * Učitava podatke i pokreće poslužitelje.
   *
   * @param nazivDatoteke naziv konfiguracijske datoteke
   */
  public void pripremiKreni(String nazivDatoteke) {
    if (!this.ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }
    if (!this.ucitajAlgoritam()) {
      return;
    }
    if (!this.ucitajPodatkeSustava()) {
      return;
    }
    this.semaforDretvi = new Semaphore(this.maksBrojDretvi);
    var builder = Thread.ofVirtual();
    var factory = builder.factory();
    this.executor = Executors.newThreadPerTaskExecutor(factory);
    var dretvaAdministracija = this.executor.submit(this::pokreniPosluziteljAdministracija);
    var dretvaKorisnici = this.executor.submit(this::pokreniPosluziteljKorisnici);
    var dretvaAvioTvrtke = this.executor.submit(this::pokreniPosluziteljAvioTvrtke);
    while (!dretvaAdministracija.isDone() || !dretvaKorisnici.isDone()
        || !dretvaAvioTvrtke.isDone()) {
      spavanjeDretve();
    }
  }

  /**
   * Pokreni posluzitelj administracija.
   */
  public void pokreniPosluziteljAdministracija() {
    try (ServerSocket ss = new ServerSocket(mreznaVrataAdmin, 0)) {
      while (!this.kraj.get()) {
        var mreznaUticnica = ss.accept();
        this.obradiAdministracija(mreznaUticnica);
      }
    } catch (IOException _) {
      // Kontrolirani prekid rada poslužitelja
    }
  }

  /**
   * Obrađuje administratorski zahtjev.
   *
   * @param mreznaUticnica mrežna utičnica administratora
   * @return {@code true} ako je zahtjev obrađen
   */
  public Boolean obradiAdministracija(Socket mreznaUticnica) {
    pauzaOstali.set(true);
    while (aktivniZahtjevi.get() > 0) {
      spavanjeDretve();
    }
    try (
        BufferedReader in = new BufferedReader(
            new InputStreamReader(mreznaUticnica.getInputStream(), StandardCharsets.UTF_8));
        PrintWriter out = new PrintWriter(
            new OutputStreamWriter(mreznaUticnica.getOutputStream(), StandardCharsets.UTF_8));) {
      String linija = in.readLine();
      mreznaUticnica.shutdownInput();
      komandeAdministracija(out, linija);
      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();
    } catch (Exception _) {
      // Kontrolirani prekid rada poslužitelja
    } finally {
      pauzaOstali.set(false);
    }
    return Boolean.TRUE;
  }

  /**
   * Obrađuje administratorske naredbe.
   *
   * @param out    izlazni tok za odgovor
   * @param linija primljena naredba
   */
  public void komandeAdministracija(PrintWriter out, String linija) {
    if (linija == null) {
      neispravnaKomandaError20(out);
      pauzaOstali.set(false);
      return;
    }
    String komanda = linija.trim();
    String[] dijelovi = regex.split(komanda);
    if (dijelovi.length == 2 && "REZERVACIJE".equals(dijelovi[0])
        && "PONIŠTI".equals(dijelovi[1])) {
      ponistiSveRezervacije();
      out.write("OK\n");
    } else if (dijelovi.length == 1 && "PING".equals(komanda)) {
      out.write("OK\n");
    } else if (dijelovi.length == 2 && "KRAJ".equals(dijelovi[0])) {
      obradiKrajAdmin(out, dijelovi[1]);
    } else {
      neispravnaKomandaError20(out);
    }
  }

  /**
   * Šalje odgovor za neispravnu naredbu.
   *
   * @param out izlazni tok za odgovor
   */
  private void neispravnaKomandaError20(PrintWriter out) {
    out.write(ERROR_20);
  }

  /**
   * Pokreni posluzitelj korisnici.
   */
  public void pokreniPosluziteljKorisnici() {
    try (ServerSocket ss = new ServerSocket(this.mreznaVrata, this.maksBrojCekaca)) {
      ss.setSoTimeout(this.pauzaDretve);
      while (!this.kraj.get()) {
        if (this.pauzaOstali.get()) {
          spavanjeDretve();
          continue;
        }
        prihvatiKorisnikaAkoMoguce(ss);
      }
    } catch (IOException _) {
      // Kontrolirani prekid rada poslužitelja
    }
  }

  /**
   * Prihvaća korisnika ako je to moguće.
   *
   * @param ss serverska utičnica
   */
  private void prihvatiKorisnikaAkoMoguce(ServerSocket ss) {
    try {
      var mreznaUticnica = ss.accept();
      if (this.pauzaOstali.get()) {
        mreznaUticnica.close();
        return;
      }
      this.executor.submit(() -> obradiKorisnik(mreznaUticnica));
    } catch (IOException _) {
      // Kontrolirani prekid rada poslužitelja
    }
  }

  /**
   * Obrađuje korisnički zahtjev.
   *
   * @param mreznaUticnica mrežna utičnica korisnika
   * @return {@code true} ako je zahtjev obrađen
   */
  public Boolean obradiKorisnik(Socket mreznaUticnica) {
    try {
      if (!semaforDretvi.tryAcquire(5, TimeUnit.SECONDS)) {
        mreznaUticnica.close();
        return Boolean.FALSE;
      }
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
      return Boolean.FALSE;
    } catch (IOException _) {
      return Boolean.FALSE;
    }
    this.aktivniZahtjevi.incrementAndGet();
    try {
      izvrsiObraduKorisnika(mreznaUticnica);
    } finally {
      this.aktivniZahtjevi.decrementAndGet();
      semaforDretvi.release();
    }
    return Boolean.TRUE;
  }

  /**
   * Izvršava obradu korisničkog zahtjeva.
   *
   * @param mreznaUticnica mrežna utičnica korisnika
   */
  private void izvrsiObraduKorisnika(Socket mreznaUticnica) {
    try {
      BufferedReader in = new BufferedReader(
          new InputStreamReader(mreznaUticnica.getInputStream(), StandardCharsets.UTF_8));
      PrintWriter out = new PrintWriter(
          new OutputStreamWriter(mreznaUticnica.getOutputStream(), StandardCharsets.UTF_8));
      String linija = in.readLine();
      mreznaUticnica.shutdownInput();
      komandeKorisnika(out, linija);
      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();
    } catch (Exception _) {
      // Kontrolirani prekid rada poslužitelja
    }
  }

  /**
   * Obrađuje korisničke naredbe.
   *
   * @param out    izlazni tok za odgovor
   * @param linija primljena naredba
   */
  public void komandeKorisnika(PrintWriter out, String linija) {
    String komanda = linija.trim();
    if (komanda == null || komanda.isEmpty()) {
      neispravnaKomandaError20(out);
      return;
    }
    String[] dijelovi = regex.split(komanda);
    switchKomandeKorisnika(out, komanda, dijelovi);
  }

  /**
   * Usmjerava obradu korisničkih naredbi prema odgovarajućoj metodi.
   *
   * @param out      izlazni tok za odgovor
   * @param komanda  puna naredba
   * @param dijelovi dijelovi naredbe
   */
  private void switchKomandeKorisnika(PrintWriter out, String komanda, String[] dijelovi) {
    switch (dijelovi[0]) {
      case "PING":
        if (dijelovi.length == 1) {
          out.println("OK");
        } else
          neispravnaKomandaError20(out);
        return;
      case "PAUZA":
        if (dijelovi.length == 2 && !dijelovi[1].isBlank()) {
          obradiPauzu(out, dijelovi[1], "ERROR 29");
        } else {
          neispravnaKomandaError20(out);
        }
        return;
      case "ISPIS":
        if (dijelovi.length == 4) {
          obradiIspisKorisnik(out, dijelovi[1], dijelovi[2], dijelovi[3]);
        } else {
          neispravnaKomandaError20(out);
        }
        return;
      case "REZERVIRAJ":
        if (dijelovi.length == 5 && !"GRUPA".equals(dijelovi[1])) {
          obradiRezervacijuKorisnik(out, dijelovi);
        } else if (komanda.startsWith("REZERVIRAJ GRUPA ")) {
          obradiRezervacijuGrupe(out, komanda);
        } else {
          neispravnaKomandaError20(out);
        }
        return;
      case "POTVRDI":
        if (dijelovi.length == 4) {
          obradiPotvrduRezervacije(out, dijelovi);
        } else {
          neispravnaKomandaError20(out);
        }
        return;
      default:
        neispravnaKomandaError20(out);
        break;
    }
  }

  /**
   * Pokreni posluzitelj avio tvrtke.
   */
  public void pokreniPosluziteljAvioTvrtke() {
    try (ServerSocket ss = new ServerSocket(this.mreznaVrataAvioTvrtke, this.maksBrojCekaca)) {
      ss.setSoTimeout(this.pauzaDretve);
      while (!this.kraj.get()) {
        if (this.pauzaOstali.get()) {
          spavanjeDretve();
          continue;
        }
        prihvatiAvioTvrtkaAkoMoguce(ss);
      }
    } catch (IOException _) {
      // Kontrolirani prekid rada poslužitelja
    }
  }

  /**
   * Prihvaća zahtjev avio tvrtke ako je to moguće.
   *
   * @param ss serverska utičnica
   */
  private void prihvatiAvioTvrtkaAkoMoguce(ServerSocket ss) {
    try {
      var mreznaUticnica = ss.accept();
      if (this.pauzaOstali.get()) {
        mreznaUticnica.close();
        return;
      }
      this.executor.submit(() -> obradiAvioTvrtka(mreznaUticnica));
    } catch (IOException _) {
      // Kontrolirani prekid čekanja
    }
  }

  /**
   * Obrađuje zahtjev avio tvrtke.
   *
   * @param mreznaUticnica mrežna utičnica avio tvrtke
   * @return {@code true} ako je zahtjev obrađen
   */
  public Boolean obradiAvioTvrtka(Socket mreznaUticnica) {
    try {
      if (!semaforDretvi.tryAcquire(pauzaDretve, TimeUnit.MILLISECONDS)) {
        mreznaUticnica.close();
        return Boolean.FALSE;
      }
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
      return Boolean.FALSE;
    } catch (IOException _) {
      return Boolean.FALSE;
    }
    this.aktivniZahtjevi.incrementAndGet();
    try {
      izvrsiObraduAvioTvrtke(mreznaUticnica);
    } finally {
      this.aktivniZahtjevi.decrementAndGet();
      semaforDretvi.release();
    }
    return Boolean.TRUE;
  }

  /**
   * Izvršava obradu zahtjeva avio tvrtke.
   *
   * @param mreznaUticnica mrežna utičnica avio tvrtke
   */
  private void izvrsiObraduAvioTvrtke(Socket mreznaUticnica) {
    try {
      BufferedReader in = new BufferedReader(
          new InputStreamReader(mreznaUticnica.getInputStream(), StandardCharsets.UTF_8));
      PrintWriter out = new PrintWriter(
          new OutputStreamWriter(mreznaUticnica.getOutputStream(), StandardCharsets.UTF_8));
      String linija = in.readLine();
      mreznaUticnica.shutdownInput();
      komandeAvioTvrtke(out, linija);
      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();
    } catch (Exception _) {
      // Kontrolirani prekid rada poslužitelja
    }
  }

  /**
   * Obrađuje naredbe avio tvrtki.
   *
   * @param out    izlazni tok za odgovor
   * @param linija primljena naredba
   */
  public void komandeAvioTvrtke(PrintWriter out, String linija) {
    if (linija == null) {
      neispravnaKomandaError20(out);
      return;
    }
    String komanda = linija.trim();
    String[] dijelovi = regex.split(komanda);
    if (dijelovi.length == 1 && "PING".equals(komanda)) {
      out.write("OK\n");
    } else if (dijelovi.length == 3 && "ISPIS".equals(dijelovi[0])) {
      obradiIspis(out, dijelovi[1], dijelovi[2]);
    } else if (dijelovi.length == 6 && "ZATVORI".equals(dijelovi[0])) {
      obradiZatvoriRedove(out, dijelovi[1], dijelovi[2], dijelovi[3], dijelovi[4], dijelovi[5]);
    } else if (dijelovi.length == 5 && "ZATVORI".equals(dijelovi[0])) {
      obradiZatvoriDatume(out, dijelovi[1], dijelovi[2], dijelovi[3], dijelovi[4]);
    } else if (komanda.startsWith("INICIJALIZIRAJ ")) {
      obradiInicijaliziraj(out, komanda);
    } else {
      neispravnaKomandaError20(out);
    }
  }

  /**
   * Obrađuje naredbu za gašenje poslužitelja.
   *
   * @param out        izlazni tok za odgovor
   * @param uneseniKod uneseni kod za gašenje
   */
  private void obradiKrajAdmin(PrintWriter out, String uneseniKod) {
    if (uneseniKod.equals(this.kodZaKraj)) {
      this.kraj.set(true);
      out.write("OK\n");
    } else {
      out.write("ERROR 28\n");
    }
  }

  /**
   * Obrađuje korisnički zahtjev za dohvat identifikatora leta.
   *
   * @param out        izlazni tok za odgovor
   * @param idUlaznice identifikator ulaznice
   * @param oznakaLeta oznaka leta
   * @param datumTekst datum leta u tekstualnom obliku
   */
  public void obradiIspisKorisnik(PrintWriter out, String idUlaznice, String oznakaLeta,
      String datumTekst) {
    if (!provjeriUlaznicu(idUlaznice)) {
      neispravnaUlaznicaError21(out);
      return;
    }
    try {
      LocalDate datum = LocalDate.parse(datumTekst);
      String oznakaOciscena = oznakaLeta.replace("\"", "");
      String idLeta = pronadiIdLeta(oznakaOciscena, datum);
      if (idLeta == null) {
        neispravniPodaciLetaError27(out);
        return;
      }
      out.write("OK " + idLeta + "\n");
    } catch (Exception _) {
      neispravniPodaciLetaError27(out);
    }
  }

  /**
   * Šalje odgovor za neispravne podatke leta.
   *
   * @param out izlazni tok za odgovor
   */
  private void neispravniPodaciLetaError27(PrintWriter out) {
    out.write("ERROR 27\n");
  }

  /**
   * Šalje odgovor za neispravnu ulaznicu.
   *
   * @param out izlazni tok za odgovor
   */
  private void neispravnaUlaznicaError21(PrintWriter out) {
    out.write("ERROR 21\n");
  }

  /**
   * Pronalazi identifikator leta prema oznaci i datumu.
   *
   * @param oznakaLeta oznaka leta
   * @param datum      datum polijetanja
   * @return identifikator leta ili {@code null} ako ne postoji
   */
  private String pronadiIdLeta(String oznakaLeta, LocalDate datum) {
    for (Map.Entry<String, Let> zapis : this.letovi.entrySet()) {
      Let let = zapis.getValue();
      if (let.letPodaci().oznakaLeta().equals(oznakaLeta) && let.datumPolijetanja().equals(datum)) {
        return zapis.getKey();
      }
    }
    return null;
  }

  /**
   * Obrađuje korisnički zahtjev za rezervaciju sjedala.
   *
   * @param out      izlazni tok za odgovor
   * @param dijelovi dijelovi naredbe
   */
  public void obradiRezervacijuKorisnik(PrintWriter out, String[] dijelovi) {
    String idUlaznice = dijelovi[1];
    String idLeta = dijelovi[2];
    String putnik = dijelovi[3];
    String razredTekst = dijelovi[4];
    if (idUlaznice.isEmpty() || idLeta.isEmpty() || putnik.isEmpty() || razredTekst.isEmpty()) {
      neispravnaKomandaError20(out);
      return;
    }
    if (!provjeriUlaznicu(idUlaznice)) {
      neispravnaUlaznicaError21(out);
      return;
    }
    Let let = this.letovi.get(idLeta);
    if (let == null || !rezervacije.containsKey(idLeta)) {
      neispravniLetError22(out);
      return;
    }
    izvrsiRezervacijuKorisnik(out, let, idLeta, putnik, razredTekst);
  }

  /**
   * Šalje odgovor za neispravan let.
   *
   * @param out izlazni tok za odgovor
   */
  private void neispravniLetError22(PrintWriter out) {
    out.write("ERROR 22\n");
  }

  /**
   * Izvršava korisničku rezervaciju sjedala.
   *
   * @param out         izlazni tok za odgovor
   * @param let         let za koji se radi rezervacija
   * @param idLeta      identifikator leta
   * @param putnik      ime putnika
   * @param razredTekst tekstualni zapis razreda sjedala
   */
  private void izvrsiRezervacijuKorisnik(PrintWriter out, Let let, String idLeta, String putnik,
      String razredTekst) {
    try {
      RazredSjedala razred = mapirajRazred(razredTekst);
      RezervacijaSjedala rezervacija = this.algoritam.rezervirajSjedalo(let, putnik, razred);
      zamijeniRezervaciju(idLeta, rezervacija);
      out.write("OK " + rezervacija.id() + "\n");
    } catch (Exception _) {
      out.write("ERROR 23\n");
    }
  }

  /**
   * Obrađuje korisnički zahtjev za potvrdu rezervacije.
   *
   * @param out      izlazni tok za odgovor
   * @param dijelovi dijelovi naredbe
   */
  public void obradiPotvrduRezervacije(PrintWriter out, String[] dijelovi) {
    String idUlaznice = dijelovi[1];
    String idRezervacije = dijelovi[2];
    String putnik = dijelovi[3];
    if (idUlaznice.isEmpty() || idRezervacije.isEmpty() || putnik.isEmpty()) {
      neispravnaKomandaError20(out);
      return;
    }
    if (!provjeriUlaznicu(idUlaznice)) {
      neispravnaUlaznicaError21(out);
      return;
    }
    RezervacijaSjedala trenutna = pronadiRezervaciju(idRezervacije);
    if (trenutna == null) {
      out.write("ERROR 24\n");
      return;
    }
    izvrsiPotvrduRezervacije(out, trenutna, putnik);
  }

  /**
   * Izvršava potvrdu rezervacije.
   *
   * @param out      izlazni tok za odgovor
   * @param trenutna postojeća rezervacija
   * @param putnik   ime putnika
   */
  private void izvrsiPotvrduRezervacije(PrintWriter out, RezervacijaSjedala trenutna,
      String putnik) {
    if (!putnik.equals(trenutna.putnik())
        || trenutna.statusRezervacije() != StatusRezervacijeSjedala.KREIRANA) {
      out.write("ERROR 25\n");
      return;
    }
    try {
      RezervacijaSjedala potvrda = this.algoritam.potvrdiRezervacijuSjedala(trenutna);
      zamijeniRezervaciju(trenutna.sjedalo().let().id(), potvrda);
      out.write("OK\n");
    } catch (ProblemKodRezervacije _) {
      out.write("ERROR 25\n");
    }
  }

  /**
   * Obrađuje zahtjev za grupnu rezervaciju.
   *
   * @param out     izlazni tok za odgovor
   * @param komanda puna naredba
   */
  public void obradiRezervacijuGrupe(PrintWriter out, String komanda) {
    if (!komanda.contains("[") || !komanda.contains("]")) {
      neispravnaKomandaError20(out);
      return;
    }
    int otvorena = komanda.indexOf('[');
    int zatvorena = komanda.indexOf(']');
    String zaglavlje = komanda.substring(0, otvorena).trim();
    String[] dijeloviZaglavlja = zaglavlje.split("\\s+");
    if (dijeloviZaglavlja.length != 4 || !"REZERVIRAJ".equals(dijeloviZaglavlja[0])
        || !"GRUPA".equals(dijeloviZaglavlja[1])) {
      neispravnaKomandaError20(out);
      return;
    }
    String rep = komanda.substring(zatvorena + 1).trim();
    if (rep.isEmpty()) {
      neispravnaKomandaError20(out);
      return;
    }
    izvrsiRezervacijuGrupe(out, komanda, dijeloviZaglavlja, otvorena, zatvorena, rep);
  }

  /**
   * Izvršava grupnu rezervaciju nakon osnovne provjere naredbe.
   *
   * @param out               izlazni tok za odgovor
   * @param komanda           puna naredba
   * @param dijeloviZaglavlja dijelovi zaglavlja naredbe
   * @param otvorena          indeks znaka '['
   * @param zatvorena         indeks znaka ']'
   * @param razredTekst       tekstualni zapis razreda sjedala
   */
  private void izvrsiRezervacijuGrupe(PrintWriter out, String komanda, String[] dijeloviZaglavlja,
      int otvorena, int zatvorena, String razredTekst) {
    try {
      String idUlaznice = dijeloviZaglavlja[2];
      String idLeta = dijeloviZaglavlja[3];
      List<String> putnici = parsirajPutnike(komanda, otvorena, zatvorena);
      if (putnici.isEmpty()) {
        neispravnaKomandaError20(out);
        return;
      }
      if (!provjeriUlaznicu(idUlaznice)) {
        neispravnaUlaznicaError21(out);
        return;
      }
      izvrsiRezervacijuGrupeZaLet(out, idLeta, putnici, razredTekst);
    } catch (Exception _) {
      out.write("ERROR 23\n");
    }
  }

  /**
   * Izvršava grupnu rezervaciju za zadani let.
   *
   * @param out         izlazni tok za odgovor
   * @param idLeta      identifikator leta
   * @param putnici     popis putnika
   * @param razredTekst tekstualni zapis razreda sjedala
   * @throws ProblemKodRezervacije ako rezervaciju nije moguće napraviti
   */
  private void izvrsiRezervacijuGrupeZaLet(PrintWriter out, String idLeta, List<String> putnici,
      String razredTekst) throws ProblemKodRezervacije {
    Let let = this.letovi.get(idLeta);
    if (let == null || !rezervacije.containsKey(idLeta)) {
      neispravniLetError22(out);
      return;
    }
    RazredSjedala razred = mapirajRazred(razredTekst);
    List<RezervacijaSjedala> rezultat = this.algoritam.rezervirajSjedalaZaGrupu(let, putnici, razred);
    for (RezervacijaSjedala rezervacija : rezultat) {
      zamijeniRezervaciju(idLeta, rezervacija);
    }
    out.write(sastaviOdgovorGrupe(rezultat) + "\n");
  }

  /**
   * Sastavlja odgovor za grupnu rezervaciju.
   *
   * @param rezultat kreirane rezervacije
   * @return tekst odgovora
   */
  private String sastaviOdgovorGrupe(List<RezervacijaSjedala> rezultat) {
    StringBuilder odgovor = new StringBuilder("OK");
    for (RezervacijaSjedala rezervacija : rezultat) {
      odgovor.append(" ").append(rezervacija.id());
    }
    return odgovor.toString();
  }

  /**
   * Parsira popis putnika iz grupne naredbe.
   *
   * @param komanda   puna naredba
   * @param otvorena  indeks znaka '['
   * @param zatvorena indeks znaka ']'
   * @return popis putnika
   */
  private List<String> parsirajPutnike(String komanda, int otvorena, int zatvorena) {
    String[] nizPutnika = komanda.substring(otvorena + 1, zatvorena).trim().split("\\s+");
    List<String> putnici = new ArrayList<>();
    for (String putnik : nizPutnika) {
      if (!putnik.isBlank()) {
        putnici.add(putnik);
      }
    }
    return putnici;
  }

  /**
   * Obrađuje naredbu za pauzu.
   *
   * @param out        izlazni tok za odgovor
   * @param vrijednost trajanje pauze u milisekundama
   * @param kodGreske  kod greške pri prekidu čekanja
   */
  public void obradiPauzu(PrintWriter out, String vrijednost, String kodGreske) {
    try {
      Thread.sleep(Integer.parseInt(vrijednost));
      out.write("OK\n");
    } catch (IllegalArgumentException _) {
      neispravnaKomandaError20(out);
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
      out.write(kodGreske + "\n");
    }
  }

  /**
   * Provjerava valjanost ulaznice na kontrolnom poslužitelju.
   *
   * @param idUlaznice identifikator ulaznice
   * @return {@code true} ako je ulaznica valjana
   */
  public boolean provjeriUlaznicu(String idUlaznice) {
    try (Socket s = new Socket(this.adresaKontrola, this.mreznaVrataKontrola);
        PrintWriter out = new PrintWriter(
            new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))) {
      out.println("ULAZNICA PROVJERI " + idUlaznice);
      String odgovor = in.readLine();
      return odgovor != null && odgovor.startsWith("OK");
    } catch (Exception _) {
      return false;
    }
  }

  /**
   * Učitava konfiguraciju poslužitelja.
   *
   * @param nazivDatoteke naziv konfiguracijske datoteke
   * @return {@code true} ako je konfiguracija uspješno učitana
   */
  public boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      this.mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata"));
      this.mreznaVrataAdmin = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataAdministracija"));
      this.mreznaVrataAvioTvrtke = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataAvioTvrtke"));
      this.mreznaVrataKontrola = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKontrola"));
      this.adresaKontrola = this.konfig.dajPostavku("adresaKontrola");
      return ucitajKonfiguracijuNastavak();
    } catch (NeispravnaKonfiguracija _) {
      return false;
    }
  }

  /**
   * Učitava preostale postavke konfiguracije.
   *
   * @return {@code true} ako su postavke uspješno učitane
   */
  private boolean ucitajKonfiguracijuNastavak() {
    this.datotekaAvioTvrtke = this.konfig.dajPostavku("datoteka_avioTvrtke");
    this.datotekaAvioni = this.konfig.dajPostavku("datoteka_avioni");
    this.datotekaLetovi = this.konfig.dajPostavku("datoteka_letovi");
    this.datumOd = LocalDate.parse(this.konfig.dajPostavku("datumOd"));
    this.datumDo = LocalDate.parse(this.konfig.dajPostavku("datumDo"));
    this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));
    this.maksBrojDretvi = Integer.parseInt(this.konfig.dajPostavku("maksBrojDretvi"));
    this.maksBrojCekaca = Integer.parseInt(this.konfig.dajPostavku("maksBrojCekaca"));
    this.odredivanjeSjedala = this.konfig.dajPostavku("odredivanjeSjedala");
    return true;
  }

  /**
   * Spavanje dretve.
   */
  public void spavanjeDretve() {
    try {
      Thread.sleep(this.pauzaDretve);
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Učitava i inicijalizira algoritam za određivanje sjedala.
   *
   * @return {@code true} ako je algoritam uspješno učitan
   */
  public boolean ucitajAlgoritam() {
    try {
      Class<?> klasa = Class.forName(this.odredivanjeSjedala);
      this.algoritam = (OdredivanjeSjedala) klasa.getDeclaredConstructor().newInstance();
      return this.algoritam.inicijaliziraj();
    } catch (Exception _) {
      return false;
    }
  }

  /**
   * Vraća aktivni algoritam za određivanje sjedala.
   *
   * @return algoritam za određivanje sjedala
   */
  public OdredivanjeSjedala dajAlgoritam() {
    return this.algoritam;
  }

  /**
   * Učitava sve podatke potrebne za rad sustava.
   *
   * @return {@code true} ako su podaci uspješno učitani
   */
  public boolean ucitajPodatkeSustava() {
    try {
      Path baza = odrediBazuDatoteka();
      ucitajAvioTvrtke(baza.resolve(this.datotekaAvioTvrtke));
      ucitajAvione(baza.resolve(this.datotekaAvioni));
      ucitajPodatkeLetova(baza.resolve(this.datotekaLetovi));
      inicijalizirajLetoveIRazmjestajSjedala(baza);
      return true;
    } catch (Exception _) {
      return false;
    }
  }

  /**
   * Određuje baznu mapu za učitavanje datoteka.
   *
   * @return putanja do bazne mape
   */
  public Path odrediBazuDatoteka() {
    String nazivKonfig = this.konfig.dajPostavku("datoteka");
    if (nazivKonfig == null || nazivKonfig.isBlank()) {
      return Path.of("").toAbsolutePath();
    }
    Path putanja = Path.of(nazivKonfig);
    if (putanja.getParent() != null) {
      return putanja.toAbsolutePath().getParent();
    }
    return Path.of("").toAbsolutePath();
  }

  /**
   * Učitava avio tvrtke iz datoteke.
   *
   * @param datoteka putanja do datoteke
   * @throws IOException ako dođe do pogreške pri čitanju datoteke
   */
  public void ucitajAvioTvrtke(Path datoteka) throws IOException {
    List<String> linije = Files.readAllLines(datoteka, StandardCharsets.UTF_8);
    for (int i = 1; i < linije.size(); i++) {
      String linija = linije.get(i).trim();
      if (linija.isEmpty()) {
        continue;
      }
      String[] dijelovi;
      if (linija.contains(";")) {
        dijelovi = linija.split(";", 2);
      } else if (linija.contains(",")) {
        dijelovi = linija.split(",", 2);
      } else {
        continue;
      }
      if (dijelovi.length < 2) {
        continue;
      }
      String id = dijelovi[0].trim();
      String naziv = dijelovi[1].trim();
      this.avioTvrtke.putIfAbsent(id, new AvioTvrtka(id, naziv, new HashMap<>()));
    }
  }

  /**
   * Učitava avione iz datoteke.
   *
   * @param datoteka putanja do datoteke
   * @throws IOException ako dođe do pogreške pri čitanju datoteke
   */
  public void ucitajAvione(Path datoteka) throws IOException {
    List<String> linije = Files.readAllLines(datoteka, StandardCharsets.UTF_8);
    for (int i = 1; i < linije.size(); i++) {
      String linija = linije.get(i).trim();
      List<String> dijelovi = parsirajCsvRed(linija);
      if (linija.isEmpty() || dijelovi.size() < 3) {
        continue;
      }
      dodajAvionIzDijelova(dijelovi);
    }
  }

  /**
   * Dodaje avion iz parsiranih dijelova retka.
   *
   * @param dijelovi dijelovi retka
   */
  private void dodajAvionIzDijelova(List<String> dijelovi) {
    String id = dijelovi.get(0).trim();
    String datotekaSjedala = dijelovi.get(1).trim();
    String naziv = dijelovi.get(2).trim();
    Map<String, String> specifikacije = new HashMap<>();
    if (dijelovi.size() > 3 && !dijelovi.get(3).isBlank()) {
      for (String dio : dijelovi.get(3).split(",")) {
        String[] par = dio.split(":", 2);
        if (par.length == 2) {
          specifikacije.put(par[0].trim(), par[1].trim());
        }
      }
    }
    this.avioni.putIfAbsent(id, new Avion(id, datotekaSjedala, naziv, specifikacije));
  }

  /**
   * Učitava podatke letova iz datoteke.
   *
   * @param datoteka putanja do datoteke
   * @throws IOException ako dođe do pogreške pri čitanju datoteke
   */
  public void ucitajPodatkeLetova(Path datoteka) throws IOException {
    List<String> linije = Files.readAllLines(datoteka, StandardCharsets.UTF_8);
    for (int i = 1; i < linije.size(); i++) {
      String linija = linije.get(i).trim();
      if (linija.isEmpty()) {
        continue;
      }
      List<String> dijelovi = parsirajCsvRed(linija);
      if (dijelovi.size() < 7) {
        continue;
      }
      dodajLetPodatkeIzDijelova(dijelovi);
    }
  }

  /**
   * Dodaje podatke o letu iz parsiranih dijelova retka.
   *
   * @param dijelovi dijelovi retka
   */
  private void dodajLetPodatkeIzDijelova(List<String> dijelovi) {
    String oznakaLeta = dijelovi.get(0).trim();
    String idTvrtke = dijelovi.get(1).trim();
    String polazni = dijelovi.get(2).trim();
    String odredisni = dijelovi.get(3).trim();
    String vrijeme = dijelovi.get(5).trim();
    String idAviona = dijelovi.get(6).trim();
    AvioTvrtka tvrtka = this.avioTvrtke.get(idTvrtke);
    Avion avion = this.avioni.get(idAviona);
    if (tvrtka == null || avion == null) {
      return;
    }
    tvrtka.dodajAvion(avion);
    LetPodaci letPodaci = new LetPodaci(oznakaLeta, tvrtka, LocalTime.parse(vrijeme), polazni, odredisni, avion);
    this.podaciLetova.putIfAbsent(oznakaLeta + "_" + vrijeme, letPodaci);
  }

  /**
   * Inicijalizira letove i raspored sjedala.
   *
   * @param baza bazna putanja za datoteke
   * @throws IOException ako dođe do pogreške pri čitanju datoteke
   */
  public void inicijalizirajLetoveIRazmjestajSjedala(Path baza) throws IOException {
    for (LetPodaci podaci : this.podaciLetova.values()) {
      for (LocalDate datum = this.datumOd; !datum.isAfter(this.datumDo); datum = datum.plusDays(1)) {
        inicijalizirajJedanLet(podaci, datum, baza);
      }
    }
  }

  /**
   * Postavlja sjedala u algoritam određivanja sjedala.
   *
   * @param idLeta identifikator leta
   * @param redovi raspored sjedala po redovima
   */
  public void postaviSjedalaUAlgoritam(String idLeta,
      Map<Integer, Map<String, RezervacijaSjedala>> redovi) {
    try {
      this.algoritam.getClass().getMethod("dodajSjedala", String.class, Map.class)
          .invoke(this.algoritam, idLeta, redovi);
    } catch (Exception _) {
      // Algoritam ne podržava dodajSjedala – preskačemo
    }
  }

  /**
   * Učitava raspored sjedala za avion.
   *
   * @param let      let za koji se učitavaju sjedala
   * @param datoteka putanja do datoteke
   * @return raspored sjedala po redovima
   * @throws IOException ako dođe do pogreške pri čitanju datoteke
   */
  public Map<Integer, Map<String, RezervacijaSjedala>> ucitajSjedalaZaAvion(Let let, Path datoteka)
      throws IOException {
    Map<Integer, Map<String, RezervacijaSjedala>> redovi = new HashMap<>();
    List<String> linije = Files.readAllLines(datoteka, StandardCharsets.UTF_8);
    for (int i = 1; i < linije.size(); i++) {
      String linija = linije.get(i).trim();
      if (linija.isEmpty()) {
        continue;
      }
      // Podrška za delimiter ; ili ,
      String[] dijelovi;
      if (linija.contains(";")) {
        dijelovi = linija.split(";", 3);
      } else if (linija.contains(",")) {
        dijelovi = linija.split(",", 3);
      } else {
        continue;
      }
      if (dijelovi.length < 3) {
        continue;
      }
      dodajRedSjedalaUMapu(redovi, let, dijelovi);
    }
    return redovi;
  }

  /**
   * Dodaje jedan red sjedala u mapu.
   *
   * @param redovi   raspored sjedala po redovima
   * @param let      let kojem sjedala pripadaju
   * @param dijelovi dijelovi retka
   */
  private void dodajRedSjedalaUMapu(Map<Integer, Map<String, RezervacijaSjedala>> redovi, Let let,
      String[] dijelovi) {
    int red = Integer.parseInt(dijelovi[0].trim());
    RazredSjedala razred = mapirajRazred(dijelovi[1].trim());
    String oznake = dijelovi[2].trim().replace("\"", "");
    Map<String, RezervacijaSjedala> sjedala = new LinkedHashMap<>();
    for (char c : oznake.toCharArray()) {
      if (c == '_') {
        continue;
      }
      if (Character.isLetterOrDigit(c)) {
        String oznaka = String.valueOf(c);
        LetSjedalo sjedalo = new LetSjedalo(let, razred, red, oznaka);
        RezervacijaSjedala rez = new RezervacijaSjedala(let.id() + ":" + red + oznaka, sjedalo,
            null, LocalDateTime.now(), StatusRezervacijeSjedala.SLOBODNA);
        sjedala.put(oznaka, rez);
      }
    }
    if (!sjedala.isEmpty()) {
      redovi.put(red, sjedala);
    }
  }

  /**
   * Parsira jedan CSV redak.
   *
   * @param linija sadržaj retka
   * @return popis izdvojenih vrijednosti
   */
  public List<String> parsirajCsvRed(String linija) {
    List<String> rezultat = new ArrayList<>();
    StringBuilder trenutni = new StringBuilder();
    boolean podNavodnicima = false;
    for (int i = 0; i < linija.length(); i++) {
      char c = linija.charAt(i);
      if (c == '"') {
        podNavodnicima = !podNavodnicima;
        continue;
      }
      if (c == ',' && !podNavodnicima) {
        rezultat.add(trenutni.toString().trim());
        trenutni.setLength(0);
        continue;
      }
      trenutni.append(c);
    }
    rezultat.add(trenutni.toString().trim());
    return rezultat;
  }

  /**
   * Mapira tekstualnu oznaku u razred sjedala.
   *
   * @param vrijednost tekstualna oznaka razreda
   * @return odgovarajući razred sjedala
   */
  public RazredSjedala mapirajRazred(String vrijednost) {
    if ("P".equals(vrijednost) || "POSLOVNI".equalsIgnoreCase(vrijednost)) {
      return RazredSjedala.POSLOVNI;
    }
    if ("E".equals(vrijednost) || "EKONOMSKI".equalsIgnoreCase(vrijednost)) {
      return RazredSjedala.EKONOMSKI;
    }
    throw new IllegalArgumentException("Nepoznat razred: " + vrijednost);
  }

  /**
   * Zamjenjuje postojeću rezervaciju novom vrijednošću.
   *
   * @param idLeta identifikator leta
   * @param nova   nova rezervacija
   */
  public synchronized void zamijeniRezervaciju(String idLeta, RezervacijaSjedala nova) {
    List<RezervacijaSjedala> lista = this.rezervacije.get(idLeta);
    if (lista == null) {
      return;
    }
    synchronized (lista) {
      for (int i = 0; i < lista.size(); i++) {
        RezervacijaSjedala postojeca = lista.get(i);
        if (postojeca.sjedalo().red() == nova.sjedalo().red()
            && postojeca.sjedalo().oznakaSjedala().equals(nova.sjedalo().oznakaSjedala())) {
          lista.set(i, nova);
          return;
        }
      }
      lista.add(nova);
    }
  }

  /**
   * Pronalazi rezervaciju prema identifikatoru.
   *
   * @param idRezervacije identifikator rezervacije
   * @return pronađena rezervacija sjedala ili {@code null}
   */
  public synchronized RezervacijaSjedala pronadiRezervaciju(String idRezervacije) {
    for (List<RezervacijaSjedala> lista : this.rezervacije.values()) {
      synchronized (lista) {
        for (RezervacijaSjedala rezervacija : lista) {
          if (rezervacija.id().equals(idRezervacije)) {
            return rezervacija;
          }
        }
      }
    }
    return null;
  }

  /**
   * Obrađuje zahtjev za ispis rezervacija leta.
   *
   * @param out        izlazni tok za odgovor
   * @param idUlaznice identifikator ulaznice
   * @param idLeta     identifikator leta
   */
  public void obradiIspis(PrintWriter out, String idUlaznice, String idLeta) {
    if (!provjeriUlaznicu(idUlaznice)) {
      neispravnaUlaznicaError21(out);
      return;
    }
    List<RezervacijaSjedala> lista = this.rezervacije.get(idLeta);
    if (lista == null) {
      neispravniLetError22(out);
      return;
    }
    List<RezervacijaSjedala> kopija;
    synchronized (lista) {
      kopija = new ArrayList<>(lista);
    }
    kopija.sort(Comparator.comparing((RezervacijaSjedala r) -> r.sjedalo().red())
        .thenComparing(r -> r.sjedalo().oznakaSjedala()));
    out.write("OK " + serijalizirajRezervacije(kopija) + "\n");
  }

  /**
   * Serijalizira popis rezervacija u JSON zapis.
   *
   * @param lista popis rezervacija
   * @return JSON zapis rezervacija
   */
  private String serijalizirajRezervacije(List<RezervacijaSjedala> lista) {
    StringBuilder json = new StringBuilder("[");
    for (int i = 0; i < lista.size(); i++) {
      if (i > 0) {
        json.append(",");
      }
      json.append(serijalizirajJednuRezervaciju(lista.get(i)));
    }
    json.append("]");
    return json.toString();
  }

  /**
   * Serijalizira jednu rezervaciju u JSON zapis.
   *
   * @param r rezervacija za serijalizaciju
   * @return JSON zapis rezervacije
   */
  private String serijalizirajJednuRezervaciju(RezervacijaSjedala r) {
    Let let = r.sjedalo().let();
    LetPodaci lp = let.letPodaci();
    StringBuilder sb = new StringBuilder();
    sb.append("{\"id\":\"").append(r.id()).append("\"");
    sb.append(",\"sjedalo\":{\"let\":{\"id\":\"").append(let.id()).append("\"");
    sb.append(",\"letPodaci\":").append(serijalizirajLetPodaci(lp));
    sb.append(",\"datumPolijetanja\":\"").append(let.datumPolijetanja()).append("\"}");
    sb.append(",\"razred\":\"").append(r.sjedalo().razred()).append("\"");
    sb.append(",\"red\":").append(r.sjedalo().red());
    sb.append(",\"oznakaSjedala\":\"").append(r.sjedalo().oznakaSjedala()).append("\"}");
    sb.append(",\"putnik\":").append(serijalizirajPutnik(r.putnik()));
    sb.append(",\"vrijemeRezervacije\":\"").append(r.vrijemeRezervacije()).append("\"");
    sb.append(",\"statusRezervacije\":\"").append(r.statusRezervacije()).append("\"}");
    return sb.toString();
  }

  /**
   * Serijalizira podatke o letu u JSON zapis.
   *
   * @param lp podaci o letu
   * @return JSON zapis podataka o letu
   */
  private String serijalizirajLetPodaci(LetPodaci lp) {
    StringBuilder sb = new StringBuilder("{");
    sb.append("\"oznakaLeta\":\"").append(lp.oznakaLeta()).append("\"");
    sb.append(",\"avioTvrtka\":{\"id\":\"").append(lp.avioTvrtka().id()).append("\"");
    sb.append(",\"naziv\":\"").append(lp.avioTvrtka().naziv()).append("\"}");
    sb.append(",\"vrijemePolijetanja\":\"").append(lp.vrijemePolijetanja()).append("\"");
    sb.append(",\"polazniAerodrom\":\"").append(lp.polazniAerodrom()).append("\"");
    sb.append(",\"odredisniAerodrom\":\"").append(lp.odredisniAerodrom()).append("\"");
    sb.append(",\"avion\":{\"id\":\"").append(lp.avion().id()).append("\"");
    sb.append(",\"model\":\"").append(lp.avion().naziv()).append("\"}}");
    return sb.toString();
  }

  /**
   * Serijalizira ime putnika.
   *
   * @param putnik ime putnika
   * @return JSON vrijednost putnika
   */
  private String serijalizirajPutnik(String putnik) {
    if (putnik == null) {
      return "null";
    }
    return "\"" + putnik + "\"";
  }

  /**
   * Obrađuje zahtjev za zatvaranje raspona redova.
   *
   * @param out         izlazni tok za odgovor
   * @param idUlaznice  identifikator ulaznice
   * @param avioTvrtka  identifikator avio tvrtke
   * @param idLeta      identifikator leta
   * @param odRedaTekst početni red u tekstualnom obliku
   * @param doRedaTekst završni red u tekstualnom obliku
   */
  public void obradiZatvoriRedove(PrintWriter out, String idUlaznice, String avioTvrtka,
      String idLeta, String odRedaTekst, String doRedaTekst) {
    if (!provjeriUlaznicu(idUlaznice)) {
      neispravnaUlaznicaError21(out);
      return;
    }
    Let let = this.letovi.get(idLeta);
    if (let == null || !let.letPodaci().avioTvrtka().id().equals(avioTvrtka)) {
      neispravniLetError22(out);
      return;
    }
    izvrsiZatvaranjeRedova(out, idLeta, odRedaTekst, doRedaTekst);
  }

  /**
   * Izvršava zatvaranje raspona redova.
   *
   * @param out         izlazni tok za odgovor
   * @param idLeta      identifikator leta
   * @param odRedaTekst početni red u tekstualnom obliku
   * @param doRedaTekst završni red u tekstualnom obliku
   */
  private void izvrsiZatvaranjeRedova(PrintWriter out, String idLeta, String odRedaTekst,
      String doRedaTekst) {
    int odReda;
    int doReda;
    try {
      odReda = Integer.parseInt(odRedaTekst);
      doReda = Integer.parseInt(doRedaTekst);
    } catch (NumberFormatException _) {
      neispravnaKomandaError20(out);
      return;
    }
    if (odReda > doReda) {
      neispravnaKomandaError20(out);
      return;
    }
    izvrsiZatvaranjeRedovaURasponu(out, idLeta, odReda, doReda);
  }

  /**
   * Izvršava zatvaranje redova u zadanom rasponu.
   *
   * @param out    izlazni tok za odgovor
   * @param idLeta identifikator leta
   * @param odReda početni red
   * @param doReda završni red
   */
  private void izvrsiZatvaranjeRedovaURasponu(PrintWriter out, String idLeta, int odReda,
      int doReda) {
    List<RezervacijaSjedala> lista = this.rezervacije.get(idLeta);
    if (lista == null) {
      neispravniLetError22(out);
      return;
    }
    synchronized (lista) {
      if (imaAktivnihURasponuRedova(lista, odReda, doReda)) {
        out.write("ERROR 26\n");
        return;
      }
      zatvoriSjedalaURasponuRedova(lista, odReda, doReda);
    }
    out.write("OK\n");
  }

  /**
   * Provjerava postoje li aktivne rezervacije u rasponu redova.
   *
   * @param lista  popis rezervacija
   * @param odReda početni red
   * @param doReda završni red
   * @return {@code true} ako u rasponu postoji aktivna rezervacija
   */
  private boolean imaAktivnihURasponuRedova(List<RezervacijaSjedala> lista, int odReda,
      int doReda) {
    for (RezervacijaSjedala rezervacija : lista) {
      int red = rezervacija.sjedalo().red();
      if (red >= odReda && red <= doReda
          && (rezervacija.statusRezervacije() == StatusRezervacijeSjedala.KREIRANA
              || rezervacija.statusRezervacije() == StatusRezervacijeSjedala.POTVRDENA)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Zatvara sjedala u rasponu redova.
   *
   * @param lista  popis rezervacija
   * @param odReda početni red
   * @param doReda završni red
   */
  private void zatvoriSjedalaURasponuRedova(List<RezervacijaSjedala> lista, int odReda,
      int doReda) {
    for (int i = 0; i < lista.size(); i++) {
      RezervacijaSjedala rezervacija = lista.get(i);
      int red = rezervacija.sjedalo().red();
      if (red >= odReda && red <= doReda) {
        lista.set(i,
            new RezervacijaSjedala(rezervacija.id(), rezervacija.sjedalo(), rezervacija.putnik(),
                rezervacija.vrijemeRezervacije(), StatusRezervacijeSjedala.ZATVORENA));
      }
    }
  }

  /**
   * Obrađuje zahtjev za zatvaranje sjedala u rasponu datuma.
   *
   * @param out        izlazni tok za odgovor
   * @param idUlaznice identifikator ulaznice
   * @param avioTvrtka identifikator avio tvrtke
   * @param odDatuma   početni datum
   * @param doDatuma   završni datum
   */
  public void obradiZatvoriDatume(PrintWriter out, String idUlaznice, String avioTvrtka,
      String odDatuma, String doDatuma) {
    if (!provjeriUlaznicu(idUlaznice)) {
      neispravnaUlaznicaError21(out);
      return;
    }
    try {
      LocalDate od = LocalDate.parse(odDatuma);
      LocalDate dd = LocalDate.parse(doDatuma);
      zatvoriSjedalaZaTvrtkunUIntervalu(avioTvrtka, od, dd);
      out.write("OK\n");
    } catch (Exception _) {
      neispravnaKomandaError20(out);
    }
  }

  /**
   * Zatvara sjedala za avio tvrtku u zadanom intervalu datuma.
   *
   * @param avioTvrtka identifikator avio tvrtke
   * @param od         početni datum
   * @param dd         završni datum
   */
  private void zatvoriSjedalaZaTvrtkunUIntervalu(String avioTvrtka, LocalDate od, LocalDate dd) {
    for (Map.Entry<String, Let> zapis : this.letovi.entrySet()) {
      Let let = zapis.getValue();
      List<RezervacijaSjedala> lista = this.rezervacije.get(zapis.getKey());
      if (!let.letPodaci().avioTvrtka().id().equals(avioTvrtka)
          || let.datumPolijetanja().isBefore(od) || let.datumPolijetanja().isAfter(dd)
          || lista == null) {
        continue;
      }

      synchronized (lista) {
        zatvoriSjedalaURasponuRedova(lista, Integer.MIN_VALUE, Integer.MAX_VALUE);
      }
    }
  }

  /**
   * Obrađuje naredbu za inicijalizaciju letova.
   *
   * @param out     izlazni tok za odgovor
   * @param komanda puna naredba
   */
  public void obradiInicijaliziraj(PrintWriter out, String komanda) {
    try {
      if (komanda.contains("\"")) {
        obradiInicijalizirajSOznakomLeta(out, komanda);
        return;
      }
      String[] dijelovi = komanda.split("\\s+");
      if (dijelovi.length == 5) {
        obradiInicijalizirajZaTvrtku(out, dijelovi[1], dijelovi[2], dijelovi[3], dijelovi[4]);
      } else {
        neispravnaKomandaError20(out);
      }
    } catch (Exception _) {
      neispravnaKomandaError20(out);
    }
  }

  /**
   * Obrađuje inicijalizaciju za točno određenu oznaku leta.
   *
   * @param out     izlazni tok za odgovor
   * @param komanda puna naredba
   */
  private void obradiInicijalizirajSOznakomLeta(PrintWriter out, String komanda) {
    int prvi = komanda.indexOf('"');
    int zadnji = komanda.lastIndexOf('"');
    if (prvi < 0 || zadnji <= prvi) {
      neispravnaKomandaError20(out);
      return;
    }
    String oznakaLeta = komanda.substring(prvi + 1, zadnji).trim();
    String[] a = komanda.substring(0, prvi).trim().split("\\s+");
    String[] b = komanda.substring(zadnji + 1).trim().split("\\s+");
    if (a.length != 3 || b.length != 2) {
      neispravnaKomandaError20(out);
      return;
    }
    obradiInicijalizirajZaOznaku(out, a[1], a[2], oznakaLeta, b[0], b[1]);
  }

  /**
   * Obrađuje inicijalizaciju letova za zadanu oznaku leta.
   *
   * @param out        izlazni tok za odgovor
   * @param idUlaznice identifikator ulaznice
   * @param avioTvrtka identifikator avio tvrtke
   * @param oznakaLeta oznaka leta
   * @param odDatuma   početni datum
   * @param doDatuma   završni datum
   */
  public void obradiInicijalizirajZaOznaku(PrintWriter out, String idUlaznice, String avioTvrtka,
      String oznakaLeta, String odDatuma, String doDatuma) {
    if (!provjeriUlaznicu(idUlaznice)) {
      neispravnaUlaznicaError21(out);
      return;
    }
    List<LetPodaci> kandidati = new ArrayList<>();
    for (LetPodaci podaci : this.podaciLetova.values()) {
      if (podaci.oznakaLeta().equals(oznakaLeta) && podaci.avioTvrtka().id().equals(avioTvrtka)) {
        kandidati.add(podaci);
      }
    }
    if (kandidati.isEmpty()) {
      neispravniPodaciLetaError27(out);
      return;
    }
    out.write(inicijalizirajNoveLetove(kandidati, odDatuma, doDatuma) ? "OK\n" : ERROR_20);
  }

  /**
   * Obrađuje inicijalizaciju letova za zadanu avio tvrtku.
   *
   * @param out        izlazni tok za odgovor
   * @param idUlaznice identifikator ulaznice
   * @param avioTvrtka identifikator avio tvrtke
   * @param odDatuma   početni datum
   * @param doDatuma   završni datum
   */
  public void obradiInicijalizirajZaTvrtku(PrintWriter out, String idUlaznice, String avioTvrtka,
      String odDatuma, String doDatuma) {
    if (!provjeriUlaznicu(idUlaznice)) {
      neispravnaUlaznicaError21(out);
      return;
    }
    List<LetPodaci> kandidati = new ArrayList<>();
    for (LetPodaci podaci : this.podaciLetova.values()) {
      if (podaci.avioTvrtka().id().equals(avioTvrtka)) {
        kandidati.add(podaci);
      }
    }
    if (kandidati.isEmpty()) {
      neispravniPodaciLetaError27(out);
      return;
    }
    out.write(inicijalizirajNoveLetove(kandidati, odDatuma, doDatuma) ? "OK\n" : ERROR_20);
  }

  /**
   * Inicijalizira nove letove za zadane podatke i interval datuma.
   *
   * @param kandidati kandidati za inicijalizaciju
   * @param odDatuma  početni datum
   * @param doDatuma  završni datum
   * @return {@code true} ako su letovi uspješno inicijalizirani
   */
  public boolean inicijalizirajNoveLetove(List<LetPodaci> kandidati, String odDatuma,
      String doDatuma) {
    try {
      LocalDate od = LocalDate.parse(odDatuma);
      LocalDate dd = LocalDate.parse(doDatuma);
      Path baza = odrediBazuDatoteka();
      for (LetPodaci podaci : kandidati) {
        for (LocalDate datum = od; !datum.isAfter(dd); datum = datum.plusDays(1)) {
          inicijalizirajJedanLet(podaci, datum, baza);
        }
      }
      return true;
    } catch (Exception _) {
      return false;
    }
  }

  /**
   * Inicijalizira jedan let i njegov raspored sjedala.
   *
   * @param podaci podaci o letu
   * @param datum  datum leta
   * @param baza   bazna putanja za datoteke
   * @throws IOException ako dođe do pogreške pri čitanju datoteke
   */
  private void inicijalizirajJedanLet(LetPodaci podaci, LocalDate datum, Path baza)
      throws IOException {
    Let let = Let.kreirajLet(podaci, datum);
    if (this.letovi.containsKey(let.id())) {
      return;
    }
    this.letovi.put(let.id(), let);
    Map<Integer, Map<String, RezervacijaSjedala>> redovi = ucitajSjedalaZaAvion(let,
        baza.resolve(podaci.avion().datoteka()));
    List<RezervacijaSjedala> lista = new ArrayList<>();
    for (Map<String, RezervacijaSjedala> red : redovi.values()) {
      lista.addAll(red.values());
    }
    this.rezervacije.put(let.id(), lista);
    postaviSjedalaUAlgoritam(let.id(), redovi);
  }

  /**
   * Poništava sve rezervacije sjedala.
   */
  public void ponistiSveRezervacije() {
    for (List<RezervacijaSjedala> lista : this.rezervacije.values()) {
      synchronized (lista) {
        for (int i = 0; i < lista.size(); i++) {
          RezervacijaSjedala rezervacija = lista.get(i);
          lista.set(i, rezervacija.inicijalizirajRezervacijuSjedala(LocalDateTime.now()));
        }
      }
    }
  }
}
