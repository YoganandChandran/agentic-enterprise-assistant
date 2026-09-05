package com.yoganand.agenticenterpriseassistant.dto;

public record DocumentIngestionResponse(

        Long documentId,

        String fileName,

        int totalChunks,

        String message

) {
}