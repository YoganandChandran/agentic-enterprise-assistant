package com.yoganand.agenticenterpriseassistant.controller;

import com.yoganand.agenticenterpriseassistant.dto.DocumentIngestionResponse;
import com.yoganand.agenticenterpriseassistant.dto.DocumentUploadResponse;
import com.yoganand.agenticenterpriseassistant.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> upload(
            @RequestParam("file") MultipartFile file
    ) {

        Long documentId =
                documentService.uploadDocument(file);

        return ResponseEntity.ok(
                new DocumentUploadResponse(
                        documentId,
                        file.getOriginalFilename(),
                        "Document uploaded successfully"
                )
        );
    }

    @PostMapping("/extract")
    public ResponseEntity<String> extractText(
            @RequestParam("file") MultipartFile file
    ) {

        String extractedText =
                documentService.extractText(file);

        return ResponseEntity.ok(extractedText);
    }

    @PostMapping("/chunk")
    public ResponseEntity<List<String>> chunkDocument(
            @RequestParam("file") MultipartFile file
    ) {

        List<String> chunks =
                documentService.extractAndChunk(file);

        return ResponseEntity.ok(chunks);
    }

    @PostMapping("/ingest")
    public ResponseEntity<DocumentIngestionResponse> ingestDocument(
            @RequestParam("file") MultipartFile file
    ) {

        return ResponseEntity.ok(
                documentService.ingestDocument(file)
        );
    }

    @PostMapping("/ingest-and-store")
    public ResponseEntity<DocumentIngestionResponse> ingestAndStoreDocument(
            @RequestParam("file") MultipartFile file
    ) {

        return ResponseEntity.ok(
                documentService.ingestAndStoreDocument(file)
        );
    }
}