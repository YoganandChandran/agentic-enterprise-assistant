package com.yoganand.agenticenterpriseassistant.document;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class LangChainDocumentLoader {

    public Document load(MultipartFile multipartFile) {

        Path tempFile = null;

        try {

            tempFile = Files.createTempFile(
                    "uploaded-document-",
                    ".pdf"
            );

            multipartFile.transferTo(tempFile);

            return FileSystemDocumentLoader.loadDocument(
                    tempFile,
                    new ApacheTikaDocumentParser()
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to load PDF document",
                    e
            );

        } finally {

            if (tempFile != null) {

                try {
                    Files.deleteIfExists(tempFile);

                } catch (IOException e) {

                    throw new RuntimeException(
                            "Failed to delete temporary file",
                            e
                    );
                }
            }
        }
    }
}
