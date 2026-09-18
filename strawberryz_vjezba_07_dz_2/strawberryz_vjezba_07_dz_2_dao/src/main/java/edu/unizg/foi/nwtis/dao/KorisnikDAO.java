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
import edu.unizg.foi.nwtis.Korisnik;


/**
 *
 * @author Dragutin Kermek
 */
public class KorisnikDAO {
  private Connection vezaBP;

  public KorisnikDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }

  public Korisnik dohvati(String korisnik, String lozinka, Boolean prijava) {
    String upit = "SELECT ime, prezime, korisnik, lozinka, email FROM korisnici WHERE korisnik = ?";

    if (prijava) {
      upit += " and lozinka = ?";
    }

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, korisnik);
      if (prijava) {
        s.setString(2, lozinka);
      }
      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        String ime = rs.getString("ime");
        String prezime = rs.getString("prezime");
        String email = rs.getString("email");

        Korisnik k = new Korisnik(korisnik, "******", prezime, ime, email);
        return k;
      }

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public Korisnik dohvati(String korisnik, String lozinka) {
    String upit =
        "SELECT ime, prezime, korisnik, lozinka, email FROM korisnici WHERE korisnik = ? and lozinka = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, korisnik);
      s.setString(2, lozinka);
      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        String ime = rs.getString("ime");
        String prezime = rs.getString("prezime");
        String email = rs.getString("email");

        Korisnik k = new Korisnik(korisnik, lozinka, prezime, ime, email);
        return k;
      }

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public boolean postoji(String korisnik) {
    String upit = "SELECT ime, prezime, korisnik, lozinka, email FROM korisnici WHERE korisnik = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, korisnik);
      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        return true;
      }

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public boolean obrisi(String korisnik) {
    String upit = "DELETE FROM korisnici WHERE korisnik = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, korisnik);
      if (s.executeUpdate() == 1) {
        return true;
      }
    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public List<Korisnik> dohvatiSve() {
    String upit = "SELECT ime, prezime, email, korisnik, lozinka FROM korisnici";

    List<Korisnik> korisnici = new ArrayList<>();

    try (Statement s = this.vezaBP.createStatement(); ResultSet rs = s.executeQuery(upit)) {

      while (rs.next()) {
        String korisnik1 = rs.getString("korisnik");
        String ime = rs.getString("ime");
        String prezime = rs.getString("prezime");
        String email = rs.getString("email");
        Korisnik k = new Korisnik(korisnik1, "******", prezime, ime, email);

        korisnici.add(k);
      }
      return korisnici;

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public List<Korisnik> dohvatiPrezimeIme(String pPrezime, String pIme) {
    String upit =
        "SELECT ime, prezime, email, korisnik, lozinka FROM korisnici WHERE prezime LIKE ? AND ime LIKE ?";

    List<Korisnik> korisnici = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit);) {

      s.setString(1, pPrezime);
      s.setString(2, pIme);
      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        String korisnik1 = rs.getString("korisnik");
        String ime = rs.getString("ime");
        String prezime = rs.getString("prezime");
        String email = rs.getString("email");
        Korisnik k = new Korisnik(korisnik1, "******", prezime, ime, email);

        korisnici.add(k);
      }
      rs.close();
      return korisnici;

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public boolean azuriraj(Korisnik k) {
    String upit = "UPDATE korisnici SET ime = ?, prezime = ?, email = ?, lozinka = ? "
        + " WHERE korisnik = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, k.ime());
      s.setString(2, k.prezime());
      s.setString(3, k.email());
      s.setString(4, k.lozinka());
      s.setString(5, k.korisnik());

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public boolean dodaj(Korisnik k) {
    String upit = "INSERT INTO korisnici (ime, prezime, email, korisnik, lozinka) "
        + "VALUES (?, ?, ?, ?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, k.ime());
      s.setString(2, k.prezime());
      s.setString(3, k.email());
      s.setString(4, k.korisnik());
      s.setString(5, k.lozinka());

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (Exception ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }
  
  ////////////////////////////////////////////////// 

  public Korisnik dohvatiEmail(String email) {

	  String upit =
	      "SELECT * FROM korisnici WHERE email = ?";

	  try (PreparedStatement s =
	      this.vezaBP.prepareStatement(upit)) {

	    s.setString(1, email);

	    ResultSet rs = s.executeQuery();

	    if (rs.next()) {

	      return new Korisnik(
	          rs.getString("korisnik"),
	          "******",
	          rs.getString("prezime"),
	          rs.getString("ime"),
	          rs.getString("email"));
	    }

	  } catch (SQLException ex) {
	    Logger.getLogger(KorisnikDAO.class.getName())
	        .log(Level.SEVERE, null, ex);
	  }

	  return null;
	}
  
  public List<Korisnik> dohvatiSveStranicenje(
		    int odStranice,
		    int brojStranica) {

		  String upit =
		      "SELECT * FROM korisnici LIMIT ? OFFSET ?";

		  List<Korisnik> korisnici = new ArrayList<>();

		  try (PreparedStatement s =
		      this.vezaBP.prepareStatement(upit)) {

		    s.setInt(1, brojStranica);
		    s.setInt(2, odStranice);

		    ResultSet rs = s.executeQuery();

		    while (rs.next()) {

		      korisnici.add(new Korisnik(
		          rs.getString("korisnik"),
		          "******",
		          rs.getString("prezime"),
		          rs.getString("ime"),
		          rs.getString("email")));
		    }

		    return korisnici;

		  } catch (SQLException ex) {
		    Logger.getLogger(KorisnikDAO.class.getName())
		        .log(Level.SEVERE, null, ex);
		  }

		  return null;
		}
  
  public List<Korisnik> dohvatiImePrezimeStranicenje(
		    String ime,
		    String prezime,
		    int odStranice,
		    int brojStranica) {

		  String upit =
		      "SELECT * FROM korisnici "
		      + "WHERE ime LIKE ? "
		      + "AND prezime LIKE ? "
		      + "LIMIT ? OFFSET ?";

		  List<Korisnik> korisnici = new ArrayList<>();

		  try (PreparedStatement s =
		      this.vezaBP.prepareStatement(upit)) {

		    s.setString(1, "%" + ime + "%");
		    s.setString(2, "%" + prezime + "%");
		    s.setInt(3, brojStranica);
		    s.setInt(4, odStranice);

		    ResultSet rs = s.executeQuery();

		    while (rs.next()) {

		      korisnici.add(new Korisnik(
		          rs.getString("korisnik"),
		          "******",
		          rs.getString("prezime"),
		          rs.getString("ime"),
		          rs.getString("email")));
		    }

		    return korisnici;

		  } catch (SQLException ex) {
		    Logger.getLogger(KorisnikDAO.class.getName())
		        .log(Level.SEVERE, null, ex);
		  }

		  return null;
		}
  
  public List<String> dohvatiGrupe(String korisnik) {

	  String upit =
	      "SELECT grupa FROM uloge WHERE korisnik = ?";

	  List<String> grupe = new ArrayList<>();

	  try (PreparedStatement s =
	      this.vezaBP.prepareStatement(upit)) {

	    s.setString(1, korisnik);

	    ResultSet rs = s.executeQuery();

	    while (rs.next()) {
	      grupe.add(rs.getString("grupa"));
	    }

	    return grupe;

	  } catch (SQLException ex) {
	    Logger.getLogger(KorisnikDAO.class.getName())
	        .log(Level.SEVERE, null, ex);
	  }

	  return null;
	}
  
  public boolean dodajGrupu(
		    String korisnik,
		    String grupa) {

		  String upit =
		      "INSERT INTO uloge(korisnik, grupa) "
		      + "VALUES (?, ?)";

		  try (PreparedStatement s =
		      this.vezaBP.prepareStatement(upit)) {

		    s.setString(1, korisnik);
		    s.setString(2, grupa);

		    return s.executeUpdate() == 1;

		  } catch (SQLException ex) {
		    Logger.getLogger(KorisnikDAO.class.getName())
		        .log(Level.SEVERE, null, ex);
		  }

		  return false;
		}
  
  public boolean obrisiGrupu(
		    String korisnik,
		    String grupa) {

		  String upit =
		      "DELETE FROM uloge "
		      + "WHERE korisnik = ? "
		      + "AND grupa = ?";

		  try (PreparedStatement s =
		      this.vezaBP.prepareStatement(upit)) {

		    s.setString(1, korisnik);
		    s.setString(2, grupa);

		    return s.executeUpdate() == 1;

		  } catch (SQLException ex) {
		    Logger.getLogger(KorisnikDAO.class.getName())
		        .log(Level.SEVERE, null, ex);
		  }

		  return false;
		}
  
  public String dohvatiAviotvrtku(String korisnik) {

	  String upit =
	      "SELECT tvrtka FROM zastupnici "
	      + "WHERE korisnik = ?";

	  try (PreparedStatement s =
	      this.vezaBP.prepareStatement(upit)) {

	    s.setString(1, korisnik);

	    ResultSet rs = s.executeQuery();

	    if (rs.next()) {
	      return rs.getString("tvrtka");
	    }

	  } catch (SQLException ex) {
	    Logger.getLogger(KorisnikDAO.class.getName())
	        .log(Level.SEVERE, null, ex);
	  }

	  return null;
	}
  
  public boolean dodajAviotvrtku(
		    String korisnik,
		    String tvrtka) {

		  String upit =
		      "INSERT INTO zastupnici(korisnik, tvrtka) "
		      + "VALUES (?, ?)";

		  try (PreparedStatement s =
		      this.vezaBP.prepareStatement(upit)) {

		    s.setString(1, korisnik);
		    s.setString(2, tvrtka);

		    return s.executeUpdate() == 1;

		  } catch (SQLException ex) {
		    Logger.getLogger(KorisnikDAO.class.getName())
		        .log(Level.SEVERE, null, ex);
		  }

		  return false;
		}
  
  public boolean obrisiAviotvrtku(
		    String korisnik,
		    String tvrtka) {

		  String upit =
		      "DELETE FROM zastupnici "
		      + "WHERE korisnik = ? "
		      + "AND tvrtka = ?";

		  try (PreparedStatement s =
		      this.vezaBP.prepareStatement(upit)) {

		    s.setString(1, korisnik);
		    s.setString(2, tvrtka);

		    return s.executeUpdate() == 1;

		  } catch (SQLException ex) {
		    Logger.getLogger(KorisnikDAO.class.getName())
		        .log(Level.SEVERE, null, ex);
		  }

		  return false;
		}
  
  public String dohvatiAviotvrtkuEmail(
		    String email) {

		  String upit =
		      "SELECT z.tvrtka "
		      + "FROM zastupnici z "
		      + "JOIN korisnici k "
		      + "ON z.korisnik = k.korisnik "
		      + "WHERE k.email = ?";

		  try (PreparedStatement s =
		      this.vezaBP.prepareStatement(upit)) {

		    s.setString(1, email);

		    ResultSet rs = s.executeQuery();

		    if (rs.next()) {
		      return rs.getString("tvrtka");
		    }

		  } catch (SQLException ex) {
		    Logger.getLogger(KorisnikDAO.class.getName())
		        .log(Level.SEVERE, null, ex);
		  }

		  return null;
		}

}
