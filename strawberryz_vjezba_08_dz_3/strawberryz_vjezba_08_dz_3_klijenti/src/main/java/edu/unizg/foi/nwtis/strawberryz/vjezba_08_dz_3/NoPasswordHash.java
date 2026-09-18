package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3;

import java.util.Map;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

public class NoPasswordHash implements Pbkdf2PasswordHash {

  @Override
  public String generate(char[] password) {
    return new String(password);
  }

  @Override
  public boolean verify(char[] password, String hashedPassword) {
    if (password == null || hashedPassword == null) {
      return false;
    }

    return new String(password).trim().equals(hashedPassword.trim());
  }

  @Override
  public void initialize(Map<String, String> parameters) {}

}
