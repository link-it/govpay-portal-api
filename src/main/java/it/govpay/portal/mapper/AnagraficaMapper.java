package it.govpay.portal.mapper;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.govpay.portal.entity.TipoVersamentoDominio;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.TipoPendenza;
import it.govpay.portal.model.TipoPendenzaForm;

@Component
public class AnagraficaMapper {

    private final ObjectMapper objectMapper;

    public AnagraficaMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private static final String COD_UO_ENTE_CREDITORE = "EC";

    public Dominio toDominio(it.govpay.portal.entity.Dominio entity) {
        if (entity == null) {
            return null;
        }

        Dominio dominio = new Dominio();
        dominio.setIdDominio(entity.getCodDominio());
        dominio.setRagioneSociale(entity.getRagioneSociale());
        dominio.setCbill(entity.getCbill());

        entity.getUnitaOrganizzative().stream()
                .filter(uo -> COD_UO_ENTE_CREDITORE.equals(uo.getCodUo()))
                .findFirst()
                .ifPresent(uo -> {
                    dominio.setIndirizzo(uo.getUoIndirizzo());
                    dominio.setCivico(uo.getUoCivico());
                    dominio.setCap(uo.getUoCap());
                    dominio.setLocalita(uo.getUoLocalita());
                    dominio.setProvincia(uo.getUoProvincia());
                    dominio.setNazione(uo.getUoNazione());
                    dominio.setEmail(uo.getUoEmail());
                    dominio.setPec(uo.getUoPec());
                    dominio.setTel(uo.getUoTel());
                    dominio.setFax(uo.getUoFax());
                    dominio.setWeb(uo.getUoUrlSitoWeb());
                });

        return dominio;
    }

    public TipoPendenza toTipoPendenza(TipoVersamentoDominio entity) {
        if (entity == null) {
            return null;
        }

        String codTipoVersamento = entity.getTipoVersamento() != null
                ? entity.getTipoVersamento().getCodTipoVersamento()
                : null;

        String descrizione = entity.getTipoVersamento() != null
                ? entity.getTipoVersamento().getDescrizione()
                : null;

        TipoPendenza tipoPendenza = new TipoPendenza(codTipoVersamento, descrizione);

        if (entity.getPagFormTipo() != null && entity.getPagFormDefinizione() != null) {
            TipoPendenzaForm form = new TipoPendenzaForm();
            form.setTipo(entity.getPagFormTipo());
            form.setDefinizione(parseJson(entity.getPagFormDefinizione()));
            form.setImpaginazione(parseJson(entity.getPagFormImpaginazione()));
            tipoPendenza.setForm(form);
        } else if (entity.getTipoVersamento() != null) {
            String formTipo = entity.getTipoVersamento().getPagFormTipo();
            String formDef = entity.getTipoVersamento().getPagFormDefinizione();
            if (formTipo != null && formDef != null) {
                TipoPendenzaForm form = new TipoPendenzaForm();
                form.setTipo(formTipo);
                form.setDefinizione(parseJson(formDef));
                form.setImpaginazione(parseJson(entity.getTipoVersamento().getPagFormImpaginazione()));
                tipoPendenza.setForm(form);
            }
        }

        return tipoPendenza;
    }

    private Object parseJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            return json;
        }
    }

}
