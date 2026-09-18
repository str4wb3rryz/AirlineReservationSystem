package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.mikroservisi.pomocnici;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class KlijentMrezneUticnice {

  public String posaljiKomandu(String komanda, String adresa, int mreznaVrata) {
    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrata);
      BufferedReader in = new BufferedReader(
          new InputStreamReader(mreznaUticnica.getInputStream(), StandardCharsets.UTF_8));
      PrintWriter out = new PrintWriter(
          new OutputStreamWriter(mreznaUticnica.getOutputStream(), StandardCharsets.UTF_8));
      out.write(komanda + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      var linija = in.readLine();
      mreznaUticnica.shutdownInput();
      mreznaUticnica.close();
      return linija;
    } catch (IOException e) {
    }
    return null;
  }
}
