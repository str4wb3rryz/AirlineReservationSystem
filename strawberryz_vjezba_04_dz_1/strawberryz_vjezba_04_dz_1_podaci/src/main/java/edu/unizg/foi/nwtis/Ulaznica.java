package edu.unizg.foi.nwtis;

import java.time.LocalDateTime;

public record Ulaznica(String id, String email, String usluga, LocalDateTime vaziDo) {
  public boolean ispravnaUlaznica(String email) {
    return this.email.compareTo(email) == 0;
  }

  public boolean vazecaUlaznica(String usluga, long miliSec) {
    var vrijeme = LocalDateTime.now();
    return (this.usluga.compareTo(usluga) == 0
        && this.vaziDo.plusNanos(miliSec * 1_000_000).isAfter(vrijeme));
  }

  public Ulaznica produzi(long miliSec) {
    var vrijeme = LocalDateTime.now().plusNanos(miliSec * 1_000_000);
    return new Ulaznica(id, email, this.usluga, vrijeme);
  }

}
