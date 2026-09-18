package edu.unizg.foi.nwtis.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.Grupa;


/**
 *
 * @author Dragutin Kermek
 */
public class GrupaDAO {
  private Connection vezaBP;

  public GrupaDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }

  public List<Grupa> dohvatiSve() {
	    String upit = "SELECT grupa, naziv FROM grupe";

	    List<Grupa> grupe = new ArrayList<>();

	    try (Statement s = this.vezaBP.createStatement(); ResultSet rs = s.executeQuery(upit)) {

	      while (rs.next()) {
	        String grupa = rs.getString("grupa");
	        String naziv = rs.getString("naziv");
	        Grupa k = new Grupa(grupa, naziv);

	        grupe.add(k);
	      }
	      return grupe;

	    } catch (SQLException ex) {
	      Logger.getLogger(GrupaDAO.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    return null;
	  }
  
  public Grupa dohvati(String gid) {

	  String upit =
	      "SELECT grupa, naziv "
	      + "FROM grupe "
	      + "WHERE grupa = ?";

	  try (PreparedStatement s =
	      this.vezaBP.prepareStatement(upit)) {

	    s.setString(1, gid);

	    ResultSet rs = s.executeQuery();

	    if (rs.next()) {

	      return new Grupa(
	          rs.getString("grupa"),
	          rs.getString("naziv"));
	    }

	  } catch (SQLException ex) {
	    Logger.getLogger(GrupaDAO.class.getName())
	        .log(Level.SEVERE, null, ex);
	  }

	  return null;
	}
 
  public boolean dodaj(Grupa grupa) {

	  String upit =
	      "INSERT INTO grupe(grupa, naziv) "
	      + "VALUES (?, ?)";

	  try (PreparedStatement s =
	      this.vezaBP.prepareStatement(upit)) {

	    s.setString(1, grupa.grupa());
	    s.setString(2, grupa.naziv());

	    return s.executeUpdate() == 1;

	  } catch (SQLException ex) {

	    Logger.getLogger(GrupaDAO.class.getName())
	        .log(Level.SEVERE, null, ex);
	  }

	  return false;
	}
  
  public boolean azuriraj(
		    String gid,
		    Grupa grupa) {

		  String upit =
		      "UPDATE grupe "
		      + "SET naziv = ? "
		      + "WHERE grupa = ?";

		  try (PreparedStatement s =
		      this.vezaBP.prepareStatement(upit)) {

		    s.setString(1, grupa.naziv());
		    s.setString(2, gid);

		    return s.executeUpdate() == 1;

		  } catch (SQLException ex) {

		    Logger.getLogger(GrupaDAO.class.getName())
		        .log(Level.SEVERE, null, ex);
		  }

		  return false;
		}
  
  public boolean obrisi(String gid) {

	  String upit =
	      "DELETE FROM grupe "
	      + "WHERE grupa = ?";

	  try (PreparedStatement s =
	      this.vezaBP.prepareStatement(upit)) {

	    s.setString(1, gid);

	    return s.executeUpdate() == 1;

	  } catch (SQLException ex) {

	    Logger.getLogger(GrupaDAO.class.getName())
	        .log(Level.SEVERE, null, ex);
	  }

	  return false;
	}
  
  public boolean obrisiSveKorisnike(String gid) {
	  String upit =
	      "DELETE FROM uloge "
	      + "WHERE grupa = ?";

	  try (PreparedStatement s =
	      this.vezaBP.prepareStatement(upit)) {

	    s.setString(1, gid);
	    s.executeUpdate();
	    return true;

	  } catch (SQLException ex) {
	    Logger.getLogger(GrupaDAO.class.getName())
	        .log(Level.SEVERE, null, ex);
	  }

	  return false;
	}
  
  public List<String> dohvatiKorisnike(
		    String gid) {

		  String upit =
		      "SELECT korisnik "
		      + "FROM uloge "
		      + "WHERE grupa = ?";

		  List<String> korisnici =
		      new ArrayList<>();

		  try (PreparedStatement s =
		      this.vezaBP.prepareStatement(upit)) {

		    s.setString(1, gid);

		    ResultSet rs = s.executeQuery();

		    while (rs.next()) {

		      korisnici.add(
		          rs.getString("korisnik"));
		    }

		    return korisnici;

		  } catch (SQLException ex) {

		    Logger.getLogger(GrupaDAO.class.getName())
		        .log(Level.SEVERE, null, ex);
		  }

		  return null;
		}
  
  public boolean dodajKorisnika(
		    String gid,
		    String korisnik) {

		  String upit =
		      "INSERT INTO uloge(korisnik, grupa) "
		      + "VALUES (?, ?)";

		  try (PreparedStatement s =
		      this.vezaBP.prepareStatement(upit)) {

		    s.setString(1, korisnik);
		    s.setString(2, gid);

		    return s.executeUpdate() == 1;

		  } catch (SQLException ex) {

		    Logger.getLogger(GrupaDAO.class.getName())
		        .log(Level.SEVERE, null, ex);
		  }

		  return false;
		}
  
  public boolean obrisiKorisnika(
		    String gid,
		    String korisnik) {

		  String upit =
		      "DELETE FROM uloge "
		      + "WHERE grupa = ? "
		      + "AND korisnik = ?";

		  try (PreparedStatement s =
		      this.vezaBP.prepareStatement(upit)) {

		    s.setString(1, gid);
		    s.setString(2, korisnik);

		    return s.executeUpdate() == 1;

		  } catch (SQLException ex) {

		    Logger.getLogger(GrupaDAO.class.getName())
		        .log(Level.SEVERE, null, ex);
		  }

		  return false;
		}
  
}
