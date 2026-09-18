package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;

@Entity
@NamedQuery(name = "Dnevnik.findAll", query = "SELECT d FROM Dnevnik d")
public class Dnevnik implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "DNEVNIK_ID_GENERATOR", sequenceName = "DNEVNIK_ID", initialValue = 1,
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DNEVNIK_ID_GENERATOR")
  private int id;

  private String adresaracunala;

  private String ipadresaracunala;

  private String nazivos;

  private String opisrada;

  private String url;

  private String verzijavm;

  private Timestamp vrijeme;

  @ManyToOne
  @JoinColumn(name = "KORISNIK")
  private Korisnici korisnici;

  public Dnevnik() {}

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getAdresaracunala() {
    return this.adresaracunala;
  }

  public void setAdresaracunala(String adresaracunala) {
    this.adresaracunala = adresaracunala;
  }

  public String getIpadresaracunala() {
    return this.ipadresaracunala;
  }

  public void setIpadresaracunala(String ipadresaracunala) {
    this.ipadresaracunala = ipadresaracunala;
  }

  public String getNazivos() {
    return this.nazivos;
  }

  public void setNazivos(String nazivos) {
    this.nazivos = nazivos;
  }

  public String getOpisrada() {
    return this.opisrada;
  }

  public void setOpisrada(String opisrada) {
    this.opisrada = opisrada;
  }

  public String getUrl() {
    return this.url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getVerzijavm() {
    return this.verzijavm;
  }

  public void setVerzijavm(String verzijavm) {
    this.verzijavm = verzijavm;
  }

  public Timestamp getVrijeme() {
    return this.vrijeme;
  }

  public void setVrijeme(Timestamp vrijeme) {
    this.vrijeme = vrijeme;
  }

  public Korisnici getKorisnici() {
    return this.korisnici;
  }

  public void setKorisnici(Korisnici korisnici) {
    this.korisnici = korisnici;
  }

}
