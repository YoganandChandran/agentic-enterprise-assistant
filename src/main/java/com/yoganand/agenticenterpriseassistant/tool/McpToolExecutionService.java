package com.yoganand.agenticenterpriseassistant.tool;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class McpToolExecutionService {

    private final ToolCallbackProvider toolCallbackProvider;

    public McpToolExecutionService(
            ToolCallbackProvider toolCallbackProvider
    ) {
        this.toolCallbackProvider = toolCallbackProvider;
    }

    // NEW METHOD - Generic LLM generated tool execution
    public String executeTool(
            ToolExecutionRequest toolExecutionRequest
    ) {

        String toolName =
                toolExecutionRequest.name();

        ToolCallback toolCallback =
                findTool(toolName);

        if (toolCallback == null) {
            throw new IllegalStateException(
                    "MCP tool not found: " + toolName
            );
        }

        return toolCallback.call(
                toolExecutionRequest.arguments()
        );
    }

    private ToolCallback findTool(String toolName) {

        return Arrays.stream(
                        toolCallbackProvider.getToolCallbacks()
                )
                .filter(tool ->
                        tool.getToolDefinition()
                                .name()
                                .equals(toolName)
                )
                .findFirst()
                .orElse(null);
    }
}