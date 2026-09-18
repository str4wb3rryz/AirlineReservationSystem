package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jms;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

@MessageDriven(mappedName = "jms/NWTiSQueue",
    activationConfig = {
        @ActivationConfigProperty(propertyName = "acknowledgeMode",
            propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "jakarta.jms.Queue")})
public class PrimateljJmsRedPoruka implements MessageListener {
  @Inject
  SpremnikJmsPoruka spremnikJmsPoruka;

  public PrimateljJmsRedPoruka() {}

  public void onMessage(Message message) {
    TextMessage msg = null;

    if (message instanceof TextMessage) {
      try {
        msg = (TextMessage) message;
        System.out.println("Stigla poruka u red čekanja:" + message.getJMSMessageID() + " "
            + new java.util.Date(message.getJMSTimestamp()) + " " + msg.getText());
        this.spremnikJmsPoruka.dodajRedPoruka(msg.getText());

      } catch (JMSException ex) {
        ex.printStackTrace();
      }

    }
  }
}
