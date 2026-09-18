package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name="DNEVNIK_RADA")
@NamedQuery(name="DnevnikRada.findAll", query="SELECT d FROM DnevnikRada d")
public class DnevnikRada implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	private String adresaracunala;

	private String ipadresaracunala;

	private String korisnickoime;

	private String nazivos;

	private String opisrada;

	private String verzijavm;

	private Timestamp vrijeme;

	public DnevnikRada() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAdresaracunala() {
		return this.adresaracunala;
	}

	public void setAdresaracunala(String adresaracunala) {
		this.adresaracunala = adresaracunala;
	}

	public String getIpadresaracunala() {
		return this.ipadresaracunala;
	}

	public void setIpadresaracunala(String ipadresaracunala) {
		this.ipadresaracunala = ipadresaracunala;
	}

	public String getKorisnickoime() {
		return this.korisnickoime;
	}

	public void setKorisnickoime(String korisnickoime) {
		this.korisnickoime = korisnickoime;
	}

	public String getNazivos() {
		return this.nazivos;
	}

	public void setNazivos(String nazivos) {
		this.nazivos = nazivos;
	}

	public String getOpisrada() {
		return this.opisrada;
	}

	public void setOpisrada(String opisrada) {
		this.opisrada = opisrada;
	}

	public String getVerzijavm() {
		return this.verzijavm;
	}

	public void setVerzijavm(String verzijavm) {
		this.verzijavm = verzijavm;
	}

	public Timestamp getVrijeme() {
		return this.vrijeme;
	}

	public void setVrijeme(Timestamp vrijeme) {
		this.vrijeme = vrijeme;
	}

}
