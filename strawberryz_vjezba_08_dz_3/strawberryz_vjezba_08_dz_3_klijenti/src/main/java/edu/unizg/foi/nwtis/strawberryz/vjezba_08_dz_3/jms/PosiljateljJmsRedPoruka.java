package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jms;

import java.io.Serializable;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;

@Stateless
public class PosiljateljJmsRedPoruka implements Serializable {

  private static final long serialVersionUID = -3326921532069088262L;

  @Resource(mappedName = "jms/NWTiSConnectionFactory")
  private ConnectionFactory connectionFactory;
  @Resource(mappedName = "jms/NWTiSQueue")
  private Queue queue;

  public boolean novaPoruka(String poruka) {
    boolean status = true;

    try (JMSContext context = connectionFactory.createContext()) {
      TextMessage message = context.createTextMessage();

      message.setText(poruka);
      context.createProducer().send(queue, message);
    } catch (JMSException _) {
      status = false;
    }
    return status;
  }

}
