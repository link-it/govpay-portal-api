<p align="center">
<img src="https://www.link.it/wp-content/uploads/2025/01/logo-govpay.svg" alt="GovPay Logo" width="200"/>
</p>

# GovPay - Porta di accesso al sistema pagoPA - Portal API

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=link-it_govpay-portal-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=link-it_govpay-portal-api)
[![Docker Hub](https://img.shields.io/docker/v/linkitaly/govpay-portal-api?label=Docker%20Hub&logo=docker)](https://hub.docker.com/r/linkitaly/govpay-portal-api)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://raw.githubusercontent.com/link-it/govpay-portal-api/main/LICENSE)
[![Spring Boot](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Flink-it%2Fgovpay-bom%2Fmain%2Fpom.xml&query=%2F%2F*%5Blocal-name()%3D'spring-boot.version'%5D&label=Spring%20Boot&color=brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)

API REST del portale dei pagamenti di GovPay: è il backend del frontend [govpay-portal](https://github.com/link-it/govpay-portal), con cui il cittadino consulta le proprie pendenze, esegue pagamenti spontanei e scarica avvisi e ricevute.

## Sommario

Il servizio espone al portale le anagrafiche degli Enti Creditori e le pendenze presenti nel database di GovPay. Le letture vanno direttamente sul database; la creazione delle pendenze passa dall'API Pendenze di GovPay e i PDF di avvisi e ricevute sono prodotti da [govpay-stampe-api](https://github.com/link-it/govpay-stampe-api).

Operazioni esposte (specifica in [src/main/resources/openapi.yaml](src/main/resources/openapi.yaml)):

| Operazione | Metodo e path | Accesso | Descrizione |
|------------|---------------|---------|-------------|
| `login` | `GET /login` | autenticato | Crea la sessione e restituisce il profilo dell'utente |
| `loginWithRedirect` | `GET /login/{urlID}` | autenticato | Crea la sessione e redirige alla URL configurata per `urlID` |
| `getProfilo` | `GET /profilo` | autenticato | Profilo dell'utente in sessione |
| `logout` | `GET /logout` | autenticato | Chiude la sessione |
| `logoutWithRedirect` | `GET /logout/{urlID}` | autenticato | Chiude la sessione e redirige alla URL configurata per `urlID` |
| `findDomini` | `GET /domini` | pubblico | Enti Creditori |
| `getDominio` | `GET /domini/{idDominio}` | pubblico | Dettaglio di un Ente Creditore |
| `getLogo` | `GET /domini/{idDominio}/logo` | pubblico | Logo dell'Ente Creditore |
| `findTipiPendenza` | `GET /domini/{idDominio}/tipiPendenza` | pubblico | Tipi pendenza dell'Ente pagabili dal portale |
| `getTipoPendenza` | `GET /domini/{idDominio}/tipiPendenza/{idTipoPendenza}` | pubblico | Dettaglio del tipo pendenza, con la definizione del form di pagamento spontaneo |
| `findPendenze` | `GET /pendenze/{idDominio}` | autenticato | Pendenze dell'utente (debitore = codice fiscale dell'utente) |
| `getPendenza` | `GET /pendenze/{idDominio}/{numeroAvviso}` | autenticato | Dettaglio di una pendenza |
| `addPendenza` | `POST /pendenze/{idDominio}/{idTipoPendenza}` | pubblico, con hardening | Crea una pendenza di pagamento spontaneo a partire dai dati del form |
| `getAvviso` | `GET /pendenze/{idDominio}/{numeroAvviso}/avviso` | pubblico, con hardening | Avviso di pagamento, in JSON o PDF (`Accept: application/pdf`) |
| `getRt` | `GET /pendenze/{idDominio}/{numeroAvviso}/ricevuta` | pubblico | Ricevuta di pagamento, in JSON o PDF |

La documentazione OpenAPI generata è servita su `/api-docs` e la Swagger UI su `/swagger-ui.html`.

### Autenticazione

Il servizio non gestisce direttamente SPID o CIE: l'autenticazione è demandata a un reverse proxy (o a un Service Provider SPID) che, dopo il login, inoltra i dati dell'utente in header HTTP. Una richiesta con l'header del codice fiscale valorizzato (default `X-SPID-FISCALNUMBER`, prefisso `TINIT-` rimosso) è considerata autenticata; le altre sono anonime. Gli header vanno quindi accettati **solo dal proxy**: il servizio non deve essere raggiungibile direttamente dai client.

- **Sessione**: `GET /login` crea la sessione (cookie `JSESSIONID`), al massimo due sessioni contemporanee per utente; `GET /logout` la invalida.
- **CSRF**: il servizio restituisce il token nel cookie `XSRF-TOKEN`; le richieste `POST` devono rimandarlo nell'header `X-XSRF-TOKEN`.
- **Hardening degli accessi anonimi**: per `addPendenza` e `getAvviso` richiesti senza autenticazione il servizio verifica il reCAPTCHA di Google (token nel parametro o nell'header indicato dalla configurazione, es. `gRecaptchaResponse`), se l'hardening è abilitato nella configurazione di GovPay (sezione `hardening` della tabella `configurazione`, gestita dalla console). Per `getAvviso` basta in alternativa il parametro `UUID` della pendenza, verificato sul database.
- Le richieste che non corrispondono a nessuna regola sono rifiutate con `403`.

### Pagamento spontaneo

1. Il portale legge con `getTipoPendenza` la definizione del form configurata in GovPay per il tipo pendenza dell'Ente (tabella `tipi_vers_domini`).
2. L'utente compila il form e il portale lo invia con `addPendenza`.
3. Se il tipo pendenza ha una trasformazione, il servizio applica il template FreeMarker configurato al JSON del form, con a disposizione dati dell'utente, header, parametri, Ente e data corrente, e ottiene la pendenza.
4. La pendenza è creata con l'API Pendenze di GovPay (`PUT /pendenze/{idA2A}/{idPendenza}`), con le credenziali del servizio.
5. Il servizio restituisce la pendenza creata, con il numero avviso con cui il portale scarica l'avviso.

## Integrazione con GovPay

| Componente | Quando serve | Obbligatorio | Ruolo |
|------------|--------------|--------------|-------|
| Database di GovPay | runtime | sì | Legge `domini` (anche il logo), `uo`, `stazioni`, `iban_accredito`, `tipi_versamento`, `tipi_vers_domini`, `tributi`, `tipi_tributo`, `applicazioni`, `versamenti`, `singoli_versamenti`, `rpt` (ricevute), `connettori` e `configurazione`. Non scrive: le pendenze sono create con l'API Pendenze. Lo schema è creato dagli script di installazione di GovPay |
| [govpay-bom](https://github.com/link-it/govpay-bom) | build | sì | Parent Maven: versioni delle dipendenze e dei plugin |
| [govpay-common](https://github.com/link-it/govpay-common) | build | sì | Libreria comune: connettori, configurazione di GovPay (hardening, giornale degli eventi), client GDE, transaction id, livelli di log da database, metriche |
| API Pendenze di GovPay | runtime | sì, per i pagamenti spontanei | Crea le pendenze (`govpay.pendenze.*`, specifica in [govpay-api-pendenze-v2.yaml](src/main/resources/govpay-api-pendenze-v2.yaml)). Le credenziali sono quelle di un'applicazione GovPay autorizzata sugli Enti e sui tipi pendenza del portale |
| [govpay-stampe-api](https://github.com/link-it/govpay-stampe-api) | runtime | sì, per i PDF | Produce i PDF di avvisi e ricevute (`govpay.stampe.base-url`, specifica in [govpay-stampe.yaml](src/main/resources/govpay-stampe.yaml)) |
| [govpay-gde-api](https://github.com/link-it/govpay-gde-api) | runtime | no | Riceve gli eventi delle operazioni del portale (`POST /eventi`), se il connettore `govpay_gde_api` è abilitato in GovPay. Un GDE non raggiungibile non blocca il servizio |
| [govpay-portal](https://github.com/link-it/govpay-portal) | runtime | no | Frontend del portale dei pagamenti, client di queste API |
| Reverse proxy con autenticazione SPID/CIE | runtime | sì, per le funzioni riservate | Autentica l'utente e inoltra gli header `X-SPID-*`; senza, sono disponibili solo le operazioni pubbliche |

Il servizio non chiama direttamente i servizi pagoPA: legge dal database le ricevute (RT) che GovPay ha ricevuto da pagoPA e le converte nel formato di stampa.

Le modifiche ai livelli di log fatte dalla console (riga `log_level` della tabella `configurazione`) raggiungono il servizio entro l'intervallo di refresh, senza riavvio.

## Compilazione

Requisiti: JDK 21 e Maven 3.8.1 o successivo.

```bash
mvn clean install -P jar     # fat jar, per esecuzione standalone e container (profilo di default)
mvn clean install -P war     # war, per il deploy su application server
```

Il profilo sceglie il packaging; il risultato è in `target/govpay-portal-api.jar` o `target/govpay-portal-api.war`. La build genera i client delle API Pendenze e Stampe dalle specifiche OpenAPI, esegue i test e l'analisi OWASP Dependency-Check delle dipendenze (esecuzione `check-owasp` di `govpay-bom`, con i falsi positivi del progetto in `src/main/resources/owasp/falsePositives`); per escludere l'analisi OWASP, per esempio in sviluppo:

```bash
mvn clean install -P jar -Dowasp.phase=none
```

## Configurazione

Le proprietà si impostano in `application.properties`, da riga di comando (`-D<proprietà>=<valore>`) o con le variabili d'ambiente equivalenti di Spring Boot (es. `govpay.pendenze.base-url` → `GOVPAY_PENDENZE_BASE_URL`). Con l'immagine Docker si usano anche le variabili descritte in [Esecuzione con Docker](#esecuzione-con-docker).

### Server e API

| Proprietà | Default | Descrizione |
|-----------|---------|-------------|
| `server.port` | `8080` | Porta HTTP del servizio |
| `portal.time-zone` | `Europe/Rome` | Fuso orario delle date nelle risposte e nelle chiamate all'API Pendenze |
| `spring.mvc.date-format` | `yyyy-MM-dd` | Formato delle date nei parametri |
| `spring.mvc.datetime-format` | `yyyy-MM-dd'T'HH:mm:ssZZZ` | Formato di data e ora nei parametri |
| `spring.mvc.throw-exception-if-no-handler-found` | `true` | Risponde con un errore applicativo sui path inesistenti |
| `spring.jackson.default-property-inclusion` | `non_null` | Esclude i campi nulli dalle risposte JSON |
| `server.error.whitelabel.enabled` | `true` | Pagina di errore di default di Spring Boot |
| `spring.main.banner-mode` | `off` | Banner di Spring Boot all'avvio |
| `springdoc.api-docs.path` | `/api-docs` | Path della specifica OpenAPI generata |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Path della Swagger UI |
| `springdoc.swagger-ui.enabled` | `true` | Abilita la Swagger UI |
| `springdoc.swagger-ui.operations-sorter` | `method` | Ordinamento delle operazioni nella Swagger UI |
| `springdoc.swagger-ui.tags-sorter` | `alpha` | Ordinamento dei tag nella Swagger UI |

### Autenticazione e sessione

| Proprietà | Default | Descrizione |
|-----------|---------|-------------|
| `govpay.security.spid-headers.fiscal-number` | `X-SPID-FISCALNUMBER` | Header con il codice fiscale dell'utente: se valorizzato la richiesta è autenticata |
| `govpay.security.spid-headers.name` | `X-SPID-NAME` | Header con il nome |
| `govpay.security.spid-headers.family-name` | `X-SPID-FAMILYNAME` | Header con il cognome |
| `govpay.security.spid-headers.email` | `X-SPID-EMAIL` | Header con l'email |
| `govpay.security.spid-headers.mobile-phone` | `X-SPID-MOBILEPHONE` | Header con il cellulare |
| `govpay.security.spid-headers.address` | `X-SPID-ADDRESS` | Header con l'indirizzo |
| `govpay.security.login-redirect-urls.<urlID>` | - | URL a cui `GET /login/{urlID}` redirige dopo aver creato la sessione |
| `govpay.security.logout-redirect-urls.<urlID>` | - | URL a cui `GET /logout/{urlID}` redirige dopo il logout |

Le URL di redirect sono una whitelist: un `urlID` non configurato restituisce `404`. I parametri della richiesta originale sono inoltrati alla URL di redirect, per esempio con `govpay.security.login-redirect-urls.portale=https://portale.ente.it/area-riservata` la richiesta `GET /login/portale?lang=it` redirige a `https://portale.ente.it/area-riservata?lang=it`.

Il reCAPTCHA non ha proprietà proprie: abilitazione, chiavi, URL di verifica, nome del parametro, timeout e soglia sono nella sezione `hardening` della configurazione di GovPay.

### API Pendenze e Stampe

| Proprietà | Default | Descrizione |
|-----------|---------|-------------|
| `govpay.pendenze.base-url` | - | URL base dell'API Pendenze v2 di GovPay, es. `http://govpay:8080/govpay/backend/api/pendenze/rs/basic/v2` |
| `govpay.pendenze.username` | - | Utente HTTP Basic dell'applicazione GovPay usata per creare le pendenze |
| `govpay.pendenze.password` | - | Password dell'applicazione |
| `govpay.stampe.base-url` | - | URL base di govpay-stampe-api, es. `http://govpay-stampe-api:8080/api/v1` |
| `govpay.stampe.logo.pagopa` | logo pagoPA (data URI PNG) | Logo pagoPA stampato sulle ricevute, come data URI `data:image/png;base64,...` |

### Giornale degli eventi e connettori

URL e autenticazione del GDE non sono proprietà del servizio: sono nel connettore `govpay_gde_api` di GovPay (tabella `connettori`).

| Proprietà | Default | Descrizione |
|-----------|---------|-------------|
| `govpay.portal.cluster-id` | `portal-api` (variabile `CLUSTER_ID`) | Identificativo del nodo, registrato negli eventi del GDE |
| `govpay.client.cache.enabled` | `false` | Carica i connettori all'avvio e li tiene in cache, invece di leggerli dal database a ogni uso |
| `govpay.client.async.core-pool-size` | `10` | Thread minimi per l'invio asincrono degli eventi al GDE |
| `govpay.client.async.max-pool-size` | `50` | Thread massimi per l'invio asincrono degli eventi al GDE |
| `govpay.client.async.queue-capacity` | `100` | Eventi in coda in attesa di invio |
| `govpay.client.async.thread-name-prefix` | `async-http-` | Prefisso dei thread di invio |

### Database

| Proprietà | Default | Descrizione |
|-----------|---------|-------------|
| `spring.datasource.url` | - | URL JDBC del database di GovPay (obbligatoria, in alternativa a `spring.datasource.jndi-name`) |
| `spring.datasource.jndi-name` | - | Nome JNDI del datasource, per il deploy del war su application server |
| `spring.datasource.username` | - | Utente del database |
| `spring.datasource.password` | - | Password del database |
| `spring.datasource.driver-class-name` | dedotto dall'URL | Classe del driver JDBC |
| `spring.jpa.database-platform` | dedotto dal driver | Dialetto Hibernate |
| `spring.jpa.properties.hibernate.dialect` | dedotto dal driver | Dialetto Hibernate |
| `spring.jpa.mapping-resources` | - | Mapping ORM per database, `META-INF/orm-<h2\|hsql\|mysql\|oracle\|postgres\|sqlserver>.xml`: oggi i file sono vuoti, la proprietà è facoltativa (l'immagine Docker la imposta) |
| `spring.jpa.hibernate.ddl-auto` | `none` | Gestione dello schema: lasciare `none` o `validate`, lo schema è di GovPay |
| `spring.jpa.open-in-view` | `false` | Sessione JPA limitata al servizio, non estesa alla vista |
| `spring.datasource.hikari.minimum-idle` | `10` (`2` nel container) | Connessioni minime inattive del pool |
| `spring.datasource.hikari.maximum-pool-size` | `10` (`5` nel container) | Connessioni massime del pool |
| `spring.datasource.hikari.connection-timeout` | `30000` (`20000` nel container) | Attesa massima di una connessione, in ms |
| `spring.datasource.hikari.idle-timeout` | `600000` (`10000` nel container) | Tempo dopo cui una connessione inattiva viene chiusa, in ms |
| `spring.datasource.hikari.max-lifetime` | `1800000` (`600000` nel container) | Durata massima di una connessione, in ms |

I driver JDBC non sono inclusi nel jar: vedi [Esecuzione](#esecuzione).

### Logging

| Proprietà | Default | Descrizione |
|-----------|---------|-------------|
| `logging.file.name` | - (solo console) | File di log; nel container `/var/log/govpay-portal/govpay-portal-api.log` |
| `logging.level.it.govpay` | `INFO` | Livello di log dei package GovPay |
| `logging.pattern.level` | `%5p [%X{transactionId:-},%X{correlationId:-}]` | Aggiunge a ogni riga transaction id e correlation id (header `X-Correlation-ID` o `X-Request-ID`, restituiti anche in risposta e propagati alle chiamate verso API Pendenze e Stampe) |
| `govpay.logging.tracciatura.enabled` | `true` | Generazione di transaction id e correlation id (da `govpay-common`) |
| `govpay.logging.livelli-dinamici.enabled` | `true` | Livelli di log letti dalla riga `log_level` della tabella `configurazione` |
| `govpay.logging.livelli-dinamici.intervallo-refresh` | `60s` (variabile `LOG_LEVEL_REFRESH_INTERVAL`) | Intervallo di rilettura dei livelli dal database |
| `govpay.logging.livelli-dinamici.logger-gestiti[0]` | `it.govpay` | Prefissi dei logger modificabili da database |

### Monitoraggio

| Proprietà | Default | Descrizione |
|-----------|---------|-------------|
| `management.server.port` | `9090` (variabile `MANAGEMENT_SERVER_PORT`) | Porta dedicata agli endpoint actuator, separata da quella del servizio |
| `management.endpoints.web.base-path` | `/actuator` | Base path degli endpoint actuator |
| `management.endpoints.web.exposure.include` | `health,prometheus,loggers` | Endpoint actuator esposti; `loggers` è comunque rifiutato dalla configurazione di sicurezza (vedi [Monitoraggio](#monitoraggio-1)) |
| `management.metrics.distribution.percentiles-histogram.http.server.requests` | `true` | Istogrammi delle latenze delle richieste ricevute, per aggregare i percentili tra più istanze |
| `management.metrics.distribution.percentiles-histogram.http.client.requests` | `true` | Istogrammi delle latenze delle chiamate verso API Pendenze, Stampe e GDE |
| `management.metrics.distribution.slo.http.server.requests` | `100ms,500ms,1s,2s,5s` | Soglie esplicite di latenza, compresa quella SLA pagoPA di 2s |
| `management.metrics.tags.application` | `govpay-portal-api` | Tag che identifica il servizio nelle metriche |
| `govpay.metrics.enabled` | `false` | Metriche di `govpay-common`: durata delle richieste divisa tra tempo interno e chiamate esterne, durata delle chiamate per servizio. Se disattivate le chiamate non vengono misurate |

## Esecuzione

### Driver JDBC

I driver JDBC **non sono inclusi** nel fat jar e vanno forniti a runtime. Creare una directory (es. `jdbc-drivers/`) e copiarvi il driver del database utilizzato (versioni gestite da `govpay-bom`):

| Database   | Driver |
|------------|--------|
| PostgreSQL | [postgresql-42.7.14.jar](https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.14/postgresql-42.7.14.jar) |
| MySQL      | [mysql-connector-j-26.10.0.jar](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/26.10.0/mysql-connector-j-26.10.0.jar) |
| Oracle     | [ojdbc11-23.26.3.0.0.jar](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/23.26.3.0.0/ojdbc11-23.26.3.0.0.jar) |
| SQL Server | [mssql-jdbc-13.6.1.jre11.jar](https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/13.6.1.jre11/mssql-jdbc-13.6.1.jre11.jar) |
| H2         | [h2-2.5.252.jar](https://repo1.maven.org/maven2/com/h2database/h2/2.5.252/h2-2.5.252.jar) |

### Jar standalone

Il jar usa `PropertiesLauncher`: la proprietà `loader.path` indica la directory dei driver JDBC. Configurazione minima per PostgreSQL:

```bash
java -Dloader.path=./jdbc-drivers \
     -Dspring.datasource.url=jdbc:postgresql://localhost:5432/govpay \
     -Dspring.datasource.username=govpay \
     -Dspring.datasource.password=<password> \
     -Dgovpay.pendenze.base-url=http://localhost:8080/govpay/backend/api/pendenze/rs/basic/v2 \
     -Dgovpay.pendenze.username=<utente> \
     -Dgovpay.pendenze.password=<password> \
     -Dgovpay.stampe.base-url=http://localhost:8082/api/v1 \
     -Dserver.port=8081 \
     -jar target/govpay-portal-api.jar
```

Le API rispondono su `http://localhost:8081/`, gli endpoint actuator su `http://localhost:9090/actuator`.

### War su application server

Il war usa il datasource dell'application server, indicato con `spring.datasource.jndi-name` (es. `java:comp/env/it.govpay.datasource`); le altre proprietà si passano come proprietà di sistema o variabili d'ambiente. Dove trovarlo:

| Origine | Dove si trova |
|---------|---------------|
| Release (tag) | Allegato alla [release GitHub](https://github.com/link-it/govpay-portal-api/releases) come `govpay-portal-api-<tag>.war` |
| Qualsiasi build | Artifact `govpay-portal-api` della run di GitHub Actions |
| Build locale | `mvn clean install -P war`, in `target/govpay-portal-api.war` |

Le richieste autenticate richiedono comunque un proxy davanti all'application server che inoltri gli header `X-SPID-*` (vedi [Autenticazione](#autenticazione)).

## Esecuzione con Docker

L'immagine è pubblicata su Docker Hub come [linkitaly/govpay-portal-api](https://hub.docker.com/r/linkitaly/govpay-portal-api) (release) e `linkitaly/govpay-portal-api-dev` (sviluppo, a ogni push su `main`). Il servizio risponde sulla porta `8080` e gli endpoint actuator sulla `9090`; i driver JDBC si montano in `/opt/jdbc-drivers`.

```bash
docker run -d --name govpay-portal-api -p 8080:8080 -p 9090:9090 \
  -v ./jdbc-drivers:/opt/jdbc-drivers \
  -e GOVPAY_DB_TYPE=postgresql \
  -e GOVPAY_DB_SERVER=postgres:5432 \
  -e GOVPAY_DB_NAME=govpay \
  -e GOVPAY_DB_USER=govpay \
  -e GOVPAY_DB_PASSWORD=<password> \
  -e GOVPAY_API_PENDENZE_URL=http://govpay:8080/govpay/backend/api/pendenze/rs/basic/v2 \
  -e GOVPAY_API_PENDENZE_USERNAME=<utente> \
  -e GOVPAY_API_PENDENZE_PASSWORD=<password> \
  -e GOVPAY_API_STAMPE_URL=http://govpay-stampe-api:8080/api/v1 \
  linkitaly/govpay-portal-api
```

Variabili d'ambiente dell'immagine, oltre alle proprietà Spring Boot in forma di variabile (es. `GOVPAY_SECURITY_LOGINREDIRECTURLS_PORTALE=https://...`, `LOGGING_LEVEL_IT_GOVPAY=DEBUG`):

| Variabile | Obbligatoria | Default | Descrizione |
|-----------|--------------|---------|-------------|
| `GOVPAY_DB_TYPE` | sì | - | Tipo di database: `postgresql`, `mysql`, `mariadb`, `oracle` |
| `GOVPAY_DB_SERVER` | sì | - | Server del database, `host` o `host:porta` (porta di default del database se omessa) |
| `GOVPAY_DB_NAME` | sì | - | Nome del database (per Oracle, service name o SID) |
| `GOVPAY_DB_USER` | sì | - | Utente del database |
| `GOVPAY_DB_PASSWORD` | sì | - | Password del database (se manca l'avvio prosegue con un avviso) |
| `GOVPAY_DS_CONN_PARAM` | no | - | Parametri aggiunti all'URL JDBC (es. `zeroDateTimeBehavior=convertToNull` per MySQL) |
| `GOVPAY_DS_JDBC_LIBS` | no | `/opt/jdbc-drivers` | Directory dei driver JDBC |
| `GOVPAY_ORACLE_JDBC_URL_TYPE` | no | `servicename` | Formato dell'URL Oracle: `servicename` o `sid` |
| `ORACLE_TNS_ADMIN` | no | - | Directory dei file TNS di Oracle |
| `GOVPAY_PORTAL_MIN_POOL` | no | `2` | Connessioni minime inattive del pool |
| `GOVPAY_PORTAL_MAX_POOL` | no | `5` | Connessioni massime del pool |
| `GOVPAY_API_PENDENZE_URL` | sì, per i pagamenti spontanei | - | URL base dell'API Pendenze v2 di GovPay (`govpay.pendenze.base-url`) |
| `GOVPAY_API_PENDENZE_USERNAME` | sì, per i pagamenti spontanei | - | Utente dell'applicazione GovPay (`govpay.pendenze.username`) |
| `GOVPAY_API_PENDENZE_PASSWORD` | sì, per i pagamenti spontanei | - | Password dell'applicazione GovPay (`govpay.pendenze.password`) |
| `GOVPAY_API_STAMPE_URL` | sì, per i PDF | - | URL base di govpay-stampe-api (`govpay.stampe.base-url`) |
| `SERVER_PORT` | no | `8080` | Porta HTTP del servizio |
| `MANAGEMENT_SERVER_PORT` | no | `9090` | Porta degli endpoint actuator |
| `CLUSTER_ID` | no | `portal-api` | Identificativo del nodo negli eventi del GDE |
| `LOG_LEVEL_REFRESH_INTERVAL` | no | `60s` | Intervallo di rilettura dei livelli di log dal database |
| `PORTAL_TIME_ZONE` | no | `Europe/Rome` | Fuso orario dell'applicazione (`portal.time-zone`) |
| `TZ` | no | `Europe/Rome` | Fuso orario del sistema operativo del container |
| `LOGGING_FILE_NAME` | no | `/var/log/govpay-portal/govpay-portal-api.log` | File di log |
| `GOVPAY_PORTAL_JVM_MAX_RAM_PERCENTAGE` | no | `80` | `-XX:MaxRAMPercentage` della JVM |
| `GOVPAY_PORTAL_JVM_INITIAL_RAM_PERCENTAGE` | no | - | `-XX:InitialRAMPercentage` della JVM |
| `GOVPAY_PORTAL_JVM_MIN_RAM_PERCENTAGE` | no | - | `-XX:MinRAMPercentage` della JVM |
| `GOVPAY_PORTAL_JVM_MAX_METASPACE_SIZE` | no | - | `-XX:MaxMetaspaceSize` della JVM |
| `GOVPAY_PORTAL_JVM_MAX_DIRECT_MEMORY_SIZE` | no | - | `-XX:MaxDirectMemorySize` della JVM |
| `JAVA_OPTS` | no | - | Opzioni aggiuntive della JVM |

Nell'installazione su container descritta nella [documentazione di GovPay](https://govpay.readthedocs.io/it/3.10.x/installazione/container/index.html) il servizio sta dietro al reverse proxy che gestisce l'autenticazione. La scansione delle vulnerabilità dell'immagine è descritta in [docker/SCANSIONE.md](docker/SCANSIONE.md).

## Monitoraggio

Gli endpoint actuator rispondono sulla porta dedicata `9090`, non su quella del servizio:

```bash
curl http://localhost:9090/actuator/health       # stato, usato anche dalle probe dei container
curl http://localhost:9090/actuator/prometheus   # metriche in formato Prometheus
```

Gli endpoint non hanno autenticazione: la porta di management va tenuta su una rete interna, non raggiungibile dall'esterno. L'endpoint `loggers` è nell'elenco degli esposti, ma oggi la configurazione di sicurezza del servizio autorizza solo `health` e `prometheus` e lo rifiuta con `403`. Per cambiare i livelli di log si usa la console, che li salva nella tabella `configurazione`: il servizio li rilegge entro l'intervallo di refresh.

## Architettura

```
 Browser ── govpay-portal (frontend)
                  │
          reverse proxy SPID/CIE  ── header X-SPID-*
                  │
         ┌────────▼─────────┐
         │ govpay-portal-api │
         └──┬─────┬──────┬──┘
            │     │      │
  database  │     │      │ PDF avvisi e ricevute
  GovPay ◄──┘     │      └──► govpay-stampe-api
     (letture)    │
                  ├──► API Pendenze di GovPay (creazione pendenze)
                  └──► govpay-gde-api (eventi, asincrono)
```

## Licenza

GovPay Portal API è distribuito con licenza [GNU General Public License v3.0](LICENSE).
