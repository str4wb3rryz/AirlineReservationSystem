package edu.unizg.foi.nwtis;

import java.util.Map;

public record AvioTvrtka(String id, String naziv, Map<String, Avion> avioni) {
  public boolean postojiAvion(Avion avion) {
    return this.avioni.containsKey(avion.id());
  }

  public AvioTvrtka dodajAvion(Avion avion) {
    if (this.avioni.containsKey(avion.id())) {
      return this;
    } else {
      this.avioni.put(avion.id(), avion);
      return new AvioTvrtka(this.id, this.naziv, this.avioni);
    }
  }

}
