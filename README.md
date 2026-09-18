# Rezervacija karata za letove aviona

Projekt za kolegij Napredne Web tehnologije i servisi

## O projektu

Rezervacija karata za letove aviona na bazi više poslužitelja na mrežnoj utičnici s omotačem
na bazi RESTful web servisa primjenom Jakarta EE Core Profile/MicroProfile i korisničkim sučeljem na bazi
Jakarta MVC i Jakarta WebSocket uz sigurnosnih mehanizama te primjenom Jakarta JPA za rad s bazom podataka,
JMS za slanje poruka 

## Struktura zadaća

| Zadaća | Vježba | Fokus |
|--------|--------|-------|
| DZ1 | Vježba 4 | Rezervacija karata na bazi više poslužitelja putem mrežne utičnice (socket serveri: `PosluziteljKontrola`, `PosluziteljRezervacije`) |
| DZ2 | Vježba 7 | Omotač na bazi RESTful web servisa primjenom Jakarta EE Core Profile i MicroProfile (mikroservisi), uz Docker kontejnerizaciju |
| DZ3 | Vježba 8 | Korisničko sučelje na bazi Jakarta MVC i Jakarta WebSocket, sigurnosni mehanizmi, Jakarta JPA za bazu podataka, JMS za razmjenu poruka |

## Tehnologije

- **Socket serveri** (Java, mrežne utičnice) – osnovna arhitektura iz DZ1
- **RESTful web servisi** – Jakarta EE Core Profile (Grizzly) i MicroProfile (Payara Micro)
- **Jakarta MVC** – kontroleri za javni i privatni (administracijski) dio sučelja
- **Jakarta WebSocket** – komunikacija u stvarnom vremenu prema korisničkom sučelju (npr. potvrde rezervacija)
- **Jakarta JPA** (Criteria API) – rad s bazom podataka
- **JMS** – slanje poruka (teme i redovi poruka) između komponenti sustava
- **Docker / Docker Compose** – svaki poslužitelj/servis u vlastitom kontejneru, povezan preko zajedničke mreže i sveska

## Arhitektura

Sustav je slojevito građen: mrežni poslužitelji za kontrolu i rezervacije (DZ1) omotani su RESTful servisima (DZ2), a zatim prošireni korisničkim sučeljem s MVC kontrolerima, WebSocket obavijestima i JMS porukama uz sigurnosnu kontrolu pristupa (DZ3). Svi servisi rade u odvojenim Docker kontejnerima prema definiranoj instalacijskoj arhitekturi.

## Napomene

Rad uključuje strogo definirana pravila za strukturu Maven modula, imenovanje paketa/klasa na hrvatskom jeziku, jedinično testiranje (JUnit) te predaju putem Eclipse/Maven projekta u Moodle.

