# Evidence prezenčních výpůjček

## K čemu aplikace slouží
Evidence prezenčních výpůjček knih za pomoci mobilních zařízení a technologie NFC. Po načtení kódu pomocí mobilního zařízení je načtena informace o umístění jednotky z databáze na serveru a následně je možné odeslat informaci o výpůjčce do systému Aleph k dalšímu zpracování statistik.

## Části
- spouštěcí script circ-logger-30
- serverová část circ-logger-z30.pl
- mobilní aplikace ([google play](https://play.google.com/store/apps/details?id=cz.techlib.evidencevypujcek)) 

## Instalace serverové části

V knihovním systému Aleph se script na který odkazuje umístí do zabezpečeného adresáře: `/exlibris/aleph/u22_1/alephe/apache/cgi-c30`

perlový script pak následnì do adresáře:
`/exlibris/aleph/u22_1/alephe/scripts/circ-logger-30.pl`

## Zabezpečení
```
AuthType Basic
AuthName Circ30
AuthUserFile /exlibris/aleph/u22_1/alephe/apache/cgi-c30/.htpasswd
Require valid-user
```
Vytvoření hesel kupříkladu: http://htaccess.all4all.cz/#a_basic

