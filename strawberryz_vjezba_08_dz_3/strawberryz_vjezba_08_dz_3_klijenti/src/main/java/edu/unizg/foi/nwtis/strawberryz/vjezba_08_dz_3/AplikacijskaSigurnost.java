package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;

@ApplicationScoped
@FormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(loginPage = "/mvc/prijava/prijavaKorisnika",
        errorPage = "/mvc/prijava/prijavaKorisnikaPogreska"))
@DatabaseIdentityStoreDefinition(dataSourceLookup = "java:app/jdbc/nwtis_hsqldb",
    callerQuery = "select lozinka from korisnici where korisnik = ?",
    groupsQuery = "select grupa from uloge where korisnik = ?",
    hashAlgorithm = NoPasswordHash.class)
@DeclareRoles({"admin", "nwtis"})
public class AplikacijskaSigurnost {
}
