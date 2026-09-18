package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;

@Entity
@NamedQuery(name = "Zastupnici.findAll", query = "SELECT z FROM Zastupnici z")
public class Zastupnici implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private String korisnik;

  @ManyToOne
  @JoinColumn(name = "TVRTKA")
  private Aviotvrtke aviotvrtke;

  @OneToOne
  @JoinColumn(name = "KORISNIK", insertable = false, updatable = false)
  private Korisnici korisnici;

  public Zastupnici() {}

  public String getKorisnik() {
    return this.korisnik;
  }

  public void setKorisnik(String korisnik) {
    this.korisnik = korisnik;
  }

  public Aviotvrtke getAviotvrtke() {
    return this.aviotvrtke;
  }

  public void setAviotvrtke(Aviotvrtke aviotvrtke) {
    this.aviotvrtke = aviotvrtke;
  }

  public Korisnici getKorisnici() {
    return this.korisnici;
  }

  public void setKorisnici(Korisnici korisnici) {
    this.korisnici = korisnici;
  }

}
