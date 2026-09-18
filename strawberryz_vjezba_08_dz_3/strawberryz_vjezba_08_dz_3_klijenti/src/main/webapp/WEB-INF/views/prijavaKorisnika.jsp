<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>REST MVC - Prijava korisnika</title>
     
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
    </head>
    <body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
        <h1>Prijava korisnika</h1>
       <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a>
            </li>
           
            <li><p>Podaci prijave:</p>          
                <form method="post" action="${pageContext.servletContext.contextPath}/j_security_check">
                    <table>
                        <tr>
                            <td>Korisnik: </td>
                            <td>
                                <input type="text" name="j_username"/>
                            </td>
                        </tr>
                        <tr>
                            <td>Lozinka: </td>
                            <td><input type="password" name="j_password"/>
                            </td>
                        </tr>
                       
                        <tr>
                            <td>&nbsp;</td>
                            <td><input type="submit" value=" Prijavi korisnika "></td>
                        </tr>                        
                    </table>
                </form>
            </li>                     
        </ul>   
    </body>
</html>
