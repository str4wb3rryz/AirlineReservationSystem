package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.List;

@Entity
@NamedQuery(name="Aviotvrtke.findAll", query="SELECT a FROM Aviotvrtke a")
public class Aviotvrtke implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String tvrtka;

	private String naziv;

	@OneToMany(mappedBy="aviotvrtke")
	private List<Zastupnici> zastupnicis;

	public Aviotvrtke() {
	}

	public String getTvrtka() {
		return this.tvrtka;
	}

	public void setTvrtka(String tvrtka) {
		this.tvrtka = tvrtka;
	}

	public String getNaziv() {
		return this.naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public List<Zastupnici> getZastupnicis() {
		return this.zastupnicis;
	}

	public void setZastupnicis(List<Zastupnici> zastupnicis) {
		this.zastupnicis = zastupnicis;
	}

	public Zastupnici addZastupnici(Zastupnici zastupnici) {
		getZastupnicis().add(zastupnici);
		zastupnici.setAviotvrtke(this);

		return zastupnici;
	}

	public Zastupnici removeZastupnici(Zastupnici zastupnici) {
		getZastupnicis().remove(zastupnici);
		zastupnici.setAviotvrtke(null);

		return zastupnici;
	}

}
