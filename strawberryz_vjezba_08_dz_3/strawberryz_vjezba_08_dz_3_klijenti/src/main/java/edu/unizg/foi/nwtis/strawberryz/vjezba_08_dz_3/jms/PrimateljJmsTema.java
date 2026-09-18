package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jms;

import edu.unizg.foi.nwtis.Korisnik;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;

@MessageDriven(mappedName = "jms/NWTiSTopic",
    activationConfig = {
        @ActivationConfigProperty(propertyName = "acknowledgeMode",
            propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "jakarta.jms.Topic")})
public class PrimateljJmsTema implements MessageListener {

  @Inject
  SpremnikJmsPoruka spremnikJmsPoruka;

  public PrimateljJmsTema() {}

  public void onMessage(Message message) {
	    try {
	        if (message instanceof TextMessage msg) {
	            System.out.println("Stigla TextMessage poruka u temu: "
	                + message.getJMSMessageID() + " "
	                + new java.util.Date(message.getJMSTimestamp()) + " "
	                + msg.getText());

	        } else if (message instanceof ObjectMessage objMsg) {
	            Object objekt = objMsg.getObject();
	            System.out.println("Stigla ObjectMessage poruka u temu: "
	                + message.getJMSMessageID() + " "
	                + new java.util.Date(message.getJMSTimestamp()) + " "
	                + objekt);

	            if (objekt instanceof Korisnik korisnik) {
	                this.spremnikJmsPoruka.dodajPorukuTeme(korisnik);
	            } else {
	                System.out.println("Nepoznat tip objekta: " + objekt);
	            }
	        }
	    } catch (JMSException ex) {
	        ex.printStackTrace();
	    }
	}
}
