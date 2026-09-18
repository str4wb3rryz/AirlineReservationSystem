package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.unizg.foi.nwtis.RezervacijaSjedala;

@Named
@SessionScoped
public class RadneRezervacije implements Serializable {

    private String idUlaznice;
    private String posluzitelj;
    private List<RezervacijaSjedala> rezervacije = new ArrayList<>();

    private String putnikKorisnickoIme;
    private String putnikIme;
    private String putnikPrezime;
    private String putnikEmail;

    public String getIdUlaznice() { return idUlaznice; }
    public void setIdUlaznice(String idUlaznice) { this.idUlaznice = idUlaznice; }
    public String getPosluzitelj() { return posluzitelj; }
    public void setPosluzitelj(String posluzitelj) { this.posluzitelj = posluzitelj; }

    public boolean isPosluziteljOdabran() {
        return posluzitelj != null && !posluzitelj.isBlank();
    }

    public boolean isUlaznicaKreirana() {
        return idUlaznice != null && !idUlaznice.isBlank();
    }

    public List<RezervacijaSjedala> getRezervacije() { return rezervacije; }
    public void setRezervacije(List<RezervacijaSjedala> rezervacije) { this.rezervacije = rezervacije; }

    public String getPutnikKorisnickoIme() { return putnikKorisnickoIme; }
    public void setPutnikKorisnickoIme(String putnikKorisnickoIme) { this.putnikKorisnickoIme = putnikKorisnickoIme; }
    public String getPutnikIme() { return putnikIme; }
    public void setPutnikIme(String putnikIme) { this.putnikIme = putnikIme; }
    public String getPutnikPrezime() { return putnikPrezime; }
    public void setPutnikPrezime(String putnikPrezime) { this.putnikPrezime = putnikPrezime; }
    public String getPutnikEmail() { return putnikEmail; }
    public void setPutnikEmail(String putnikEmail) { this.putnikEmail = putnikEmail; }

    public boolean isPutnikOdabran() {
        return putnikKorisnickoIme != null && !putnikKorisnickoIme.isBlank();
    }

}
