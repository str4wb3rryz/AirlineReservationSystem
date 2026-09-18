package edu.unizg.foi.nwtis;

import java.time.LocalTime;

public record LetPodaci(String oznakaLeta, AvioTvrtka avioTvrtka, LocalTime vrijemePolijetanja,
    String polazniAerodrom, String odredisniAerodrom, Avion avion) {
}
