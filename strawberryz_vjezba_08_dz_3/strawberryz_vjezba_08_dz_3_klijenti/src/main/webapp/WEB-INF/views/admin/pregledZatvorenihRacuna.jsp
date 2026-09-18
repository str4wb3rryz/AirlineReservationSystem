<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Pregled zatvorenih računa</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h1>Pregled zatvorenih računa</h1>

  <form method="get" action="${pageContext.servletContext.contextPath}/mvc/admin/pregledZatvorenihRacuna">
    <fieldset>
      <legend>Pretraživanje / filtriranje</legend>
      Ime (osoba računa): <input type="text" name="imeRacuna" value="${imeRacuna}">
      Prezime (osoba računa): <input type="text" name="prezimeRacuna" value="${prezimeRacuna}"><br/>
      Ime (osoba stavke): <input type="text" name="imeStavke" value="${imeStavke}">
      Prezime (osoba stavke): <input type="text" name="prezimeStavke" value="${prezimeStavke}">
      <button type="submit">Pretraži</button>
    </fieldset>
  </form>

  <table>
    <thead>
      <tr>
        <th>Id</th>
        <th>Vrijeme</th>
        <th>Korisnik</th>
        <th>Broj stavki</th>
        <th>Iznos</th>
        <th>Stavke</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${racuni}">
        <tr>
          <td class="desno">${r.id}</td>
          <td>${r.vrijeme}</td>
          <td>${r.korisnik}</td>
          <td class="desno">${r.brojStavki}</td>
          <td class="desno"><fmt:formatNumber value="${r.iznos}" minFractionDigits="2" maxFractionDigits="2"/></td>
          <td>
            <a href="${pageContext.servletContext.contextPath}/mvc/admin/pregledStavkiZatvorenogRacuna/${r.id}">Pregled stavki</a>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/admin/pocetak">Administracijski dio</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
