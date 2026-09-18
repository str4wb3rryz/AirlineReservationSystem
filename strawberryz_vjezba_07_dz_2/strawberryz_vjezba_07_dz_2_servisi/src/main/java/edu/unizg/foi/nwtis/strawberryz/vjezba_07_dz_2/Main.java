package edu.unizg.foi.nwtis.strawberryz.vjezba_07_dz_2;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

/**
 * Main class.
 *
 */
public class Main {
  private static String korisnickoImeBazaPodataka;
  private static String lozinkaBazaPodataka;
  private static String upravljacBazaPodataka;
  private static String urlBazaPodataka;
  private static String urlServis;

  // Base URI the Grizzly HTTP server will listen on
  // public static String BASE_URI = "http://20.24.5.5:8080/";
  public static String BASE_URI = "http://localhost:8080/";

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   * 
   * @return Grizzly HTTP server.
   */
  public static HttpServer startServer() {
    // create a resource config that scans for JAX-RS resources and providers
    // in edu.unizg.foi.nwtis.vjezba_07_dz_2 package
    final ResourceConfig rc =
        new ResourceConfig().packages("edu.unizg.foi.nwtis.strawberryz.vjezba_07_dz_2")
            .register(org.glassfish.jersey.jackson.JacksonFeature.class);

    // create and start a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
  }

  /**
   * Main method.
   * 
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws Exception {
    var nazivDatoteke = args[0];
    if (!ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }
    BASE_URI = urlServis;

    final HttpServer server = startServer();
    System.out.println(String.format(
        "Jersey app started with endpoints available at " + "%s%nHit Ctrl-C to stop it...",
        BASE_URI));

    do {
      if (System.in.read() == 32) {
        break;
      }
      Thread.sleep(Duration.ofSeconds(5));
    } while (true);

    server.shutdown(5, TimeUnit.SECONDS);
  }

  /**
   * Ucitaj konfiguraciju.
   *
   * @param nazivDatoteke naziv datoteke
   * @return true, ako je uspješno učitavanje konfiguracije
   */
  public static boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      var konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      korisnickoImeBazaPodataka = konfig.dajPostavku("korisnickoImeBazaPodataka");
      lozinkaBazaPodataka = konfig.dajPostavku("lozinkaBazaPodataka");
      upravljacBazaPodataka = konfig.dajPostavku("upravljacBazaPodataka");
      urlBazaPodataka = konfig.dajPostavku("urlBazaPodataka");
      urlServis = konfig.dajPostavku("urlServis");

      return true;
    } catch (NeispravnaKonfiguracija _) {
      // Nije potrebna obrada iznimke
    }
    return false;
  }

  public static Connection dajVezu() throws Exception {
    Class.forName(upravljacBazaPodataka);
    var vezaBazaPodataka = DriverManager.getConnection(urlBazaPodataka, korisnickoImeBazaPodataka,
        lozinkaBazaPodataka);
    return vezaBazaPodataka;
  }

}
