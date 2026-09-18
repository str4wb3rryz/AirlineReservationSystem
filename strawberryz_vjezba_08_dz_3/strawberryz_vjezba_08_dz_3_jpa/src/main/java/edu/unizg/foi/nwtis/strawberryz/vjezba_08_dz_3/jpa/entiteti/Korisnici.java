package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.List;

@Entity
@NamedQuery(name="Korisnici.findAll", query="SELECT k FROM Korisnici k")
public class Korisnici implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String korisnik;

	private String email;

	private String ime;

	private String lozinka;

	private String prezime;

	@OneToMany(mappedBy="korisnici")
	private List<Dnevnik> dnevniks;

	@OneToMany(mappedBy="korisnici")
	private List<Racun> racuns;

	@ManyToMany
	@JoinTable(
		name="ULOGE"
		, joinColumns={
@JoinColumn(name="KORISNIK")
			}
		, inverseJoinColumns={
@JoinColumn(name="GRUPA")
			}
		)
	private List<Grupe> grupes;

	@OneToOne(mappedBy="korisnici")
	private Zastupnici zastupnici;

	public Korisnici() {
	}

	public String getKorisnik() {
		return this.korisnik;
	}

	public void setKorisnik(String korisnik) {
		this.korisnik = korisnik;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getIme() {
		return this.ime;
	}

	public void setIme(String ime) {
		this.ime = ime;
	}

	public String getLozinka() {
		return this.lozinka;
	}

	public void setLozinka(String lozinka) {
		this.lozinka = lozinka;
	}

	public String getPrezime() {
		return this.prezime;
	}

	public void setPrezime(String prezime) {
		this.prezime = prezime;
	}

	public List<Dnevnik> getDnevniks() {
		return this.dnevniks;
	}

	public void setDnevniks(List<Dnevnik> dnevniks) {
		this.dnevniks = dnevniks;
	}

	public Dnevnik addDnevnik(Dnevnik dnevnik) {
		getDnevniks().add(dnevnik);
		dnevnik.setKorisnici(this);

		return dnevnik;
	}

	public Dnevnik removeDnevnik(Dnevnik dnevnik) {
		getDnevniks().remove(dnevnik);
		dnevnik.setKorisnici(null);

		return dnevnik;
	}

	public List<Racun> getRacuns() {
		return this.racuns;
	}

	public void setRacuns(List<Racun> racuns) {
		this.racuns = racuns;
	}

	public Racun addRacun(Racun racun) {
		getRacuns().add(racun);
		racun.setKorisnici(this);

		return racun;
	}

	public Racun removeRacun(Racun racun) {
		getRacuns().remove(racun);
		racun.setKorisnici(null);

		return racun;
	}

	public List<Grupe> getGrupes() {
		return this.grupes;
	}

	public void setGrupes(List<Grupe> grupes) {
		this.grupes = grupes;
	}

	public Zastupnici getZastupnici() {
		return this.zastupnici;
	}

	public void setZastupnici(Zastupnici zastupnici) {
		this.zastupnici = zastupnici;
	}

}
