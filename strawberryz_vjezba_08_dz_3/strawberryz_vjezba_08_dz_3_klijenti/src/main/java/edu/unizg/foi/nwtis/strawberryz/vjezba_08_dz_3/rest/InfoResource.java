package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.rest;

import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.ws.WebSocketRezervacije;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("info")
public class InfoResource {

  @Path("rezervacije")
  @GET
  public Response posaljiPorukuRezervacije(@QueryParam("poruka") String poruka) {
    WebSocketRezervacije.send(poruka);
    return Response.ok().entity("Poruka poslana!").build();
  }
}
