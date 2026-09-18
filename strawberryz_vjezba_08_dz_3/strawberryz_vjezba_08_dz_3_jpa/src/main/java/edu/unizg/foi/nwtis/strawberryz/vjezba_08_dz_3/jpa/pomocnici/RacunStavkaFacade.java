package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.RacunStavka;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Racun;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Racun_;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Racunstavka;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Racunstavka_;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@Stateless
public class RacunStavkaFacade extends EntityManagerProducer implements Serializable {

  private static final long serialVersionUID = -6683395439378356398L;
  private CriteriaBuilder cb;

  @PostConstruct
  private void init() {
    cb = getEntityManager().getCriteriaBuilder();
  }

  public void create(Racunstavka stavka) {
    getEntityManager().persist(stavka);
  }

  public void edit(Racunstavka stavka) {
    getEntityManager().merge(stavka);
  }

  public void remove(Racunstavka stavka) {
    getEntityManager().remove(getEntityManager().merge(stavka));
  }

  public Racunstavka find(Object id) {
    return getEntityManager().find(Racunstavka.class, id);
  }

  public List<Racunstavka> findAll() {
    CriteriaQuery<Racunstavka> cq = cb.createQuery(Racunstavka.class);
    cq.select(cq.from(Racunstavka.class));
    return getEntityManager().createQuery(cq).getResultList();
  }

  public List<Racunstavka> findByRacun(int racunId) {
    CriteriaQuery<Racunstavka> cq = cb.createQuery(Racunstavka.class);
    Root<Racunstavka> stavka = cq.from(Racunstavka.class);
    cq.where(cb.equal(stavka.get(Racunstavka_.racun).get(Racun_.id), racunId));
    TypedQuery<Racunstavka> q = getEntityManager().createQuery(cq);
    return q.getResultList();
  }

  public Racunstavka dodajStavku(Racun racun, String putnikIme, String putnikPrezime,
      String rezervacija, double iznos) {
    var stavka = new Racunstavka();
    stavka.setRacun(racun);
    stavka.setPutnikime(putnikIme);
    stavka.setPutnikiprezime(putnikPrezime);
    stavka.setRezervacija(rezervacija);
    stavka.setIznos(iznos);
    create(stavka);

    racun.setBrojstavki(racun.getBrojstavki() + 1);
    getEntityManager().merge(racun);

    return stavka;
  }

  public RacunStavka pretvori(Racunstavka s) {
    if (s == null) {
      return null;
    }
    return new RacunStavka(s.getId(), s.getRacun() != null ? s.getRacun().getId() : 0,
        s.getPutnikime(), s.getPutnikiprezime(), s.getRezervacija(), s.getIznos());
  }

  public List<RacunStavka> pretvori(List<Racunstavka> stavke) {
    List<RacunStavka> rezultat = new ArrayList<>();
    for (Racunstavka s : stavke) {
      rezultat.add(pretvori(s));
    }
    return rezultat;
  }
}
