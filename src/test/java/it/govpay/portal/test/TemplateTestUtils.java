package it.govpay.portal.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.core.io.ClassPathResource;

/**
 * Utility per caricare i template FreeMarker dai file di test.
 */
public class TemplateTestUtils {

    private static final String TEMPLATES_PATH = "templates/";

    private TemplateTestUtils() {}

    /**
     * Carica un template dal classpath e lo restituisce come stringa.
     *
     * @param templateName nome del file template (es. "trasformazione-tari.ftl")
     * @return contenuto del template
     */
    public static String loadTemplate(String templateName) {
        try {
            ClassPathResource resource = new ClassPathResource(TEMPLATES_PATH + templateName);
            try (InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore nel caricamento del template: " + templateName, e);
        }
    }

    /**
     * Carica un template e lo codifica in Base64.
     *
     * @param templateName nome del file template
     * @return template codificato in Base64
     */
    public static String loadTemplateAsBase64(String templateName) {
        String template = loadTemplate(templateName);
        return Base64.getEncoder().encodeToString(template.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Template per la tassa rifiuti (TARI).
     */
    public static String getTariTemplate() {
        return loadTemplate("trasformazione-tari.ftl");
    }

    /**
     * Template per la tassa rifiuti (TARI) codificato in Base64.
     */
    public static String getTariTemplateBase64() {
        return loadTemplateAsBase64("trasformazione-tari.ftl");
    }

    /**
     * Template con contesto completo (headers, query params, path params).
     */
    public static String getContestoCompletoTemplate() {
        return loadTemplate("trasformazione-contesto-completo.ftl");
    }

    /**
     * Template con contesto completo codificato in Base64.
     */
    public static String getContestoCompletoTemplateBase64() {
        return loadTemplateAsBase64("trasformazione-contesto-completo.ftl");
    }

    /**
     * Template per pendenze multi-voce.
     */
    public static String getMultivoceTemplate() {
        return loadTemplate("trasformazione-multivoce.ftl");
    }

    /**
     * Template per pendenze multi-voce codificato in Base64.
     */
    public static String getMultivoceTemplateBase64() {
        return loadTemplateAsBase64("trasformazione-multivoce.ftl");
    }
}
