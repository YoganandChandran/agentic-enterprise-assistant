package com.yoganand.agenticenterpriseassistant.service.impl;

import com.yoganand.agenticenterpriseassistant.document.DocumentChunker;
import com.yoganand.agenticenterpriseassistant.document.DocumentValidator;
import com.yoganand.agenticenterpriseassistant.document.LangChainDocumentLoader;
import com.yoganand.agenticenterpriseassistant.dto.DocumentIngestionResponse;
import com.yoganand.agenticenterpriseassistant.model.Document;
import com.yoganand.agenticenterpriseassistant.repository.DocumentRepository;
import com.yoganand.agenticenterpriseassistant.service.DocumentService;
import com.yoganand.agenticenterpriseassistant.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final LangChainDocumentLoader langChainDocumentLoader;
    private final DocumentChunker documentChunker;
    private final DocumentValidator documentValidator;
    private final EmbeddingService embeddingService;


    @Override
    public Long uploadDocument(MultipartFile file) {

        Document document = Document.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        Document savedDocument =
                documentRepository.save(document);

        return savedDocument.getId();
    }

    @Override
    public String extractText(MultipartFile file) {

        dev.langchain4j.data.document.Document document =
                langChainDocumentLoader.load(file);

        return document.text();
    }

    @Override
    public List<String> extractAndChunk(MultipartFile file) {

        dev.langchain4j.data.document.Document document =
                langChainDocumentLoader.load(file);

        return documentChunker.split(document)
                .stream()
                .map(segment -> segment.text())
                .toList();
    }

    @Override
    public DocumentIngestionResponse ingestDocument(MultipartFile file) {

        // Step 1: Validate uploaded file
        documentValidator.validate(file);


        // Step 2: Save metadata
        Document document = Document.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        Document savedDocument =
                documentRepository.save(document);


        // Step 3: Load document using LangChain4j
        dev.langchain4j.data.document.Document langChainDocument =
                langChainDocumentLoader.load(file);


        // Step 4: Split document into chunks
        int totalChunks =
                documentChunker
                        .split(langChainDocument)
                        .size();


        // Step 5: Return summary
        return new DocumentIngestionResponse(
                savedDocument.getId(),
                savedDocument.getFileName(),
                totalChunks,
                "Document ingested successfully"
        );
    }

    @Override
    public DocumentIngestionResponse ingestAndStoreDocument(
            MultipartFile file
    ) {

        // Step 1: Validate
        documentValidator.validate(file);


        // Step 2: Save metadata
        Document document = Document.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        Document savedDocument =
                documentRepository.save(document);


        // Step 3: Load PDF using LangChain4j
        dev.langchain4j.data.document.Document langChainDocument =
                langChainDocumentLoader.load(file);


        // Step 4: Split into chunks
        List<dev.langchain4j.data.segment.TextSegment> segments =
                documentChunker.split(langChainDocument);


        // Step 5: Generate embeddings and store in pgvector
        embeddingService.storeSegments(segments);


        // Step 6: Return response
        return new DocumentIngestionResponse(
                savedDocument.getId(),
                savedDocument.getFileName(),
                segments.size(),
                "Document embedded and stored successfully"
        );
    }
}
