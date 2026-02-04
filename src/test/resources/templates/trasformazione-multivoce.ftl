{
    "causale": "${jsonPath.read("$.causale")}",
    "importo": ${jsonPath.read("$.importoTotale")},
    "dataScadenza": "${jsonPath.read("$.dataScadenza")}",
    "soggettoPagatore": {
        "tipo": "${jsonPath.read("$.soggetto.tipo")}",
        "identificativo": "${jsonPath.read("$.soggetto.cf")}",
        "anagrafica": "${jsonPath.read("$.soggetto.anagrafica")}",
        "indirizzo": "${jsonPath.read("$.soggetto.indirizzo")!""}",
        "civico": "${jsonPath.read("$.soggetto.civico")!""}",
        "cap": "${jsonPath.read("$.soggetto.cap")!""}",
        "localita": "${jsonPath.read("$.soggetto.localita")!""}",
        "provincia": "${jsonPath.read("$.soggetto.provincia")!""}",
        "email": "${jsonPath.read("$.soggetto.email")!""}"
    },
    "voci": [
    <#assign vociList = jsonPath.readList("$.voci[*]")>
    <#list vociList as voceJson>
        <#assign voce = voceJson?eval>
        {
            "idVocePendenza": "${voce.id}",
            "importo": ${voce.importo},
            "descrizione": "${voce.descrizione}"
        }<#sep>,</#sep>
    </#list>
    ]
}
