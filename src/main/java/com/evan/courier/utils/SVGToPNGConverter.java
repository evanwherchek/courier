package com.evan.courier.utils;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SVGToPNGConverter {

    public static byte[] convertSVGToPNG(String svgContent) throws IOException, TranscoderException {
        // Create a PNG transcoder
        PNGTranscoder transcoder = new PNGTranscoder();

        // Set transcoder hints for quality and proper rendering
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 800f);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 400f);

        // Create input from SVG string
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
            svgContent.getBytes(StandardCharsets.UTF_8)
        );
        TranscoderInput input = new TranscoderInput(inputStream);

        // Create output stream for PNG
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(outputStream);

        // Perform the transcoding
        transcoder.transcode(input, output);

        // Return the PNG bytes
        return outputStream.toByteArray();
    }
}
