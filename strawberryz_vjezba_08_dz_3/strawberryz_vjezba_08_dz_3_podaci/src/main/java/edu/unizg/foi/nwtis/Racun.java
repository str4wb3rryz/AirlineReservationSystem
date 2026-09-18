package edu.unizg.foi.nwtis;

import java.time.LocalDateTime;
import java.util.List;

public record Racun(int id, LocalDateTime vrijeme, String korisnik, int brojStavki, double iznos,
    List<RacunStavka> stavke) {

}
