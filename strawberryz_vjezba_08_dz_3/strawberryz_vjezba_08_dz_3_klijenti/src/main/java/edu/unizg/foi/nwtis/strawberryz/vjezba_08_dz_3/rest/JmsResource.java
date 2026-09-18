package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.rest;

import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jms.PosiljateljJmsRedPoruka;
import edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jms.PosiljateljJmsTema;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("jms")
public class JmsResource {

  @Inject
  PosiljateljJmsRedPoruka posiljateljJmsRedPoruka;

  @Inject
  PosiljateljJmsTema posiljateljJmsTema;

  @Path("redporuka")
  @GET
  public Response redporuka(@QueryParam("poruka") String poruka) {
    posiljateljJmsRedPoruka.novaPoruka(poruka);
    return Response.ok().entity("Poruka za JMS red poruka je poslana!").build();
  }

  @Path("tema")
  @GET
  public Response tema(@QueryParam("poruka") String poruka) {
    posiljateljJmsTema.novaPoruka(poruka);
    return Response.ok().entity("Poruka za JMS temu je poslana!").build();
  }
}
