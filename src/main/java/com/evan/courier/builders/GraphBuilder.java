package com.evan.courier.builders;

import com.evan.courier.utils.SVGToPNGConverter;
import com.evan.courier.utils.TemplateEngine;
import org.apache.batik.transcoder.TranscoderException;

import java.io.IOException;
import java.nio.file.*;
import java.net.*;
import java.util.*;

public class GraphBuilder {

    public static class DataPoint {
        private String label;
        private double value;

        public DataPoint(String label, double value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() { return label; }
        public double getValue() { return value; }
    }

    public String build() {
        // Prepare data
        List<DataPoint> dataPoints = Arrays.asList(
            new DataPoint("Jan", 45.5),
            new DataPoint("Feb", 67.2),
            new DataPoint("Mar", 52.8),
            new DataPoint("Apr", 78.5),
            new DataPoint("May", 85.3),
            new DataPoint("Jun", 92.1)
        );

        // Find max value for scaling
        double maxValue = dataPoints.stream()
            .mapToDouble(DataPoint::getValue)
            .max()
            .orElse(100.0);

        // Round up maxValue for cleaner axis
        maxValue = Math.ceil(maxValue / 10) * 10;

        // Build data model
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("chartTitle", "Monthly Sales");
        dataModel.put("dataPoints", dataPoints);
        dataModel.put("maxValue", maxValue);

        // Generate SVG content (using dedicated SVG template without HTML wrapper)
        String svgContent = TemplateEngine.processTemplate("graph-svg.ftl", dataModel);

        // Convert SVG to PNG
        byte[] pngData;
        try {
            pngData = SVGToPNGConverter.convertSVGToPNG(svgContent);
            System.out.println("PNG generated successfully. Size: " + pngData.length + " bytes");

            // Save PNG to resources directory for email attachment
            // Use target/classes path which is where Spring Boot serves resources from
            URL resourceUrl = getClass().getClassLoader().getResource("");
            Path resourcePath = Paths.get(resourceUrl.toURI());
            Path graphPath = resourcePath.resolve("images").resolve("graph.png");

            // Create images directory if it doesn't exist
            Files.createDirectories(graphPath.getParent());

            // Write the PNG file
            Files.write(graphPath, pngData);
            System.out.println("Graph PNG saved to: " + graphPath);
        } catch (IOException | TranscoderException | URISyntaxException e) {
            throw new RuntimeException("Failed to convert SVG to PNG", e);
        }

        // Generate HTML content using wrapper template
        return TemplateEngine.processTemplate("graph-wrapper.ftl", dataModel);
    }
}