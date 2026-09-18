package edu.unizg.foi.nwtis.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.AvioTvrtka;

public class AvioTvrtkeDAO {
  private Connection vezaBP;

  public AvioTvrtkeDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }

  public List<AvioTvrtka> dohvatiSve() {
    String upit = "SELECT tvrtka, naziv FROM aviotvrtke";
    List<AvioTvrtka> avioTvrtke = new ArrayList<>();

    try (Statement s = this.vezaBP.createStatement();
         ResultSet rs = s.executeQuery(upit)) {

      while (rs.next()) {
        String tvrtka = rs.getString("tvrtka");
        String naziv  = rs.getString("naziv");
        avioTvrtke.add(new AvioTvrtka(tvrtka, naziv, new ConcurrentHashMap<>()));
      }
      return avioTvrtke;

    } catch (SQLException ex) {
      Logger.getLogger(AvioTvrtkeDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public AvioTvrtka dohvati(String atid) {
    String upit =
        "SELECT tvrtka, naziv "
        + "FROM aviotvrtke "
        + "WHERE tvrtka = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, atid);
      ResultSet rs = s.executeQuery();

      if (rs.next()) {
        return new AvioTvrtka(
            rs.getString("tvrtka"),
            rs.getString("naziv"), new ConcurrentHashMap<>());
      }

    } catch (SQLException ex) {
      Logger.getLogger(AvioTvrtkeDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public boolean dodaj(AvioTvrtka avioTvrtka) {
    String upit =
        "INSERT INTO aviotvrtke(tvrtka, naziv) "
        + "VALUES (?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, avioTvrtka.id());
      s.setString(2, avioTvrtka.naziv());
      return s.executeUpdate() == 1;

    } catch (SQLException ex) {
      Logger.getLogger(AvioTvrtkeDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public boolean azuriraj(String atid, AvioTvrtka avioTvrtka) {
    String upit =
        "UPDATE aviotvrtke "
        + "SET naziv = ? "
        + "WHERE tvrtka = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, avioTvrtka.naziv());
      s.setString(2, atid);
      return s.executeUpdate() == 1;

    } catch (SQLException ex) {
      Logger.getLogger(AvioTvrtkeDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public boolean obrisi(String atid) {
    String upit =
        "DELETE FROM aviotvrtke "
        + "WHERE tvrtka = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, atid);
      return s.executeUpdate() == 1;

    } catch (SQLException ex) {
      Logger.getLogger(AvioTvrtkeDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public boolean obrisiSveZastupnike(String atid) {
    String upit =
        "DELETE FROM zastupnici "
        + "WHERE tvrtka = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, atid);
      s.executeUpdate();
      return true;

    } catch (SQLException ex) {
      Logger.getLogger(AvioTvrtkeDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public List<String> dohvatiZastupnike(String atid) {
    String upit =
        "SELECT korisnik "
        + "FROM zastupnici "
        + "WHERE tvrtka = ?";

    List<String> zastupnici = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, atid);
      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        zastupnici.add(rs.getString("korisnik"));
      }
      return zastupnici;

    } catch (SQLException ex) {
      Logger.getLogger(AvioTvrtkeDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public boolean dodajZastupnika(String atid, String korisnik) {
    String upit =
        "INSERT INTO zastupnici(korisnik, tvrtka) "
        + "VALUES (?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);
      s.setString(2, atid);
      return s.executeUpdate() == 1;

    } catch (SQLException ex) {
      Logger.getLogger(AvioTvrtkeDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public boolean obrisiZastupnika(String atid, String korisnik) {
    String upit =
        "DELETE FROM zastupnici "
        + "WHERE tvrtka = ? "
        + "AND korisnik = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, atid);
      s.setString(2, korisnik);
      return s.executeUpdate() == 1;

    } catch (SQLException ex) {
      Logger.getLogger(AvioTvrtkeDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }
}