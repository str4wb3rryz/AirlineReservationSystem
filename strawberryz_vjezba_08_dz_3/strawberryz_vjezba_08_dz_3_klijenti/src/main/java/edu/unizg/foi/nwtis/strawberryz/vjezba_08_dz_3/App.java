package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3;

import java.util.Map;

import jakarta.mvc.security.Csrf;
import jakarta.mvc.security.Csrf.CsrfOptions;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/mvc")
public class App extends Application {

    @Override
    public Map<String, Object> getProperties() {
        return Map.of(Csrf.CSRF_PROTECTION, CsrfOptions.IMPLICIT);
    }
}
