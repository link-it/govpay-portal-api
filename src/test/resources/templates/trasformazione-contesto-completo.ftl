{
    "idA2A": "APP_PORTAL",
    "idPendenza": "${transactionId}",
    "causale": "Pendenza per dominio ${idDominio} tipo ${idTipoVersamento}",
    "importo": ${jsonPath.read("$.importo")},
    "soggettoPagatore": {
        "tipo": "F",
        "identificativo": "${jsonPath.read("$.cf")}",
        "anagrafica": "${jsonPath.read("$.nominativo")}"
    },
    "voci": [{
        "idVocePendenza": "${idDominio}-${idTipoVersamento}-001",
        "importo": ${jsonPath.read("$.importo")},
        "descrizione": "Voce generata il ${date?string('dd/MM/yyyy')}",
        "codEntrata": "ENTRATA_TEST"
    }]
}
