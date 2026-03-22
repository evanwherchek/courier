package com.evan.courier.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class TemplateEngine {
    private static final Logger logger = LoggerFactory.getLogger(TemplateEngine.class);
    private static final Configuration cfg;

    static {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassForTemplateLoading(TemplateEngine.class, "/templates");
        cfg.setDefaultEncoding("UTF-8");
        logger.info("Template engine initialized");
    }

    /**
     * Renders a Freemarker template from the {@code /templates} classpath directory.
     *
     * @param templateName the filename of the template (e.g., {@code "email-wrapper.ftl"})
     * @param data         the data model map made available to the template during processing
     * @return the fully rendered template output as a string
     * @throws RuntimeException if the template cannot be loaded or processing fails
     */
    public static String processTemplate(String templateName, Map<String, Object> data) {
        try {
            Template template = cfg.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(data, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("Failed to process template: " + templateName, e);
        }
    }
}
