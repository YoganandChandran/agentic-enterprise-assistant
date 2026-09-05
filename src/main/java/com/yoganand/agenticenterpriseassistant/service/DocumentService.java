package com.yoganand.agenticenterpriseassistant.service;

import com.yoganand.agenticenterpriseassistant.dto.DocumentIngestionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    Long uploadDocument(MultipartFile file);

    String extractText(MultipartFile file);

    List<String> extractAndChunk(MultipartFile file);

    DocumentIngestionResponse ingestDocument(MultipartFile file);

    DocumentIngestionResponse ingestAndStoreDocument(
            MultipartFile file
    );
}
