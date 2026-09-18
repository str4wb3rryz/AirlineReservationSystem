<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Pregled putnika</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/aviotvrtke.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h2>Pregled putnika</h2>
  <c:if test="${putnikOdabran}">
    <p>Odabrani putnik: <strong>${odabraniPutnik}</strong></p>
  </c:if>
  <c:if test="${mojaTvrtka == null}">
    <p><em>Niste zastupnik niti jedne avio tvrtke - dodavanje/brisanje zastupnika nije moguće.</em></p>
  </c:if>

  <table>
    <thead>
      <tr>
        <th>Korisnik</th>
        <th>Ime</th>
        <th>Prezime</th>
        <th>Email</th>
        <th>Odabir putnika</th>
        <th>Letovi putnika</th>
        <th>Zastupnik</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="p" items="${putnici}">
        <tr>
          <td>${p.korisnik}</td>
          <td>${p.ime}</td>
          <td>${p.prezime}</td>
          <td>${p.email}</td>
          <td>
            <form class="inline" method="post" action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/odabirPutnika">
              <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
              <input type="hidden" name="putnikKorisnickoIme" value="${p.korisnik}">
              <button type="submit">Postavi kao putnika</button>
            </form>
          </td>
          <td>
            <form class="inline" method="get" action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledLetovaPutnika">
              <input type="hidden" name="putnik" value="${p.korisnik}">
              Od: <input type="date" lang="hr" name="odDatuma" required>
              Do: <input type="date" lang="hr" name="doDatuma" required>
              <button type="submit">Letovi</button>
            </form>
          </td>
          <td>
            <c:choose>
              <c:when test="${mojaTvrtka == null}">-</c:when>
              <c:when test="${p.nasZastupnik}">
                <form class="inline" method="post" action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/obrisiZastupnika">
                  <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
                  <input type="hidden" name="putnik" value="${p.korisnik}">
                  <input type="hidden" name="odStranice" value="${odStranice}">
                  <input type="hidden" name="stranica" value="${stranica}">
                  <button type="submit">Obriši kao zastupnika</button>
                </form>
              </c:when>
              <c:when test="${not p.zastupnikBiloKoje}">
                <form class="inline" method="post" action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/dodajZastupnika">
                  <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
                  <input type="hidden" name="putnik" value="${p.korisnik}">
                  <input type="hidden" name="odStranice" value="${odStranice}">
                  <input type="hidden" name="stranica" value="${stranica}">
                  <button type="submit">Dodaj kao zastupnika</button>
                </form>
              </c:when>
              <c:otherwise><span title="Zastupnik druge avio tvrtke">zastupnik druge tvrtke</span></c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <p>
    <c:if test="${imaPrethodna}">
      <a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledPutnika?odStranice=${odStranice - 1}&stranica=${stranica}">&laquo; Prethodna</a>
    </c:if>
    Stranica ${odStranice}
    <c:if test="${imaSljedeca}">
      <a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledPutnika?odStranice=${odStranice + 1}&stranica=${stranica}">Sljedeća &raquo;</a>
    </c:if>
  </p>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pocetak">Avio tvrtke</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
