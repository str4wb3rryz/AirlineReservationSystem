<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="hr">
<head>
<meta charset="UTF-8">
<title>Pregled statusa poslužitelja</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>

<h1>Pregled i promjena statusa poslužitelja</h1>

<h2>Poslužitelj Kontrola</h2>
<table>
    <thead>
        <tr><th>Dio poslužitelja</th><th>Status</th><th>Akcija</th></tr>
    </thead>
    <tbody>
        <tr>
            <td>Korisnički dio</td>
            <td class="${kontrolaKorisnickiAktivan ? 'status-up' : 'status-down'}">
                ${kontrolaKorisnickiAktivan ? 'AKTIVAN' : 'NEDOSTUPAN'}
            </td>
            <td>&mdash;</td>
        </tr>
        <tr>
            <td>Administracijski dio</td>
            <td class="${kontrolaAdminAktivan ? 'status-up' : 'status-down'}">
                ${kontrolaAdminAktivan ? 'AKTIVAN' : 'NEDOSTUPAN'}
            </td>
            <td>
                <a class="kraj-btn"
                   href="${pageContext.request.contextPath}/mvc/admin/krajKontrolaAdmin"
                   onclick="return confirm('Sigurno zaustaviti administracijski dio poslužitelja Kontrola?');">
                    Kraj rada
                </a>
            </td>
        </tr>
    </tbody>
</table>

<h2>Poslužitelji Rezervacije</h2>
<table>
    <thead>
        <tr><th>ID poslužitelja</th><th>Dio poslužitelja</th><th>Status</th><th>Akcija</th></tr>
    </thead>
    <tbody>
        <c:forEach var="status" items="${statusiRezervacije}">
            <tr>
                <td rowspan="3">${status.id}</td>
                <td>Korisnički dio</td>
                <td class="${status.korisnickiAktivan ? 'status-up' : 'status-down'}">
                    ${status.korisnickiAktivan ? 'AKTIVAN' : 'NEDOSTUPAN'}
                </td>
                <td>&mdash;</td>
            </tr>
            <tr>
                <td>Dio za avio tvrtke</td>
                <td class="${status.avioAktivan ? 'status-up' : 'status-down'}">
                    ${status.avioAktivan ? 'AKTIVAN' : 'NEDOSTUPAN'}
                </td>
                <td>&mdash;</td>
            </tr>
            <tr>
                <td>Administracijski dio</td>
                <td class="${status.adminAktivan ? 'status-up' : 'status-down'}">
                    ${status.adminAktivan ? 'AKTIVAN' : 'NEDOSTUPAN'}
                </td>
                <td>
                    <a class="kraj-btn"
                       href="${pageContext.request.contextPath}/mvc/admin/krajRezervacijeAdmin/${status.id}"
                       onclick="return confirm('Sigurno zaustaviti administracijski dio poslužitelja Rezervacije (ID ${status.id})?');">
                        Kraj rada
                    </a>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty statusiRezervacije}">
            <tr><td colspan="4">Nema konfiguriranih poslužitelja Rezervacije.</td></tr>
        </c:if>
    </tbody>
</table>
        <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/admin/pregledPosluzitelja">Osvježi statuse</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a>
            </li>
       </ul>
</body>
</html>
