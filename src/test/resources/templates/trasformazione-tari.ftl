{
    "causale": "Tassa rifiuti ${jsonPath.read("$.annoRiferimento")} - ${jsonPath.read("$.nome")} ${jsonPath.read("$.cognome")}",
    "importo": ${jsonPath.read("$.importo")},
    "dataScadenza": "${jsonPath.read("$.dataScadenza")}",
    "soggettoPagatore": {
        "tipo": "F",
        "identificativo": "${jsonPath.read("$.codiceFiscale")}",
        "anagrafica": "${jsonPath.read("$.nome")} ${jsonPath.read("$.cognome")}",
        "email": "${jsonPath.read("$.email")!""}"
    },
    "voci": [{
        "idVocePendenza": "TARI-${jsonPath.read("$.annoRiferimento")}",
        "importo": ${jsonPath.read("$.importo")},
        "descrizione": "Tassa rifiuti anno ${jsonPath.read("$.annoRiferimento")}"
    }]
}
