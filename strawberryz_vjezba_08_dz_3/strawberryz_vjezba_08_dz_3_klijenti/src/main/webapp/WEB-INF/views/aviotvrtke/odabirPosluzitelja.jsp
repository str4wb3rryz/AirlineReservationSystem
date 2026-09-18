<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<html>
<head>
  <title>Odabir poslužitelja</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/aviotvrtke.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>

<h2>Odabir poslužitelja rezervacija</h2>

<c:if test="${not empty trenutniPosluzitelj}">
  <p>Trenutno odabrani poslužitelj: <strong>${trenutniPosluzitelj}</strong></p>
</c:if>

<c:choose>
  <c:when test="${empty listaPosluzitelja}">
    <p>Nema dostupnih poslužitelja.</p>
  </c:when>
  <c:otherwise>
    <table border="1">
      <thead>
        <tr>
          <th>ID</th>
          <th>Adresa</th>
          <th>Status</th>
          <th>Odabir</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="p" items="${listaPosluzitelja}">
          <tr>
            <td>${p.id}</td>
            <td>${p.adresa}</td>
            <td>
              <c:choose>
                <c:when test="${p.aktivan}">Aktivan</c:when>
                <c:otherwise>Nije dostupan</c:otherwise>
              </c:choose>
            </td>
            <td>
              <c:if test="${p.aktivan}">
                <form method="post" action="${pageContext.request.contextPath}/mvc/aviotvrtke/odabirPosluzitelja">
                  <input type="hidden" name="posluzitelj" value="${p.id}"/>
                  <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
                  <button type="submit">Odaberi</button>
                </form>
              </c:if>
              <c:if test="${not p.aktivan}">
                <span>Nedostupan</span>
              </c:if>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </c:otherwise>
</c:choose>

</body>
</html>
