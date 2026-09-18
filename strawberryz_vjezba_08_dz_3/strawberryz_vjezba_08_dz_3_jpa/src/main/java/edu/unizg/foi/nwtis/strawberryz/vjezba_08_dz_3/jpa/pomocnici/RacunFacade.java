package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.RacunStavka;
import edu.unizg.foi.nwtis.RacunStavka;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Korisnici_;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Racun;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Racun_;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Racunstavka;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Racunstavka_;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Stateless
public class RacunFacade extends EntityManagerProducer implements Serializable {

  private static final long serialVersionUID = -2871826465957278071L;
  private CriteriaBuilder cb;

  @PostConstruct
  private void init() {
    cb = getEntityManager().getCriteriaBuilder();
  }

  public void create(Racun racun) {
    getEntityManager().persist(racun);
  }

  public void edit(Racun racun) {
    getEntityManager().merge(racun);
  }

  public void remove(Racun racun) {
    getEntityManager().remove(getEntityManager().merge(racun));
  }

  public Racun find(Object id) {
    return getEntityManager()
        .find(Racun.class, id);
  }

  public List<Racun> findAll() {
    CriteriaQuery<Racun> cq =
        cb.createQuery(Racun.class);
    cq.select(cq.from(
        Racun.class));
    return getEntityManager().createQuery(cq).getResultList();
  }

  public List<Racun> findByKorisnik(
      String korisnickoIme) {
    CriteriaQuery<Racun> cq =
        cb.createQuery(Racun.class);
    Root<Racun> racun =
        cq.from(Racun.class);
    Join<Racun, Korisnici> korisnici =
        racun.join(Racun_.korisnici);
    cq.where(cb.equal(korisnici.get("korisnik"), korisnickoIme));
    cq.orderBy(cb.desc(racun.get(Racun_.vrijeme)));
    TypedQuery<Racun> q =
        getEntityManager().createQuery(cq);
    return q.getResultList();
  }

  public Racun findOtvoreniRacun(
      String korisnickoIme) {
    CriteriaQuery<Racun> cq =
        cb.createQuery(Racun.class);
    Root<Racun> racun =
        cq.from(Racun.class);
    Join<Racun, Korisnici> korisnici =
        racun.join(Racun_.korisnici);

    Predicate zaKorisnika = cb.equal(korisnici.get("korisnik"), korisnickoIme);
    Predicate otvoren = cb.equal(racun.get(Racun_.iznos), 0.0);
    cq.where(cb.and(zaKorisnika, otvoren));
    cq.orderBy(cb.desc(racun.get(Racun_.vrijeme)));

    TypedQuery<Racun> q =
        getEntityManager().createQuery(cq);
    q.setMaxResults(1);
    List<Racun> rez =
        q.getResultList();
    return rez.isEmpty() ? null : rez.get(0);
  }

  public boolean imaOtvorenihRacuna(String korisnickoIme) {
    return findOtvoreniRacun(korisnickoIme) != null;
  }

  public List<Racun> findZatvoreni(
      String imeRacuna, String prezimeRacuna, String imeStavke, String prezimeStavke) {

    CriteriaQuery<Racun> cq =
        cb.createQuery(Racun.class);
    Root<Racun> racun =
        cq.from(Racun.class);

    List<Predicate> predikati = new ArrayList<>();
    predikati.add(cb.greaterThan(racun.get(Racun_.iznos), 0.0));

    if (imeRacuna != null && !imeRacuna.isBlank()) {
      Join<Racun, Korisnici>
          korisnici = racun.join(Racun_.korisnici);
      predikati.add(cb.like(cb.lower(korisnici.get("ime")), "%" + imeRacuna.toLowerCase() + "%"));
    }
    if (prezimeRacuna != null && !prezimeRacuna.isBlank()) {
      Join<Racun, Korisnici>
          korisnici = racun.join(Racun_.korisnici);
      predikati.add(cb.like(cb.lower(korisnici.get("prezime")),
          "%" + prezimeRacuna.toLowerCase() + "%"));
    }
    if ((imeStavke != null && !imeStavke.isBlank())
        || (prezimeStavke != null && !prezimeStavke.isBlank())) {
      Join<Racun, Racunstavka>
          stavke = racun.join(Racun_.racunstavkas);
      if (imeStavke != null && !imeStavke.isBlank()) {
        predikati.add(cb.like(cb.lower(stavke.get(Racunstavka_.putnikime)),
            "%" + imeStavke.toLowerCase() + "%"));
      }
      if (prezimeStavke != null && !prezimeStavke.isBlank()) {
        predikati.add(cb.like(cb.lower(stavke.get(Racunstavka_.putnikiprezime)),
            "%" + prezimeStavke.toLowerCase() + "%"));
      }
      cq.distinct(true);
    }

    cq.where(cb.and(predikati.toArray(new Predicate[0])));
    cq.orderBy(cb.desc(racun.get(Racun_.vrijeme)));

    TypedQuery<Racun> q =
        getEntityManager().createQuery(cq);
    return q.getResultList();
  }

  public long stvarniBrojStavki(int racunId) {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<Racunstavka> stavke = cq.from(Racunstavka.class);
    cq.select(cb.count(stavke));
    cq.where(cb.equal(stavke.get(Racunstavka_.racun).get(Racun_.id), racunId));
    return getEntityManager().createQuery(cq).getSingleResult();
  }

  public double zbrojIznosaStavki(int racunId) {
    CriteriaQuery<Double> cq = cb.createQuery(Double.class);
    Root<Racunstavka> stavke = cq.from(Racunstavka.class);
    cq.select(cb.coalesce(cb.sum(stavke.get(Racunstavka_.iznos)), 0.0));
    cq.where(cb.equal(stavke.get(Racunstavka_.racun).get(Racun_.id), racunId));
    return getEntityManager().createQuery(cq).getSingleResult();
  }

  public boolean jeUsklađen(
      Racun racun) {
    return racun.getBrojstavki() == stvarniBrojStavki(racun.getId());
  }

  public void uskladiBrojStavki(
      Racun racun) {
    long stvarni = stvarniBrojStavki(racun.getId());
    racun.setBrojstavki((int) stvarni);
    edit(racun);
  }

  public Racun zatvoriRacun(
      Racun racun) {
    double ukupno = zbrojIznosaStavki(racun.getId());
    ukupno = Math.round(ukupno * 100.0) / 100.0;

    racun.setIznos(ukupno);
    edit(racun);

    return racun;
  }

  public Racun otvoriNoviRacun(
      Korisnici korisnik) {
    var novi = new Racun();
    novi.setKorisnici(korisnik);
    novi.setBrojstavki(0);
    novi.setIznos(0.0);
    novi.setVrijeme(Timestamp.valueOf(LocalDateTime.now()));
    create(novi);
    return novi;
  }
}
