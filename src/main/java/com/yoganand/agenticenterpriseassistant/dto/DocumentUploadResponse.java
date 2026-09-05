package com.yoganand.agenticenterpriseassistant.dto;


public record DocumentUploadResponse(
        Long documentId,
        String fileName,
        String message
) {
}
