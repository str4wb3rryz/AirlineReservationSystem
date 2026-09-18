package edu.unizg.foi.nwtis;

import java.time.LocalDateTime;

public record RezervacijaSjedala(String id, LetSjedalo sjedalo, String putnik,
    LocalDateTime vrijemeRezervacije, StatusRezervacijeSjedala statusRezervacije) {

  public RezervacijaSjedala inicijalizirajRezervacijuSjedala(LocalDateTime vrijemeRezervacije) {
    var vrijeme = vrijemeRezervacije.getNano();
    var baza = sjedalo.let().id() + Long.toString(vrijeme);
    var hashBaza = baza.hashCode();
    var id = Integer.toHexString(hashBaza);

    return new RezervacijaSjedala(id, sjedalo, null, vrijemeRezervacije,
        StatusRezervacijeSjedala.SLOBODNA);
  }

  public RezervacijaSjedala kreirajRezervacijuSjedala(String putnik,
      LocalDateTime vrijemeRezervacije) {
    var vrijeme = vrijemeRezervacije.getNano();
    var baza = sjedalo.let().id() + putnik + Long.toString(vrijeme);
    var hashBaza = baza.hashCode();
    var id = Integer.toHexString(hashBaza);

    return new RezervacijaSjedala(id, sjedalo, putnik, vrijemeRezervacije,
        StatusRezervacijeSjedala.KREIRANA);
  }

  public RezervacijaSjedala potvrdiRezervacijuSjedala() {
    return new RezervacijaSjedala(this.id, this.sjedalo, this.putnik, this.vrijemeRezervacije,
        StatusRezervacijeSjedala.POTVRDENA);
  }

  public RezervacijaSjedala ponistiRezervacijuSjedala() {
    return new RezervacijaSjedala(this.id, this.sjedalo, this.putnik, this.vrijemeRezervacije,
        StatusRezervacijeSjedala.NEVAZECA);
  }

  // Kasnije dodane metode
  public static RezervacijaSjedala inicijalizirajRezervacijuSjedala(LetSjedalo sjedalo,
      LocalDateTime vrijemeRezervacije) {
    var vrijeme = vrijemeRezervacije.getNano();
    var baza = sjedalo.let().id() + Long.toString(vrijeme);
    var hashBaza = baza.hashCode();
    var id = Integer.toHexString(hashBaza);

    return new RezervacijaSjedala(id, sjedalo, null, vrijemeRezervacije,
        StatusRezervacijeSjedala.SLOBODNA);
  }

  public RezervacijaSjedala zatvoriRezervacijuSjedala() {
    return new RezervacijaSjedala(this.id, this.sjedalo, this.putnik, this.vrijemeRezervacije,
        StatusRezervacijeSjedala.ZATVORENA);
  }
}
