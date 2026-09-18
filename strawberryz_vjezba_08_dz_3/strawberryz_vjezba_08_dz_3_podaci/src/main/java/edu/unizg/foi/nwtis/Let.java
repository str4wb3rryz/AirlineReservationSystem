package edu.unizg.foi.nwtis;

import java.time.LocalDate;

public record Let(String id, LetPodaci letPodaci, LocalDate datumPolijetanja) {

  public static Let kreirajLet(LetPodaci letPodaci, LocalDate datumPolijetanja) {

    var vrijeme = letPodaci.vrijemePolijetanja().hashCode() + datumPolijetanja.hashCode();
    var baza = letPodaci.oznakaLeta() + Long.toString(vrijeme);
    var hashBaza = baza.hashCode();
    var id = Integer.toHexString(hashBaza);

    return new Let(id, letPodaci, datumPolijetanja);
  }
}
