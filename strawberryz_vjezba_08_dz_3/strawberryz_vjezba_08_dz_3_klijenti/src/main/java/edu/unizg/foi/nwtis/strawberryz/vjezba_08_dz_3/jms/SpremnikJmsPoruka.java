package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jms;

import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import edu.unizg.foi.nwtis.Korisnik;

@ApplicationScoped
public class SpremnikJmsPoruka {
  private List<String> listaRedPoruka = new ArrayList<>();
  private List<Korisnik> listaPorukaTeme = new ArrayList<>();

  public void dodajRedPoruka(String poruka) {
    this.listaRedPoruka.add(poruka);
  }
  public List<String> dajRedPoruka() {
    return this.listaRedPoruka;
  }
  public void obrisiRedPoruka() {
    this.listaRedPoruka.clear();
  }
  public void obrisiRedPoruka(int indeks) {
    if (indeks >= 0 && indeks < this.listaRedPoruka.size()) {
      this.listaRedPoruka.remove(indeks);
    }
  }

  public void dodajPorukuTeme(Korisnik korisnik) {
    this.listaPorukaTeme.add(korisnik);
  }
  public List<Korisnik> dajPorukeTeme() {
    return this.listaPorukaTeme;
  }
  public void obrisiPorukeTeme() {
    this.listaPorukaTeme.clear();
  }
  public void obrisiKorisnikaIzTeme(int indeks) {
    if (indeks >= 0 && indeks < this.listaPorukaTeme.size()) {
      this.listaPorukaTeme.remove(indeks);
    }
  }
}
