package com.yoganand.agenticenterpriseassistant.controller;

import com.yoganand.agenticenterpriseassistant.dto.RagRetrievalResponse;
import com.yoganand.agenticenterpriseassistant.service.RagAnswerService;
import com.yoganand.agenticenterpriseassistant.service.RagRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.yoganand.agenticenterpriseassistant.dto.RagAnswerResponse;
import com.yoganand.agenticenterpriseassistant.dto.RagQuestionRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagRetrievalService ragRetrievalService;
    private final RagAnswerService ragAnswerService;


    @GetMapping("/retrieve")
    public ResponseEntity<RagRetrievalResponse> retrieve(
            @RequestParam String query
    ) {

        return ResponseEntity.ok(
                ragRetrievalService.retrieve(query)
        );
    }

    @PostMapping("/ask")
    public ResponseEntity<RagAnswerResponse> ask(
            @RequestBody RagQuestionRequest request
    ) {

        String answer = ragAnswerService.getAnswer(
                request.userId(),request.question()
        );

        return ResponseEntity.ok(
                new RagAnswerResponse(answer)
        );
    }
}