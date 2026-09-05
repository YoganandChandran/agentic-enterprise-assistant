package com.yoganand.agenticenterpriseassistant.controller;

import com.yoganand.agenticenterpriseassistant.dto.ChatRequest;
import com.yoganand.agenticenterpriseassistant.dto.ChatResponse;
import com.yoganand.agenticenterpriseassistant.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request
    ) {

        String response = chatService.chat(request.message());

        return ResponseEntity.ok(
                new ChatResponse(response)
        );
    }
}