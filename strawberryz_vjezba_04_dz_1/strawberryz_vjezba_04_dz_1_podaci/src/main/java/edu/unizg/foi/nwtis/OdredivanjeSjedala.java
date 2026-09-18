package edu.unizg.foi.nwtis;

import java.util.List;

public interface OdredivanjeSjedala {
  boolean inicijaliziraj();

  RezervacijaSjedala rezervirajSjedalo(Let let, String putnik, RazredSjedala razred)
      throws ProblemKodRezervacije;

  RezervacijaSjedala potvrdiRezervacijuSjedala(RezervacijaSjedala rezervacija)
      throws ProblemKodRezervacije;

  List<RezervacijaSjedala> rezervirajSjedalaZaGrupu(Let let, List<String> putnici,
      RazredSjedala razred) throws ProblemKodRezervacije;

  List<RezervacijaSjedala> potvrdiRezervacijuSjedalaGrupe(List<RezervacijaSjedala> rezervacije)
      throws ProblemKodRezervacije;
}
