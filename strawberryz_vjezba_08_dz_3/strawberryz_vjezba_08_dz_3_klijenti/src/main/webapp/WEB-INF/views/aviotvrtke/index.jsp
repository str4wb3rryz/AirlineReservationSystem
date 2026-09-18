<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>NWTiS - Avio tvrtke</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/aviotvrtke.css">
    </head>
    <body>
        <h1>Avio tvrtke</h1>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
        <ul>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/odabirPosluzitelja">Odabir poslužitelja za rezervacije</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledRacuna">Pregled računa zastupnika</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledPutnika">Pregled putnika</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledLetova">Pregled letova avio tvrtke</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledRezervacija">Pregled nepotvrđenih rezervacija</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledLetovaInicijalizacija">Pregled letova (inicijalizacija/stanje)</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zatvaranjeSjedala">Zatvaranje sjedala</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/inicijalizacijaLetova">Inicijalizacija letova</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
        </ul>
    </body>
</html>