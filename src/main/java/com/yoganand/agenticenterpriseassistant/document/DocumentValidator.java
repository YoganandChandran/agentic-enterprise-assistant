package com.yoganand.agenticenterpriseassistant.document;

import com.yoganand.agenticenterpriseassistant.exception.InvalidDocumentException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DocumentValidator {

    private static final String PDF_CONTENT_TYPE =
            "application/pdf";

    public void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException(
                    "Document file cannot be empty"
            );
        }

        if (!PDF_CONTENT_TYPE.equals(file.getContentType())) {
            throw new InvalidDocumentException(
                    "Only PDF files are supported"
            );
        }
    }
}