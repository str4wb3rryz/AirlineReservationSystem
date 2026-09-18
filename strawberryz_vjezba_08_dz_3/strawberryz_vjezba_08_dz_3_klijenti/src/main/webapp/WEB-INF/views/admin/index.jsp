<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>NWTiS - Administracijski dio</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
    </head>
    <body>
        <h1>Administracijski dio</h1>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
        <ul>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/admin/pregledPosluzitelja">Pregled i promjena statusa poslužitelja</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/admin/nadzornaPlocaRezervacije">Nadzorna ploča za rezervacije</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/admin/nadzornaPlocaKorisnici">Nadzorna ploča za korisnike</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/admin/nadzornaPlocaRacuni">Nadzorna ploča za račune</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/admin/pregledZatvorenihRacuna">Pregled zatvorenih računa</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
        </ul>
    </body>
</html>