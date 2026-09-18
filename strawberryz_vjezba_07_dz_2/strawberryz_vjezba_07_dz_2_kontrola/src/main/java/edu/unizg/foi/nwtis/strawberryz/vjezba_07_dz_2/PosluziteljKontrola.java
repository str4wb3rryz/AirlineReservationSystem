package edu.unizg.foi.nwtis.strawberryz.vjezba_07_dz_2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import edu.unizg.foi.nwtis.Ulaznica;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

/**
 * Poslužitelj za obradu korisničkih i administratorskih zahtjeva sustava
 * kontrole.
 */
public class PosluziteljKontrola {

  /** Konfiguracija. */
  Konfiguracija konfig = null;

  /** Izvršitelj dretvi. */
  ExecutorService executor = null;

  /** Mrežna vrata za korisnike. */
  int mreznaVrata = 0;

  /** Mrežna vrata za administraciju. */
  int mreznaVrataAdmin = 0;

  /** Trajanje ulaznice u milisekundama. */
  long trajanjeUlaznice = 0;

  /** Kod za gašenje poslužitelja. */
  String kodZaKraj = "";

  /** Pauza dretve u milisekundama. */
  int pauzaDretve = 0;

  /** Maksimalan broj dretvi. */
  int maksBrojDretvi = 0;

  /** Maksimalan broj čekajućih zahtjeva. */
  int maksBrojCekaca = 0;

  /** Oznaka za kraj rada. */
  AtomicBoolean kraj = new AtomicBoolean(false);

  /** Oznaka pauze za korisnike. */
  AtomicBoolean pauzaKorisnik = new AtomicBoolean(false);

  /** Broj aktivnih zahtjeva. */
  AtomicInteger aktivniZahtjevi = new AtomicInteger(0);

  /** Semafor za kontrolu broja dretvi. */
  Semaphore semaforDretvi;

  /**
   * Zapis (record) s informacijama o poslužitelju.
   *
   * @param adresa                    adresa poslužitelja
   * @param mreznaVrata               mrežna vrata za korisnike
   * @param mreznaVrataAdministracija mrežna vrata za administraciju
   * @param kodZaKraj                 kod za gašenje poslužitelja
   */
  public record PosluziteljInfo(String adresa, int mreznaVrata, int mreznaVrataAdministracija,
      String kodZaKraj) {
  }

  /**
   * Zapis (record) koji predstavlja korisnika.
   *
   * @param ime     ime korisnika
   * @param prezime prezime korisnika
   * @param lozinka lozinka korisnika
   * @param email   email korisnika
   */
  public record Korisnik(String ime, String prezime, String lozinka, String email) {
  }

  /** Kolekcija ulaznice. */
  Map<String, Ulaznica> ulaznice = new ConcurrentHashMap<>();

  /** Kolekcija korisnici. */
  Map<String, Korisnik> korisnici = new ConcurrentHashMap<>();

  /** Kolekcija zabranjenje adrese. */
  Set<String> zabranjenjeAdrese = ConcurrentHashMap.newKeySet();

  /** Registar posluzitelja. */
  Map<String, PosluziteljInfo> registarPosluzitelja = new ConcurrentHashMap<>();

  /** Komanda dodaj. */
  String dodaj = "DODAJ";

  /** Komanda provjeri. */
  String provjeri = "PROVJERI";

  /** Komanda korisnik. */
  String korisnik = "KORISNIK";

  /** Komanda ponisti. */
  String ponisti = "PONIŠTI";

  /** Komanda rezervacije. */
  String rezervacije = "REZERVACIJE";

  /**
   * Glavna metoda programa.
   *
   * @param args argumenti komandne linije
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      return;
    }
    var program = new PosluziteljKontrola();
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
   * Učitava konfiguraciju i pokreće poslužitelje.
   *
   * @param nazivDatoteke naziv konfiguracijske datoteke
   */
  public void pripremiKreni(String nazivDatoteke) {
    if (!this.ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }
    this.semaforDretvi = new Semaphore(this.maksBrojDretvi);
    var builder = Thread.ofVirtual();
    var factory = builder.factory();
    this.executor = Executors.newThreadPerTaskExecutor(factory);
    var dretvaAdministracija = this.executor.submit(this::pokreniPosluziteljAdministracija);
    var dretvaKorisnici = this.executor.submit(this::pokreniPosluziteljKorisnici);
    while (!dretvaAdministracija.isDone() || !dretvaKorisnici.isDone()) {
      spavanjeDretve();
    }
  }

  /**
   * Pokreće poslužitelj za korisnike.
   */
  public void pokreniPosluziteljKorisnici() {
    try (ServerSocket ss = new ServerSocket(this.mreznaVrata, this.maksBrojCekaca)) {
      ss.setSoTimeout(this.pauzaDretve);
      while (!this.kraj.get()) {
        if (this.pauzaKorisnik.get()) {
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
   * Prihvaća korisnika ako je moguće.
   *
   * @param ss serverska utičnica
   */
  private void prihvatiKorisnikaAkoMoguce(ServerSocket ss) {
    try {
      var mreznaUticnica = ss.accept();
      if (this.pauzaKorisnik.get()) {
        mreznaUticnica.close();
        return;
      }
      this.executor.submit(() -> obradiKorisnik(mreznaUticnica));
    } catch (IOException _) {
      // Kontrolirani prekid čekanja
    }
  }

  /**
   * Pokreće posluzitelj administracija.
   */
  public void pokreniPosluziteljAdministracija() {
    try (ServerSocket ss = new ServerSocket(this.mreznaVrataAdmin, 0)) {
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
   * @param mreznaUticnica mrežna utičnica
   * @return rezultat obrade (true ako je uspješno)
   */
  public Boolean obradiAdministracija(Socket mreznaUticnica) {
    pauzaKorisnik.set(true);
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
      pauzaKorisnik.set(false);
    }
    return Boolean.TRUE;
  }

  /**
   * Obrađuje korisnički zahtjev.
   *
   * @param mreznaUticnica mrežna utičnica korisnika
   * @return {@code true} ako je zahtjev obrađen
   */
  public Boolean obradiKorisnik(Socket mreznaUticnica) {
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
    try (
        BufferedReader in = new BufferedReader(
            new InputStreamReader(mreznaUticnica.getInputStream(), StandardCharsets.UTF_8));
        PrintWriter out = new PrintWriter(
            new OutputStreamWriter(mreznaUticnica.getOutputStream(), StandardCharsets.UTF_8));) {
      String linija = in.readLine();
      mreznaUticnica.shutdownInput();
      if (jeZabranjenAdresa(mreznaUticnica)) {
        out.write("ERROR 17\n");
      } else {
        komandeKorisnika(out, linija);
      }
      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();
    } catch (Exception _) {
      // Kontrolirani prekid rada poslužitelja
    }
  }

  /**
   * Provjerava je li adresa zabranjena.
   *
   * @param mreznaUticnica mrežna utičnica korisnika
   * @return {@code true} ako je adresa ili naziv računala zabranjen
   */
  private boolean jeZabranjenAdresa(Socket mreznaUticnica) {
    String hostAddr = mreznaUticnica.getInetAddress().getHostAddress();
    String hostName = mreznaUticnica.getInetAddress().getHostName();
    return this.zabranjenjeAdrese.contains(hostAddr) || this.zabranjenjeAdrese.contains(hostName);
  }

  /**
   * Obrađuje administratorske naredbe.
   *
   * @param out    izlazni tok za odgovor
   * @param linija primljena naredba
   */
  public void komandeAdministracija(PrintWriter out, String linija) {
    if (linija == null) {
      neispravnaKomandaError10(out);
      return;
    }
    String komanda = linija.trim();
    String[] dijelovi = komanda.split(" ");
    if (dijelovi.length == 1 && "PING".equals(komanda)) {
      out.write("OK\n");
    } else if (dijelovi.length == 2) {
      obradiAdminKomandaDvaDijela(out, dijelovi);
    } else if (dijelovi.length == 5 && "REGISTRIRAJ".equals(dijelovi[0])) {
      obradiRegistraciju(out, dijelovi[1], dijelovi[2], dijelovi[3], dijelovi[4]);
    } else {
      neispravnaKomandaError10(out);
    }
  }

  /**
   * Šalje odgovor za neispravnu naredbu.
   *
   * @param out izlazni tok za odgovor
   */
  private void neispravnaKomandaError10(PrintWriter out) {
    out.write("ERROR 10\n");
  }

  /**
   * Obrađuje administratorsku naredbu od dva dijela.
   *
   * @param out      izlazni tok za odgovor
   * @param dijelovi dijelovi naredbe
   */
  private void obradiAdminKomandaDvaDijela(PrintWriter out, String[] dijelovi) {
    switch (dijelovi[0]) {
      case "ULAZNICA":
        if (ponisti.equals(dijelovi[1])) {
          this.ulaznice.clear();
          out.write("OK\n");
        } else {
          neispravnaKomandaError10(out);
        }
        break;
      case "ZABRANA":
        obradiZabranu(out, dijelovi[1]);
        break;
      case "DOZVOLA":
        obradiDozvolu(out, dijelovi[1]);
        break;
      case "KRAJ":
        obradiKrajAdmin(out, dijelovi[1]);
        break;
      default:
        neispravnaKomandaError10(out);
        break;
    }
  }

  /**
   * Obrađuje naredbu za završetak rada poslužitelja.
   *
   * @param out        izlazni tok za odgovor
   * @param uneseniKod uneseni kod za gašenje
   */
  private void obradiKrajAdmin(PrintWriter out, String uneseniKod) {
    if (!uneseniKod.equals(this.kodZaKraj)) {
      out.write("ERROR 18\n");
      return;
    }
    Map<String, String> odgovori = posaljiKrajSvimaPosluziteljima();
    ponoviKrajZaNeuspjele(odgovori);
    kraj.set(true);
    executor.shutdown();
    out.write("OK\n");
  }

  /**
   * Šalje naredbu za kraj svim registriranim poslužiteljima.
   *
   * @return mapa odgovora po poslužiteljima
   */
  private Map<String, String> posaljiKrajSvimaPosluziteljima() {
    Map<String, String> odgovori = new HashMap<>();
    for (PosluziteljInfo info : registarPosluzitelja.values()) {
      String kljuc = info.adresa() + ":" + info.mreznaVrata();
      String odgovor = posaljiKrajJednomPosluzitelju(info);
      odgovori.put(kljuc, odgovor);
    }
    return odgovori;
  }

  /**
   * Šalje naredbu za kraj jednom poslužitelju.
   *
   * @param info podaci o poslužitelju
   * @return odgovor poslužitelja
   */
  private String posaljiKrajJednomPosluzitelju(PosluziteljInfo info) {
    try (Socket socket = new Socket(info.adresa(), info.mreznaVrataAdministracija());
        PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
      writer.println("KRAJ " + info.kodZaKraj());
      writer.flush();
      String odgovor = reader.readLine();
      return odgovor != null ? odgovor : "NO RESPONSE";
    } catch (Exception _) {
      return "ERROR";
    }
  }

  /**
   * Ponavlja slanje naredbe za kraj poslužiteljima koji nisu uspješno odgovorili.
   *
   * @param odgovori prikupljeni odgovori poslužitelja
   */
  private void ponoviKrajZaNeuspjele(Map<String, String> odgovori) {
    boolean sviOk = odgovori.values().stream().allMatch("OK"::equals);
    if (sviOk) {
      return;
    }
    for (PosluziteljInfo info : registarPosluzitelja.values()) {
      String kljuc = info.adresa() + ":" + info.mreznaVrata();
      if (!"OK".equals(odgovori.get(kljuc))) {
        posaljiKrajJednomPosluzitelju(info);
      }
    }
  }

  /**
   * Obrađuje korisničke naredbe.
   *
   * @param out    izlazni tok za odgovor
   * @param linija primljena naredba
   */
  public void komandeKorisnika(PrintWriter out, String linija) {
    if (linija == null) {
      neispravnaKomandaError10(out);
      return;
    }
    String komanda = linija.trim();
    String[] dijelovi = komanda.split(" ");
    if (dijelovi.length == 1 && "PING".equals(komanda)) {
      out.write("OK\n");
    } else if (dijelovi.length == 2 && "PAUZA".equals(dijelovi[0])) {
      obradiPauzu(out, dijelovi[1]);
    } else if (dijelovi.length > 2 && korisnik.equals(dijelovi[0])) {
      obradiKorisnika(out, komanda);
    } else if (dijelovi.length > 2 && "ULAZNICA".equals(dijelovi[0])) {
      obradiUlaznica(out, komanda);
    } else {
      neispravnaKomandaError10(out);
    }
  }

  /**
   * Obrađuje naredbe vezane uz korisnika.
   *
   * @param out     izlazni tok za odgovor
   * @param komanda korisnička naredba
   */
  private void obradiKorisnika(PrintWriter out, String komanda) {
    String[] dijelovi = komanda.split(" ");
    if (dijelovi.length == 6 && dodaj.equals(dijelovi[1])) {
      obradiKorisnikDodaj(out, dijelovi);
    } else if (dijelovi.length == 3 && provjeri.equals(dijelovi[1])) {
      obradiKorisnikProvjeri(out, dijelovi[2]);
    } else {
      neispravnaKomandaError10(out);
    }
  }

  /**
   * Obrađuje naredbu za dodavanje korisnika.
   *
   * @param out      izlazni tok za odgovor
   * @param dijelovi dijelovi naredbe
   */
  private void obradiKorisnikDodaj(PrintWriter out, String[] dijelovi) {
    String ime = dijelovi[2].replace("\"", "");
    String prezime = dijelovi[3].replace("\"", "");
    String lozinka = dijelovi[4];
    String email = dijelovi[5];
    if (ime.isEmpty() || prezime.isEmpty() || lozinka.isEmpty() || !email.contains("@")) {
      neispravnaKomandaError10(out);
      return;
    }
    if (korisnici.containsKey(email)) {
      out.write("ERROR 11\n");
    } else {
      korisnici.put(email, new Korisnik(ime, prezime, lozinka, email));
      out.write("OK\n");
    }
  }

  /**
   * Obrađuje naredbu za provjeru korisnika.
   *
   * @param out   izlazni tok za odgovor
   * @param email adresa e-pošte korisnika
   */
  private void obradiKorisnikProvjeri(PrintWriter out, String email) {
    if (!email.contains("@")) {
      neispravnaKomandaError10(out);
      return;
    }
    if (korisnici.containsKey(email)) {
      out.write("OK\n");
    } else {
      out.write("ERROR 12\n");
    }
  }

  /**
   * Obrađuje naredbe vezane uz ulaznicu.
   *
   * @param out     izlazni tok za odgovor
   * @param komanda korisnička naredba
   */
  private void obradiUlaznica(PrintWriter out, String komanda) {
    String[] dijelovi = komanda.split(" ");
    if (!jeIspravnaUlaznicaKomanda(dijelovi)) {
      neispravnaKomandaError10(out);
      return;
    }
    switch (dijelovi[1]) {
      case "DODAJ":
        obradiUlaznicaDodaj(out, dijelovi[2], dijelovi[3]);
        break;
      case "PROVJERI":
        obradiUlaznicaProvjeri(out, dijelovi[2]);
        break;
      case "KORISNIK":
        obradiUlaznicaKorisnik(out, dijelovi[2]);
        break;
      case "PONIŠTI":
        obradiUlaznicaPonisti(out, dijelovi[2]);
        break;
      default:
        neispravnaKomandaError10(out);
        break;
    }
  }

  /**
   * Provjerava je li naredba za ulaznicu ispravnog oblika.
   *
   * @param dijelovi dijelovi naredbe
   * @return {@code true} ako naredba ima očekivani oblik
   */
  private boolean jeIspravnaUlaznicaKomanda(String[] dijelovi) {
    if (dijelovi.length == 4 && dodaj.equals(dijelovi[1])) {
      return true;
    }
    return dijelovi.length == 3 && (provjeri.equals(dijelovi[1]) || korisnik.equals(dijelovi[1])
        || ponisti.equals(dijelovi[1]));
  }

  /**
   * Obrađuje naredbu za dodavanje ulaznice.
   *
   * @param out     izlazni tok za odgovor
   * @param email   adresa e-pošte korisnika
   * @param lozinka lozinka korisnika
   */
  private void obradiUlaznicaDodaj(PrintWriter out, String email, String lozinka) {
    if (!email.contains("@") || lozinka.isEmpty()) {
      neispravnaKomandaError10(out);
      return;
    }
    Korisnik k = korisnici.get(email);
    if (k == null || !k.lozinka().equals(lozinka)) {
      out.write("ERROR 12\n");
      return;
    }
    if (imaVazecuUlaznicu(email)) {
      out.write("ERROR 13\n");
      return;
    }
    kreirajIDodajUlaznicu(out, email);
  }

  /**
   * Provjerava postoji li važeća ulaznica za korisnika.
   *
   * @param email adresa e-pošte korisnika
   * @return {@code true} ako korisnik već ima važeću ulaznicu
   */
  private boolean imaVazecuUlaznicu(String email) {
    return ulaznice.values().stream()
        .anyMatch(u -> u.ispravnaUlaznica(email) && u.vazecaUlaznica(rezervacije, 0));
  }

  /**
   * Kreira i dodaje novu ulaznicu.
   *
   * @param out   izlazni tok za odgovor
   * @param email adresa e-pošte korisnika
   */
  private void kreirajIDodajUlaznicu(PrintWriter out, String email) {
    try {
      long nano = System.nanoTime();
      String dio1 = Integer.toHexString((email + nano).hashCode());
      String dio2 = Integer.toHexString((InetAddress.getLocalHost().toString() + nano).hashCode());
      String id = dio1 + dio2;
      LocalDateTime vaziDo = LocalDateTime.now().plus(trajanjeUlaznice, ChronoUnit.MILLIS);
      Ulaznica ulaznica = new Ulaznica(id, email, rezervacije, vaziDo);
      ulaznice.put(id, ulaznica);
      out.write("OK " + id + "\n");
    } catch (Exception _) {
      neispravnaKomandaError10(out);
    }
  }

  /**
   * Obrađuje naredbu za provjeru ulaznice.
   *
   * @param out izlazni tok za odgovor
   * @param id  identifikator ulaznice
   */
  private void obradiUlaznicaProvjeri(PrintWriter out, String id) {
    Ulaznica u = ulaznice.get(id);
    if (u == null || !u.vazecaUlaznica(rezervacije, 0)) {
      neispravnaUlaznicaError14(out);
    } else {
      ulaznice.put(id, u.produzi(trajanjeUlaznice));
      out.write("OK\n");
    }
  }

  /**
   * Šalje odgovor za neispravnu ulaznicu.
   *
   * @param out izlazni tok za odgovor
   */
  private void neispravnaUlaznicaError14(PrintWriter out) {
    out.write("ERROR 14\n");
  }

  /**
   * Obrađuje naredbu za dohvat korisnika po ulaznici.
   *
   * @param out izlazni tok za odgovor
   * @param id  identifikator ulaznice
   */
  private void obradiUlaznicaKorisnik(PrintWriter out, String id) {
    Ulaznica u = ulaznice.get(id);
    if (u == null || !u.vazecaUlaznica(rezervacije, 0)) {
      neispravnaUlaznicaError14(out);
    } else {
      out.write("OK " + u.email() + "\n");
    }
  }

  /**
   * Obrađuje naredbu za poništavanje ulaznice.
   *
   * @param out izlazni tok za odgovor
   * @param id  identifikator ulaznice
   */
  private void obradiUlaznicaPonisti(PrintWriter out, String id) {
    Ulaznica u = ulaznice.get(id);
    if (u == null || !u.vazecaUlaznica(rezervacije, 0)) {
      neispravnaUlaznicaError14(out);
    } else {
      ulaznice.remove(id);
      out.write("OK\n");
    }
  }

  /**
   * Obrađuje naredbu za pauzu.
   *
   * @param out        izlazni tok za odgovor
   * @param vrijednost trajanje pauze u milisekundama
   */
  private void obradiPauzu(PrintWriter out, String vrijednost) {
    try {
      Thread.sleep(Integer.parseInt(vrijednost));
      out.write("OK\n");
    } catch (NumberFormatException _) {
      neispravnaKomandaError10(out);
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
      out.write("ERROR 15\n");
    }
  }

  /**
   * Obrađuje naredbu za zabranu adrese.
   *
   * @param out    izlazni tok za odgovor
   * @param adresa adresa ili naziv računala
   */
  private void obradiZabranu(PrintWriter out, String adresa) {
    if (!provjeriAdresu(adresa)) {
      neispravnaKomandaError10(out);
      return;
    }
    if (this.zabranjenjeAdrese.contains(adresa)) {
      out.write("ERROR 16\n");
      return;
    }
    this.zabranjenjeAdrese.add(adresa);
    out.write("OK\n");
  }

  /**
   * Obrađuje naredbu za uklanjanje zabrane adrese.
   *
   * @param out    izlazni tok za odgovor
   * @param adresa adresa ili naziv računala
   */
  private void obradiDozvolu(PrintWriter out, String adresa) {
    if (!provjeriAdresu(adresa)) {
      neispravnaKomandaError10(out);
      return;
    }
    if (!this.zabranjenjeAdrese.contains(adresa)) {
      out.write("ERROR 16\n");
      return;
    }
    this.zabranjenjeAdrese.remove(adresa);
    out.write("OK\n");
  }

  /**
   * Provjerava je li adresa valjana.
   *
   * @param adresa adresa ili naziv računala
   * @return {@code true} ako je adresu moguće razriješiti
   */
  private boolean provjeriAdresu(String adresa) {
    try {
      InetAddress.getByName(adresa);
      return true;
    } catch (Exception _) {
      return false;
    }
  }

  /**
   * Obrađuje registraciju udaljenog poslužitelja.
   *
   * @param out                   izlazni tok za odgovor
   * @param adresa                adresa poslužitelja
   * @param mreznaVrataTekst      tekstualni zapis korisničkih mrežnih vrata
   * @param mreznaVrataAdminTekst tekstualni zapis administratorskih mrežnih vrata
   * @param kodZaKrajPosluzitelja kod za gašenje poslužitelja
   */
  private void obradiRegistraciju(PrintWriter out, String adresa, String mreznaVrataTekst,
      String mreznaVrataAdminTekst, String kodZaKrajPosluzitelja) {
    if (!provjeriAdresu(adresa) || !provjeriPort(mreznaVrataTekst)
        || !provjeriPort(mreznaVrataAdminTekst)) {
      neispravnaKomandaError10(out);
      return;
    }
    PosluziteljInfo info = new PosluziteljInfo(adresa, Integer.parseInt(mreznaVrataTekst),
        Integer.parseInt(mreznaVrataAdminTekst), kodZaKrajPosluzitelja);
    this.registarPosluzitelja.put(adresa, info);
    out.write("OK\n");
  }

  /**
   * Provjerava je li broj porta valjan.
   *
   * @param port tekstualni zapis porta
   * @return {@code true} ako je port u dopuštenom rasponu
   */
  private boolean provjeriPort(String port) {
    try {
      int portNum = Integer.parseInt(port);
      return portNum > 0 && portNum <= 65535;
    } catch (NumberFormatException _) {
      return false;
    }
  }

  /**
   * Spavanje dretve.
   */
  private void spavanjeDretve() {
    try {
      Thread.sleep(this.pauzaDretve);
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
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
      this.trajanjeUlaznice = Long.parseLong(this.konfig.dajPostavku("trajanjeUlaznice"));
      this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
      this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));
      this.maksBrojDretvi = Integer.parseInt(this.konfig.dajPostavku("maksBrojDretvi"));
      this.maksBrojCekaca = Integer.parseInt(this.konfig.dajPostavku("maksBrojCekaca"));
      return true;
    } catch (NeispravnaKonfiguracija _) {
      return false;
    }
  }
}
