<p align="center">
<img src="https://www.link.it/wp-content/uploads/2025/01/logo-govpay.svg" alt="GovPay Logo" width="200"/>
</p>

# GovPay - Porta di accesso al sistema pagoPA - Portal API

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://raw.githubusercontent.com/link-it/govpay-portal-api/main/LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)

## Sommario

API REST Spring Boot per il Portale al Cittadino di GovPay. Consente ai cittadini di:
- Consultare i propri debiti (pendenze)
- Effettuare pagamenti spontanei
- Scaricare avvisi di pagamento in formato PDF
- Scaricare ricevute di pagamento

Il sistema si integra con GovPay come backend per la gestione dei pagamenti pagoPA.

## Architettura

### Componenti Principali

```
┌─────────────────────────────────────────────────────────────────┐
│                      Portal Frontend                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    GovPay Portal API                             │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐    │
│  │  Anagrafica    │  │   Pendenze     │  │    Stampe      │    │
│  │  Controller    │  │   Controller   │  │    Service     │    │
│  └────────────────┘  └────────────────┘  └────────────────┘    │
│           │                   │                   │              │
│           ▼                   ▼                   ▼              │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              Services Layer (Business Logic)            │    │
│  └─────────────────────────────────────────────────────────┘    │
│           │                   │                   │              │
│           ▼                   ▼                   ▼              │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐    │
│  │  GovPay DB     │  │  GovPay API    │  │  GovPay API    │    │
│  │  (JPA)         │  │  Pendenze      │  │  Stampe        │    │
│  └────────────────┘  └────────────────┘  └────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        GovPay Backend                            │
└─────────────────────────────────────────────────────────────────┘
```

### Flusso Pagamento Spontaneo

1. **Richiesta Form**: Il frontend richiede la configurazione del tipo pendenza
2. **Rendering Form**: Il template FreeMarker genera il form dinamico
3. **Submit Dati**: L'utente compila e invia il form
4. **Trasformazione**: I dati vengono trasformati via template FreeMarker
5. **Creazione Pendenza**: La pendenza viene creata su GovPay
6. **Generazione Avviso**: Viene restituito l'avviso di pagamento

## API Endpoints

### Anagrafica

| Metodo | Endpoint | Auth | Descrizione |
|--------|----------|------|-------------|
| `GET` | `/login` | SPID | Crea la sessione e restituisce il profilo utente |
| `GET` | `/login/{urlID}` | SPID | Crea la sessione e redirige verso una URL configurata |
| `GET` | `/profilo` | SPID | Ottiene il profilo dell'utente autenticato |
| `GET` | `/logout` | SPID | Effettua il logout dell'utente |
| `GET` | `/logout/{urlID}` | SPID | Effettua il logout e redirige verso una URL configurata |
| `GET` | `/domini` | Public | Lista dei domini (enti creditori) disponibili |
| `GET` | `/domini/{idDominio}` | Public | Dettaglio di un dominio |
| `GET` | `/domini/{idDominio}/logo` | Public | Logo del dominio |
| `GET` | `/domini/{idDominio}/tipiPendenza` | Public | Tipi di pendenza disponibili per il dominio |
| `GET` | `/domini/{idDominio}/tipiPendenza/{idTipoPendenza}` | Public | Dettaglio tipo pendenza con form |

### Pendenze

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `POST` | `/pendenze/{idDominio}/{idTipoPendenza}` | Crea una nuova pendenza (pagamento spontaneo) |
| `GET` | `/pendenze/{idDominio}` | Lista pendenze per dominio |
| `GET` | `/pendenze/{idDominio}/{numeroAvviso}` | Dettaglio pendenza |
| `GET` | `/pendenze/{idDominio}/{numeroAvviso}/avviso` | Avviso di pagamento (JSON o PDF) |
| `GET` | `/pendenze/{idDominio}/{numeroAvviso}/ricevuta` | Ricevuta di pagamento (JSON o PDF) |

## Configurazione

### Parametri Applicazione

```properties
# Configurazione Server
server.port=8080
server.servlet.context-path=/govpay-portal-api

# Timezone
govpay.timezone=Europe/Rome
```

### Parametri GovPay API

```properties
# URL base API GovPay
govpay.api.pendenze.base-url=http://localhost:8080/govpay/backend/api/pendenze/rs/form/v2
govpay.api.stampe.base-url=http://localhost:8080/govpay/backend/api/pendenze/rs/form/v2

# Credenziali Basic Auth
govpay.api.pendenze.username=admin
govpay.api.pendenze.password=admin
govpay.api.stampe.username=admin
govpay.api.stampe.password=admin
```

### Database

```properties
# PostgreSQL (produzione)
spring.datasource.url=jdbc:postgresql://localhost:5432/govpay
spring.datasource.username=govpay
spring.datasource.password=govpay
spring.datasource.driver-class-name=org.postgresql.Driver

# H2 in-memory (sviluppo/test)
#spring.datasource.url=jdbc:h2:mem:govpaydb
#spring.datasource.driver-class-name=org.h2.Driver
```

### Sicurezza SPID

```properties
# Nomi degli header HTTP inoltrati dal reverse proxy (valori di default mostrati)
govpay.security.spid-headers.fiscal-number=X-SPID-FISCALNUMBER
govpay.security.spid-headers.name=X-SPID-NAME
govpay.security.spid-headers.family-name=X-SPID-FAMILYNAME
govpay.security.spid-headers.email=X-SPID-EMAIL
govpay.security.spid-headers.mobile-phone=X-SPID-MOBILEPHONE
govpay.security.spid-headers.address=X-SPID-ADDRESS
```

### Login e Logout con Redirect

Al termine del flusso SPID, il reverse proxy redirige il browser verso l'endpoint
`/login/{urlID}` della Portal API. L'applicazione crea la sessione autenticata e
redirige il browser verso la URL associata all'`urlID` configurato. Lo stesso
meccanismo e' disponibile per il logout.

```properties
# URL di redirect dopo il login (mappa urlID -> URL)
govpay.security.login-redirect-urls.portale=https://portal.example.com/dashboard
govpay.security.login-redirect-urls.app=https://app.example.com/home

# URL di redirect dopo il logout (mappa urlID -> URL)
govpay.security.logout-redirect-urls.portale=https://portal.example.com/logged-out
govpay.security.logout-redirect-urls.app=https://app.example.com/goodbye
```

I query parameter della richiesta originale vengono inoltrati alla URL di redirect.
Ad esempio, `GET /login/portale?lang=it` redirige verso
`https://portal.example.com/dashboard?lang=it`.

Solo le URL presenti nella whitelist sono ammesse; un `urlID` non configurato
restituisce `404 Not Found`.

### Google reCAPTCHA

```properties
# Abilitazione reCAPTCHA per protezione form
# Configurato tramite tabella CONFIGURAZIONI in database
```

## Compilazione ed Esecuzione

### Requisiti

- Java 21
- Maven 3.6.3+
- Database PostgreSQL 9.6+ (o H2 per sviluppo), MySQL, Oracle, SQL Server
- Driver JDBC del database scelto (non incluso nel fat jar)
- GovPay backend operativo

### Compilazione

```bash
# Imposta JAVA_HOME
export JAVA_HOME=/path/to/jdk-21

# Compilazione con profilo JAR (default)
mvn clean install

# Compilazione con profilo WAR (per deploy su application server)
mvn clean install -Pwar
```

### Esecuzione

I driver JDBC non sono inclusi nel fat jar e devono essere forniti esternamente
tramite la proprietà `loader.path` del `PropertiesLauncher` di Spring Boot.

```bash
# Avvio applicazione standalone (con driver JDBC esterno)
java -Dloader.path=./jdbc-drivers -jar target/govpay-portal-api.jar

# Con profilo specifico
java -Dloader.path=./jdbc-drivers -jar target/govpay-portal-api.jar --spring.profiles.active=prod

# Con variabili d'ambiente
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/govpay
export SPRING_DATASOURCE_USERNAME=govpay
export SPRING_DATASOURCE_PASSWORD=govpay
java -Dloader.path=./jdbc-drivers -jar target/govpay-portal-api.jar
```

Per i dettagli sui driver supportati e le istruzioni di download, vedere
[docker/jdbc-drivers/README.md](docker/jdbc-drivers/README.md).

### Deploy WAR

```bash
# Genera il WAR
mvn clean package -Pwar

# Copia su Tomcat/WildFly
cp target/govpay-portal-api.war $CATALINA_HOME/webapps/
```

## Caratteristiche Implementate

### Autenticazione e Autorizzazione
- Supporto autenticazione SPID tramite header proxy
- Gestione sessioni utente
- Accesso anonimo per endpoint pubblici

### Trasformazione FreeMarker
- Template dinamici per form pagamento spontaneo
- Contesto ricco con dati utente, dominio, data/ora
- Generazione automatica identificativi pendenza

### Integrazione GovPay
- Client REST generati da OpenAPI
- Gestione errori con mapping HTTP status
- Logging dettagliato delle chiamate API

### Stampe PDF
- Generazione avvisi di pagamento
- Generazione ricevute di pagamento
- Supporto lingua secondaria (es. tedesco per Alto Adige)

### Sicurezza
- Protezione reCAPTCHA per form pubblici
- Validazione input
- Gestione eccezioni centralizzata

## Test

```bash
# Esecuzione test
mvn test

# Con report copertura JaCoCo
mvn test jacoco:report

# Report disponibile in target/site/jacoco/index.html
```

## API Documentation

La documentazione OpenAPI è disponibile all'avvio dell'applicazione:

- **Swagger UI**: `http://localhost:8080/govpay-portal-api/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/govpay-portal-api/v3/api-docs`

## Note Tecniche

| Componente | Versione |
|------------|----------|
| Java | 21 |
| Spring Boot | 3.5.9 |
| OpenAPI Generator | 7.10.0 |
| SpringDoc OpenAPI | 2.8.15 |
| JaCoCo | 0.8.13 |
| Database | PostgreSQL, MySQL, Oracle, SQL Server, H2 |

## Contribuire

Per contribuire al progetto:

1. Fork del repository
2. Creare un branch per la feature (`git checkout -b feature/AmazingFeature`)
3. Commit delle modifiche (`git commit -m 'Add some AmazingFeature'`)
4. Push del branch (`git push origin feature/AmazingFeature`)
5. Aprire una Pull Request

Assicurarsi di:
- Seguire lo stile di codifica del progetto
- Aggiungere test per nuove funzionalità
- Documentare le modifiche nel README se necessario

## License

Questo progetto è distribuito sotto licenza GPL v3. Vedere il file [LICENSE](LICENSE) per i dettagli.

## Contatti

- **Progetto**: [GovPay Portal API](https://github.com/link-it/govpay-portal-api)
- **Organizzazione**: [Link.it](https://www.link.it)

## Riconoscimenti

Questo progetto è parte dell'ecosistema [GovPay](https://www.govpay.it) per la gestione dei pagamenti della Pubblica Amministrazione italiana tramite pagoPA.
