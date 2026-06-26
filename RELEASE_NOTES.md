# GovPay Portal API — 2.0.0-SNAPSHOT

Migrazione dello stack a **Spring Boot 4.1 / Spring Framework 7 / Jackson 3**, in linea
con il resto dell'ecosistema GovPay (`govpay-bom`, `govpay-common` e gli altri moduli
`*-api` sono già a `2.0.0-SNAPSHOT`).

## Build / Dipendenze

- Parent `govpay-bom` `1.1.4` → `2.0.0-SNAPSHOT` (Spring Boot `4.1.0`, Spring Framework
  `7.0.8`, Spring Data `2026.0.0`, Hibernate 7, Jackson `3.1.4`, springdoc `3.0.3`,
  swagger-ui `5.32.6`). Versione progetto e `govpay-common` → `2.0.0-SNAPSHOT`.
- Rimossi gli override ora gestiti dal BOM: `springdoc-openapi.version`,
  `swagger-annotations.version` e `openapi.tool.codegen.version` (ereditato a `7.23.0`,
  che genera client compatibili con Spring 7).
- Aggiunte dipendenze: `spring-boot-restclient` (nuovo modulo per `RestTemplateBuilder`);
  per i test `spring-boot-webmvc-test` (slice `@AutoConfigureMockMvc`) e
  `spring-boot-data-jpa-test` (slice `@DataJpaTest` / `TestEntityManager`).

## Refactor — Jackson 3

- Migrazione da `com.fasterxml.jackson.databind/core/datatype` a `tools.jackson`. Le
  annotazioni (`com.fasterxml.jackson.annotation.*`) restano invariate.
- L'`ObjectMapper` di Jackson 3 è immutabile: la configurazione globale è ora applicata
  via `JsonMapperBuilderCustomizer` (`JacksonConfig`) invece che con un bean `@Primary`,
  così si applica al mapper effettivamente usato dai message converter HTTP. I mapper
  creati manualmente usano `JsonMapper.builder()`.
- Feature migrate a `EnumFeature` / `DateTimeFeature`; `JsonProcessingException` →
  `tools.jackson.core.JacksonException`; `TextNode` → `StringNode`. Supporto `java.time`
  nativo in Jackson 3 (rimosso `JavaTimeModule` / `jackson-datatype-jsr310`).
- `GovPayClientConfig`: il `RestTemplate` dei client generati usa ora
  `JacksonJsonHttpMessageConverter` (Spring 7) con un `JsonMapper` configurato.
- L'esclusione dei null nelle risposte resta governata da
  `spring.jackson.default-property-inclusion=non_null` (coerente col default di Jackson 3).

## Refactor — Spring Framework 7 / Spring Security 7 / Spring Boot 4

- `AntPathRequestMatcher` (rimosso in Security 7) → `PathPatternRequestMatcher`
  (`SecurityConfig`, `HardeningRequestMatcher`).
- Package relocati di Spring Boot 4: `JpaRepositoriesAutoConfiguration` →
  `DataJpaRepositoriesAutoConfiguration`, `UserDetailsServiceAutoConfiguration` e
  `RestTemplateBuilder` nei nuovi moduli dedicati.
- `StampeMapper`: adeguato ai nuovi nomi delle costanti enum `ReceiptVersion`
  (`SANP_240`, `SANP_240_V2`, `SANP_230`) prodotte dal nuovo OpenAPI Generator.

## Test

- Allineati a Jackson 3 (`JsonMapper`, `EnumFeature`/`DateTimeFeature`) e ai nuovi slice
  di test di Spring Boot 4; `@MockBean` → `@MockitoBean`.
- `SecurityConfigTest`: la verifica di accesso pubblico alla Swagger UI usa ora l'entry
  point canonico `/swagger-ui.html` (redirect `3xx`); lo static asset
  `/swagger-ui/index.html` è servito dal container reale e non risolvibile sotto MockMvc
  con springdoc 3.x.
- Suite completa verde: **687 test**.

## Pipeline

- `maven.yml`: chiave cache OWASP version-aware (include `owasp.plugin.version` dal pom,
  così un bump del plugin invalida la cache evitando incompatibilità di schema del DB NVD).
- `refresh-owasp-db.yml`: allineato a `govpay-common` / `govpay-gde-api`.

---

# GovPay Portal API — 1.1.12

## API

- La lista dei tipi pendenza per il pagamento via portale (`GET /domini/{idDominio}/tipiPendenza`) ora esclude i tipi con `pagAbilitato` `false` o `null`. Il filtro è stato aggiunto nella query JPQL `findByDominioIdAndAbilitatoWithFormPortale` (issue #9).

## Bug Fix

- `CreaPendenzaService`: il tentativo di creare una pendenza per un tipo con `pagAbilitato` non `true` ora restituisce `HTTP 422 Unprocessable Entity` (`UnprocessableEntityException`) invece di `400 Bad Request`, conforme a RFC 9110 §15.5.21 (richiesta sintatticamente valida ma non processabile per vincoli di business).

## Build / Dipendenze

- Aggiornato parent `govpay-bom` da `1.1.2` → `1.1.4`.
- Aggiornato `govpay-common` da `1.0.0` → `1.1.2`.
- Avanzata versione progetto a `1.1.12`.

## Refactor

- `GdeService` allineato al nuovo contratto di `AbstractGdeService`: aggiunto override del metodo astratto `getConfigurazioneComponente(ComponenteEvento, Giornale)` (mappa la componente sul ramo corrispondente di `Giornale`, allineato al pattern già adottato in `govpay-fdr-batch`).

## Security

- Aggiunte suppression per `CVE-2026-22747` (false positive su Spring Security `6.5.10`; la CVE riguarda solo le versioni `7.0.0`–`7.0.4`):
  - OWASP Dependency-Check: `src/main/resources/owasp/falsePositives/CVE-2026-22747.xml`.
  - OSV-Scanner: `osv-scanner.toml` in root.

## Test

- `CreaPendenzaServiceTest`: aggiornato al nuovo tipo di eccezione (`UnprocessableEntityException`).
- `PendenzeControllerIntegrationTest.testCreaPendenza_PagamentoNonAbilitato`: ora attende `HTTP 422` invece di `400`.
- `TipoVersamentoDominioRepositoryTest`: aggiunto test che verifica l'esclusione dei tipi con `pagAbilitato` `false` o `null` dal listing.
- `AnagraficaServiceTest`: aggiornato con `pagAbilitato`.

## Pipeline (precedenti su questa milestone)

- Unificati i report della GitHub Release in un unico ZIP `release-reports-${tag}.zip`, allineando la pipeline a `govpay-fdr-batch`.
- Migliorata la gestione della cache OWASP Dependency-Check (chiave basata su data, skip `autoUpdate` NVD su cache-hit, nuovo workflow `refresh-owasp-db.yml` schedulato notturno). Aggiornate `actions/upload-artifact` e `download-artifact` a `v7`.
- Aggiunto job `osv-scan` nella pipeline GitHub Actions, allineato a `govpay-fdr-batch`.
