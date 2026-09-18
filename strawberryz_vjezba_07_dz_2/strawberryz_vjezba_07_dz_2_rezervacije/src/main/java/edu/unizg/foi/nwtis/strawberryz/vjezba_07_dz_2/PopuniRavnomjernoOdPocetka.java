package edu.unizg.foi.nwtis.strawberryz.vjezba_07_dz_2;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import edu.unizg.foi.nwtis.Let;
import edu.unizg.foi.nwtis.OdredivanjeSjedala;
import edu.unizg.foi.nwtis.ProblemKodRezervacije;
import edu.unizg.foi.nwtis.RazredSjedala;
import edu.unizg.foi.nwtis.RezervacijaSjedala;
import edu.unizg.foi.nwtis.StatusRezervacijeSjedala;

/**
 * Algoritam koji ravnomjerno popunjava sjedala od početka reda.
 */
public class PopuniRavnomjernoOdPocetka implements OdredivanjeSjedala {

  /** The let sjedala. */
  private Map<String, Map<Integer, Map<String, RezervacijaSjedala>>> letSjedala = new ConcurrentHashMap<>();

  /** The lijeva stranica. */
  private Map<String, Boolean> lijevaStranica = new ConcurrentHashMap<>();

  /**
   * Inicijalizira stanje algoritma.
   *
   * @return {@code true} ako je inicijalizacija uspješna
   */
  @Override
  public boolean inicijaliziraj() {
    letSjedala.clear();
    lijevaStranica.clear();
    return true;
  }

  /**
   * Rezervira jedno sjedalo za putnika.
   *
   * @param let    let za koji se radi rezervacija
   * @param putnik ime putnika
   * @param razred traženi razred sjedala
   * @return kreirana rezervacija sjedala
   * @throws ProblemKodRezervacije ako rezervaciju nije moguće napraviti
   */
  @Override
  public RezervacijaSjedala rezervirajSjedalo(Let let, String putnik, RazredSjedala razred)
      throws ProblemKodRezervacije {
    synchronized (letSjedala) {
      Map<Integer, Map<String, RezervacijaSjedala>> redovi = letSjedala.computeIfAbsent(let.id(), k -> new TreeMap<>());

      for (Map.Entry<Integer, Map<String, RezervacijaSjedala>> ulaz : redovi.entrySet()) {
        List<String> slobodna = dajSlobodnaSjedalaURedu(ulaz.getValue(), razred);
        if (slobodna.isEmpty()) {
          continue;
        }
        String odabrano = odaberiSjedalo(slobodna, let.id());
        return kreirajISpremniRezervaciju(ulaz.getValue(), odabrano, putnik);
      }
    }
    throw new ProblemKodRezervacije();
  }

  /**
   * Vraća slobodna sjedala u redu za zadani razred.
   *
   * @param redSjedala sjedala u jednom redu
   * @param razred     traženi razred sjedala
   * @return popis slobodnih oznaka sjedala
   */
  private List<String> dajSlobodnaSjedalaURedu(Map<String, RezervacijaSjedala> redSjedala,
      RazredSjedala razred) {
    List<String> slobodna = new ArrayList<>();
    for (Map.Entry<String, RezervacijaSjedala> ulaz : redSjedala.entrySet()) {
      RezervacijaSjedala rez = ulaz.getValue();
      if (rez.statusRezervacije() == StatusRezervacijeSjedala.SLOBODNA
          && rez.sjedalo().razred() == razred) {
        slobodna.add(ulaz.getKey());
      }
    }
    slobodna.sort(String::compareTo);
    return slobodna;
  }

  /**
   * Odabire sjedalo iz skupa slobodnih mjesta.
   *
   * @param slobodna popis slobodnih sjedala
   * @param letId    identifikator leta
   * @return oznaka odabranog sjedala
   */
  private String odaberiSjedalo(List<String> slobodna, String letId) {
    if (slobodna.size() == 1) {
      return slobodna.get(0);
    }
    boolean koristiLijevu = lijevaStranica.getOrDefault(letId, true);
    String odabrano = koristiLijevu ? slobodna.get(0) : slobodna.get(slobodna.size() - 1);
    lijevaStranica.put(letId, !koristiLijevu);
    return odabrano;
  }

  /**
   * Kreira i sprema rezervaciju za odabrano sjedalo.
   *
   * @param redSjedala sjedala u redu
   * @param odabrano   oznaka odabranog sjedala
   * @param putnik     ime putnika
   * @return nova rezervacija sjedala
   */
  private RezervacijaSjedala kreirajISpremniRezervaciju(Map<String, RezervacijaSjedala> redSjedala,
      String odabrano, String putnik) {
    RezervacijaSjedala stara = redSjedala.get(odabrano);
    RezervacijaSjedala nova = stara.kreirajRezervacijuSjedala(putnik, LocalDateTime.now());
    redSjedala.put(odabrano, nova);
    return nova;
  }

  /**
   * Potvrđuje rezervaciju sjedala.
   *
   * @param rezervacija rezervacija za potvrdu
   * @return potvrđena rezervacija sjedala
   * @throws ProblemKodRezervacije ako potvrda nije moguća
   */
  @Override
  public RezervacijaSjedala potvrdiRezervacijuSjedala(RezervacijaSjedala rezervacija)
      throws ProblemKodRezervacije {
    return rezervacija.potvrdiRezervacijuSjedala();
  }

  /**
   * Rezervira sjedala za grupu putnika.
   *
   * @param let     let za koji se radi rezervacija
   * @param putnici popis putnika
   * @param razred  traženi razred sjedala
   * @return popis kreiranih rezervacija
   * @throws ProblemKodRezervacije ako rezervaciju nije moguće napraviti
   */
  @Override
  public List<RezervacijaSjedala> rezervirajSjedalaZaGrupu(Let let, List<String> putnici,
      RazredSjedala razred) throws ProblemKodRezervacije {
    List<RezervacijaSjedala> rezultat = new ArrayList<>();
    List<RezervacijaSjedala> kreirane = new ArrayList<>();

    synchronized (letSjedala) {
      Map<Integer, Map<String, RezervacijaSjedala>> redovi = letSjedala.computeIfAbsent(let.id(), k -> new TreeMap<>());
      int preostalo = putnici.size();
      int indeks = 0;

      try {
        for (Map.Entry<Integer, Map<String, RezervacijaSjedala>> ulaz : redovi.entrySet()) {
          if (preostalo == 0) {
            break;
          }
          List<String> slobodna = dajSlobodnaSjedalaURedu(ulaz.getValue(), razred);
          for (int i = 0; i < slobodna.size() && preostalo > 0; i++) {
            RezervacijaSjedala nova = kreirajISpremniRezervaciju(ulaz.getValue(), slobodna.get(i), putnici.get(indeks));
            kreirane.add(nova);
            rezultat.add(nova);
            indeks++;
            preostalo--;
          }
        }
        if (preostalo > 0) {
          rollbackRezervacija(redovi, kreirane);
          throw new ProblemKodRezervacije();
        }
      } catch (ProblemKodRezervacije e) {
        throw e;
      } catch (Exception _) {
        rollbackRezervacija(redovi, kreirane);
        throw new ProblemKodRezervacije();
      }
    }
    return rezultat;
  }

  /**
   * Poništava prethodno kreirane rezervacije.
   *
   * @param redovi   raspored sjedala po redovima
   * @param kreirane rezervacije koje treba poništiti
   */
  private void rollbackRezervacija(Map<Integer, Map<String, RezervacijaSjedala>> redovi,
      List<RezervacijaSjedala> kreirane) {
    for (RezervacijaSjedala rez : kreirane) {
      Map<String, RezervacijaSjedala> red = redovi.get(rez.sjedalo().red());
      if (red != null) {
        red.put(rez.sjedalo().oznakaSjedala(),
            rez.inicijalizirajRezervacijuSjedala(LocalDateTime.now()));
      }
    }
  }

  /**
   * Potvrđuje rezervacije sjedala za grupu.
   *
   * @param rezervacije rezervacije za potvrdu
   * @return popis potvrđenih rezervacija
   * @throws ProblemKodRezervacije ako potvrda nije moguća
   */
  @Override
  public List<RezervacijaSjedala> potvrdiRezervacijuSjedalaGrupe(
      List<RezervacijaSjedala> rezervacije) throws ProblemKodRezervacije {
    List<RezervacijaSjedala> potvrdene = new ArrayList<>();
    for (RezervacijaSjedala rez : rezervacije) {
      if (rez.statusRezervacije() != StatusRezervacijeSjedala.KREIRANA) {
        rollbackPotvrdenih(potvrdene);
        throw new ProblemKodRezervacije();
      }
      potvrdene.add(rez.potvrdiRezervacijuSjedala());
    }
    return potvrdene;
  }

  /**
   * Poništava prethodno potvrđene rezervacije.
   *
   * @param potvrdene potvrđene rezervacije za poništavanje
   */
  private void rollbackPotvrdenih(List<RezervacijaSjedala> potvrdene) {
    for (RezervacijaSjedala p : potvrdene) {
      synchronized (letSjedala) {
        Map<Integer, Map<String, RezervacijaSjedala>> redovi = letSjedala.get(p.sjedalo().let().id());
        if (redovi == null) {
          continue;
        }
        Map<String, RezervacijaSjedala> red = redovi.get(p.sjedalo().red());
        if (red != null) {
          red.put(p.sjedalo().oznakaSjedala(),
              p.inicijalizirajRezervacijuSjedala(LocalDateTime.now()));
        }
      }
    }
  }

  /**
   * Dodaje sjedala za zadani let u lokalni raspored.
   *
   * @param letId  identifikator leta
   * @param redovi raspored sjedala po redovima
   */
  public void dodajSjedala(String letId, Map<Integer, Map<String, RezervacijaSjedala>> redovi) {
    synchronized (letSjedala) {
      Map<Integer, Map<String, RezervacijaSjedala>> sortirani = new TreeMap<>();
      for (Map.Entry<Integer, Map<String, RezervacijaSjedala>> ulaz : redovi.entrySet()) {
        sortirani.put(ulaz.getKey(), new LinkedHashMap<>(ulaz.getValue()));
      }
      letSjedala.put(letId, sortirani);
    }
  }
}
