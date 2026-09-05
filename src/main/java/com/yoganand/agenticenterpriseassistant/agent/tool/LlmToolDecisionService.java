package com.yoganand.agenticenterpriseassistant.agent.tool;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LlmToolDecisionService {

    private final ChatModel chatModel;

    private final McpToolSpecificationProvider
            mcpToolSpecificationProvider;

    public LlmToolDecisionService(
            ChatModel chatModel,
            McpToolSpecificationProvider mcpToolSpecificationProvider
    ) {
        this.chatModel = chatModel;
        this.mcpToolSpecificationProvider =
                mcpToolSpecificationProvider;
    }

    public AiMessage decideTool(
            String userQuery
    ) {

        List<ToolSpecification> toolSpecifications =
                mcpToolSpecificationProvider
                        .getToolSpecifications();

        ChatRequest chatRequest =
                ChatRequest.builder()
                        .messages(
                                UserMessage.from(userQuery)
                        )
                        .toolSpecifications(
                                toolSpecifications
                        )
                        .build();

        ChatResponse chatResponse =
                chatModel.chat(chatRequest);

        return chatResponse.aiMessage();
    }

}