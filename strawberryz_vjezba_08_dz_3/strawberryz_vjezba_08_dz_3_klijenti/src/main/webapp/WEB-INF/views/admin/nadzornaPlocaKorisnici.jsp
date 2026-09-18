<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="edu.unizg.foi.nwtis.Korisnik" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JPA MVC - Nadzorna ploča korisnici</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
    </head>
    <body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
        <h1>JPA MVC - Nadzorna ploča korisnici</h1>
        <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/admin/obrisiSveKorisnike">Obriši sve korisnike iz liste</a>
            </li>
        </ul>
        <br/>
        <table>
            <tr>
                <th>R.br.</th>
                <th>Korisnik</th>
                <th>Lozinka</th>
                <th>Prezime</th>
                <th>Ime</th>
                <th>Email</th>
                <th>Akcija</th>
            </tr>
            <%
            int i = 0;
            List<Korisnik> korisnici = (List<Korisnik>) request.getAttribute("korisniciPoruke");
            for (Korisnik k : korisnici) {
            %>
            <tr>
                <td class="desno"><%= i %></td>
                <td><%= k.korisnik() %></td>
                <td><%= k.lozinka() %></td>
                <td><%= k.prezime() %></td>
                <td><%= k.ime() %></td>
                <td><%= k.email() %></td>
                <td>
                    <a href="${pageContext.servletContext.contextPath}/mvc/admin/obrisiKorisnikaIzListe/<%= i %>">Obriši</a>
                </td>
            </tr>
            <%
                i++;  // inkrement NAKON prikaza, jer se indeks koristi za brisanje
            }
            %>
        </table>
    </body>
</html>