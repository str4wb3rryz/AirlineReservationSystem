package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.List;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Aviotvrtke;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Aviotvrtke_;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Zastupnici;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Zastupnici_;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

@Stateless
public class AvioTvrtkeFacade extends EntityManagerProducer implements Serializable {
  private static final long serialVersionUID = -6255662830932462596L;

  private CriteriaBuilder cb;

  @PostConstruct
  private void init() {
    cb = getEntityManager().getCriteriaBuilder();
  }

  public void create(Aviotvrtke aviotvrtke) {
    getEntityManager().persist(aviotvrtke);
  }

  public void edit(Aviotvrtke aviotvrtke) {
    getEntityManager().merge(aviotvrtke);
  }

  public void remove(Aviotvrtke aviotvrtke) {
    getEntityManager().remove(getEntityManager().merge(aviotvrtke));
  }

  public Aviotvrtke find(Object id) {
    return getEntityManager().find(Aviotvrtke.class, id);
  }

  public List<Aviotvrtke> findAll() {
    CriteriaQuery<Aviotvrtke> cq = cb.createQuery(Aviotvrtke.class);
    cq.select(cq.from(Aviotvrtke.class));
    return getEntityManager().createQuery(cq).getResultList();
  }

  public List<Aviotvrtke> findRange(int[] range) {
    CriteriaQuery<Aviotvrtke> cq = cb.createQuery(Aviotvrtke.class);
    cq.select(cq.from(Aviotvrtke.class));
    TypedQuery<Aviotvrtke> q = getEntityManager().createQuery(cq);
    q.setMaxResults(range[1] - range[0]);
    q.setFirstResult(range[0]);
    return q.getResultList();
  }

  public int count() {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    cq.select(cb.count(cq.from(Aviotvrtke.class)));
    return ((Long) getEntityManager().createQuery(cq).getSingleResult()).intValue();
  }

  public List<Zastupnici> findZastupnici(String tvrtka) {
    CriteriaQuery<Zastupnici> cq = cb.createQuery(Zastupnici.class);
    Root<Zastupnici> zastupnici = cq.from(Zastupnici.class);
    Expression<String> zaTvrtku = zastupnici.get(Zastupnici_.aviotvrtke).get(Aviotvrtke_.tvrtka);
    cq.where(cb.equal(zaTvrtku, tvrtka));
    TypedQuery<Zastupnici> q = getEntityManager().createQuery(cq);
    return q.getResultList();
  }

  public List<Zastupnici> findAllZastupnici() {
    CriteriaQuery<Zastupnici> cq = cb.createQuery(Zastupnici.class);
    cq.select(cq.from(Zastupnici.class));
    return getEntityManager().createQuery(cq).getResultList();
  }

}
