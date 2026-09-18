package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;

@Entity
@NamedQuery(name = "Racunstavka.findAll", query = "SELECT r FROM Racunstavka r")
public class Racunstavka implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "RACUNSTAVKA_ID_GENERATOR", sequenceName = "RACUNSTAVKA_ID",
      initialValue = 1, allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RACUNSTAVKA_ID_GENERATOR")
  private int id;

  private double iznos;

  private String putnikime;

  private String putnikiprezime;

  private String rezervacija;

  @ManyToOne
  @JoinColumn(name = "RACUNID")
  private Racun racun;

  public Racunstavka() {}

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public double getIznos() {
    return this.iznos;
  }

  public void setIznos(double iznos) {
    this.iznos = iznos;
  }

  public String getPutnikime() {
    return this.putnikime;
  }

  public void setPutnikime(String putnikime) {
    this.putnikime = putnikime;
  }

  public String getPutnikiprezime() {
    return this.putnikiprezime;
  }

  public void setPutnikiprezime(String putnikiprezime) {
    this.putnikiprezime = putnikiprezime;
  }

  public String getRezervacija() {
    return this.rezervacija;
  }

  public void setRezervacija(String rezervacija) {
    this.rezervacija = rezervacija;
  }

  public Racun getRacun() {
    return this.racun;
  }

  public void setRacun(Racun racun) {
    this.racun = racun;
  }

}
