package it.govpay.portal.utils.trasformazioni;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.core.StopException;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModel;
import it.govpay.portal.utils.trasformazioni.exception.TrasformazioneException;

/**
 * Utility per l'elaborazione di template FreeMarker.
 * Basato su it.govpay.core.utils.trasformazioni.TrasformazioniUtils di GovPay,
 * ma senza dipendenze da OpenSPCoop2/GovWay.
 */
public class TrasformazioniUtils {

    private static final Logger log = LoggerFactory.getLogger(TrasformazioniUtils.class);

    private static final String MAP_RESPONSE = "responseMap";

    private static Configuration freemarkerConfig;

    private TrasformazioniUtils() {}

    /**
     * Inizializza la configurazione FreeMarker (singleton).
     */
    private static synchronized Configuration getConfiguration() {
        if (freemarkerConfig == null) {
            freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
            freemarkerConfig.setDefaultEncoding(StandardCharsets.UTF_8.name());
            freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            freemarkerConfig.setLogTemplateExceptions(false);
            freemarkerConfig.setWrapUncheckedExceptions(true);
            freemarkerConfig.setFallbackOnNullLoopVariable(false);
            freemarkerConfig.setAPIBuiltinEnabled(true);
        }
        return freemarkerConfig;
    }

    /**
     * Elabora un template FreeMarker e restituisce il risultato come stringa.
     *
     * @param templateName nome del template (per logging/errori)
     * @param templateContent contenuto del template
     * @param context contesto con i dati per la trasformazione
     * @return risultato della trasformazione
     */
    public static String transform(String templateName, String templateContent, Map<String, Object> context)
            throws TrasformazioneException {
        try (StringWriter writer = new StringWriter()) {
            transformToWriter(templateName, templateContent, context, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new TrasformazioneException("Errore I/O durante la trasformazione: " + e.getMessage(), e);
        }
    }

    /**
     * Elabora un template FreeMarker e restituisce il risultato come byte array.
     *
     * @param templateName nome del template
     * @param templateContent contenuto del template
     * @param context contesto con i dati per la trasformazione
     * @return risultato della trasformazione come byte array
     */
    public static byte[] transformToBytes(String templateName, String templateContent, Map<String, Object> context)
            throws TrasformazioneException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            transformToStream(templateName, templateContent, context, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new TrasformazioneException("Errore I/O durante la trasformazione: " + e.getMessage(), e);
        }
    }

    /**
     * Elabora un template FreeMarker (come byte array) e scrive su OutputStream.
     *
     * @param templateName nome del template
     * @param templateBytes contenuto del template come byte array
     * @param context contesto con i dati per la trasformazione
     * @param outputStream stream di output
     */
    public static void transformToStream(String templateName, byte[] templateBytes, Map<String, Object> context,
            OutputStream outputStream) throws TrasformazioneException {
        String templateContent = new String(templateBytes, StandardCharsets.UTF_8);
        transformToStream(templateName, templateContent, context, outputStream);
    }

    /**
     * Elabora un template FreeMarker e scrive su OutputStream.
     *
     * @param templateName nome del template
     * @param templateContent contenuto del template
     * @param context contesto con i dati per la trasformazione
     * @param outputStream stream di output
     */
    public static void transformToStream(String templateName, String templateContent, Map<String, Object> context,
            OutputStream outputStream) throws TrasformazioneException {
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            transformToWriter(templateName, templateContent, context, writer);
            writer.flush();
        } catch (IOException e) {
            throw new TrasformazioneException("Errore I/O durante la trasformazione: " + e.getMessage(), e);
        }
    }

    /**
     * Elabora un template FreeMarker e scrive su Writer.
     *
     * @param templateName nome del template
     * @param templateContent contenuto del template
     * @param context contesto con i dati per la trasformazione
     * @param writer writer di output
     */
    public static void transformToWriter(String templateName, String templateContent, Map<String, Object> context,
            Writer writer) throws TrasformazioneException {
        try {
            // Prepara la mappa dinamica con le utility FreeMarker
            Map<String, Object> dynamicMap = new HashMap<>(context);

            // Aggiunge utility per usare metodi statici
            BeansWrapper wrapper = new BeansWrapper(Configuration.VERSION_2_3_32);
            TemplateModel classModel = wrapper.getStaticModels();
            dynamicMap.put(Costanti.MAP_CLASS_LOAD_STATIC, classModel);

            // Aggiunge utility per istanziare oggetti
            dynamicMap.put(Costanti.MAP_CLASS_NEW_INSTANCE, new freemarker.template.utility.ObjectConstructor());

            // Aggiunge mappa per i dati di ritorno
            Map<String, Object> responseMap = new HashMap<>();
            dynamicMap.put(MAP_RESPONSE, responseMap);

            // Costruisce e processa il template
            Configuration config = getConfiguration();
            Template template = new Template(templateName, new StringReader(templateContent), config);
            template.process(dynamicMap, writer);
            writer.flush();

        } catch (StopException e) {
            // StopException è usata dai template per segnalare errori di validazione
            throw new TrasformazioneException("Template interrotto: " + e.getMessageWithoutStackTop(), e);
        } catch (freemarker.template.TemplateException e) {
            log.error("Errore FreeMarker nel template '{}': {}", templateName, e.getMessage());
            throw new TrasformazioneException("Errore nel template FreeMarker: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new TrasformazioneException("Errore I/O durante la trasformazione: " + e.getMessage(), e);
        }
    }

    /**
     * Elabora un template FreeMarker utilizzando il TransformationContext builder.
     *
     * @param templateName nome del template
     * @param templateContent contenuto del template
     * @param contextBuilder builder del contesto
     * @return risultato della trasformazione
     */
    public static String transform(String templateName, String templateContent, TransformationContext contextBuilder)
            throws TrasformazioneException {
        return transform(templateName, templateContent, contextBuilder.build());
    }

    /**
     * Valida un template FreeMarker senza eseguirlo.
     *
     * @param templateName nome del template
     * @param templateContent contenuto del template
     * @throws TrasformazioneException se il template non è valido
     */
    public static void validateTemplate(String templateName, String templateContent) throws TrasformazioneException {
        try {
            Configuration config = getConfiguration();
            new Template(templateName, new StringReader(templateContent), config);
        } catch (IOException e) {
            throw new TrasformazioneException("Template non valido: " + e.getMessage(), e);
        }
    }

    /**
     * Crea un nuovo TransformationContext builder.
     */
    public static TransformationContext context() {
        return TransformationContext.builder();
    }
}
