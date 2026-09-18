package edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.ws;

import java.io.IOException;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/rezervacije")
public class WebSocketRezervacije {

  static Queue<Session> queue = new ConcurrentLinkedQueue<>();

  private static final int MAX_POVIJEST = 10;
  static final Deque<String> povijest = new ConcurrentLinkedDeque<>();

  public static void send(String poruka) {
    zapamtiPoruku(poruka);
    for (Session session : queue) {
      if (session.isOpen()) {
        try {
          System.out.println("Šaljem poruku: " + poruka);
          session.getBasicRemote().sendText(poruka);
        } catch (IOException ex) {
          System.out.println(ex.getMessage());
        }
      }
    }
  }

  private static void zapamtiPoruku(String poruka) {
    povijest.addLast(poruka);
    while (povijest.size() > MAX_POVIJEST) {
      povijest.pollFirst();
    }
  }

  @OnOpen
  public void openConnection(Session session, EndpointConfig conf) {
    queue.add(session);
    System.out.println("Otvorena veza.");
    for (String poruka : povijest) {
      try {
        session.getBasicRemote().sendText(poruka);
      } catch (IOException ex) {
        System.out.println(ex.getMessage());
      }
    }
  }

  @OnClose
  public void closedConnection(Session session, CloseReason reason) {
    queue.remove(session);
    System.out.println("Zatvorena veza.");
  }

  @OnMessage
  public void Message(Session session, String poruka) {
    System.out.println("Primljena poruka: " + poruka);
    WebSocketRezervacije.send(poruka);
  }

  @OnError
  public void error(Session session, Throwable t) {
    queue.remove(session);
    System.out.println("Zatvorena veza zbog pogreške.");
  }
}
