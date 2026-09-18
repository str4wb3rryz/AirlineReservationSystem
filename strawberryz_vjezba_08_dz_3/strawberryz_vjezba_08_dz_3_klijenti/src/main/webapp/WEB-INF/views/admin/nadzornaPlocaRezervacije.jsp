<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Nadzorna ploča za rezervacije</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
</head>
<body>
   <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <script type="text/javascript">
  var wsRezervacije;
  var wsAvioTvrtke;
    var MAX_REDAKA = 10;
    
    function wsAdresa(putanja){
    	var adresa = window.location.pathname;
        var dijelovi = adresa.split("/");
        adresa = "ws://" + window.location.hostname + ":"
            + window.location.port + "/" + dijelovi[1]
            + putanja;
	}

    function connectRezervacije() {
    	var adresa = wsAdresa("/ws/rezervacije");
      if ('WebSocket' in window) {
    	  wsRezervacije = new WebSocket(adresa);
      } else if ('MozWebSocket' in window) {
    	  wsRezervacije = new MozWebSocket(adresa);
      } else {
        alert('WebSocket nije podržan od web preglednika.');
        return;
      }
      wsRezervacije.onmessage = onMessageRezervacije;
    }

    function onMessageRezervacije(evt) {
      var dijelovi = evt.data.split(",");
      var tijelo = document.getElementById("tijeloRezervacije");

      var red = document.createElement("tr");
      for (var i = 0; i < dijelovi.length; i++) {
        var celija = document.createElement("td");
        celija.textContent = dijelovi[i];
        red.appendChild(celija);
      }

      tijelo.insertBefore(red, tijelo.firstChild);

      while (tijelo.rows.length > MAX_REDAKA) {
        tijelo.removeChild(tijelo.lastChild);
      }
    }
    function connectAvioTvrtke() {
    	var adresa = wsAdresa("/ws/rezervacije");
        if ('WebSocket' in window) {
        	wsAvioTvrtke = new WebSocket(adresa);
        } else if ('MozWebSocket' in window) {
        	wsAvioTvrtke = new MozWebSocket(adresa);
        } else {
          alert('WebSocket nije podržan od web preglednika.');
          return;
        }
        wsAvioTvrtke.onmessage = onMessageAvioTvrtke;
      }

      function onMessageAvioTvrtke(evt) {
        var dijelovi = evt.data.split(",");
        var tijelo = document.getElementById("tijeloAvioTvrtke");

        var red = document.createElement("tr");
        for (var i = 0; i < dijelovi.length; i++) {
          var celija = document.createElement("td");
          celija.textContent = dijelovi[i];
          red.appendChild(celija);
        }

        tijelo.insertBefore(red, tijelo.firstChild);

        while (tijelo.rows.length > MAX_REDAKA) {
          tijelo.removeChild(tijelo.lastChild);
        }
      }

      window.addEventListener("load", connectRezervacije, false);
      window.addEventListener("load", connectAvioTvrtke, false);
  </script>
  <h2>Nadzorna ploča - Rezervacije</h2>
  <table id="tablicaRezervacije">
    <thead>
      <tr>
        <th>Broj rezervacije</th>
        <th>Oznaka leta</th>
        <th>Id leta</th>
        <th>Ime i prezime putnika</th>
      </tr>
    </thead>
    <tbody id="tijeloRezervacije"></tbody>
  </table>
  <h2>Nadzorna ploča - Aviotvrtke</h2>
  <table id="tablicaAvioTvrtke">
    <thead>
      <tr>
        <th>Broj rezervacije</th>
        <th>Oznaka leta</th>
        <th>Id leta</th>
        <th>Ime i prezime putnika</th>
        <th>Ime i prezime zastupnika</th>
      </tr>
    </thead>
    <tbody id="tijeloAvioTvrtke"></tbody>
  </table>
  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>