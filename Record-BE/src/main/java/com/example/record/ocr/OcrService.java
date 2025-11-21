package com.example.record.ocr;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Service
public class OcrService {

    public String extractTextFromImage(File imageFile) throws IOException {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create();
             FileInputStream fis = new FileInputStream(imageFile)) {

            ByteString imgBytes = ByteString.readFrom(fis);
            Image image = Image.newBuilder().setContent(imgBytes).build();

            Feature feature = Feature.newBuilder()
                    .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                    .build();

            ImageContext ctx = ImageContext.newBuilder()
                    .addLanguageHints("ko")
                    .addLanguageHints("en")
                    .build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image)
                    .setImageContext(ctx)
                    .build();

            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));
            if (response == null || response.getResponsesCount() == 0) return "";

            AnnotateImageResponse r0 = response.getResponses(0);
            if (r0.hasError()) throw new IOException("Vision API error: " + r0.getError().getMessage());

            return r0.hasFullTextAnnotation()
                    ? r0.getFullTextAnnotation().getText()
                    : "";
        }
    }
}
