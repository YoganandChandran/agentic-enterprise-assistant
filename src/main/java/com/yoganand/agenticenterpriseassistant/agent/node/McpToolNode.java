package com.yoganand.agenticenterpriseassistant.agent.node;

import com.yoganand.agenticenterpriseassistant.agent.RagAgentState;
import com.yoganand.agenticenterpriseassistant.agent.tool.LlmToolDecisionService;
import com.yoganand.agenticenterpriseassistant.tool.McpToolExecutionService;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class McpToolNode implements NodeAction<RagAgentState> {

    private final LlmToolDecisionService
            llmToolDecisionService;

    private final McpToolExecutionService
            mcpToolExecutionService;

    @Override
    public Map<String, Object> apply(RagAgentState state) {

        String question = state.getQuestion();

        System.out.println(
                "\n========== AGENTIC TOOL DECISION =========="
        );

        System.out.println(
                "Question: " + question
        );

        // Step 1: Let LLM decide which tool to use
        AiMessage aiMessage =
                llmToolDecisionService.decideTool(question);

        // Step 2: Check whether LLM requested a tool
        if (!aiMessage.hasToolExecutionRequests()) {

            System.out.println(
                    "No tool execution requested by LLM"
            );

            return Map.of(
                    "toolResult",
                    ""
            );
        }

        // Currently execute the first requested tool
        ToolExecutionRequest toolRequest =
                aiMessage.toolExecutionRequests().getFirst();

        System.out.println(
                "Selected Tool: "
                        + toolRequest.name()
        );

        System.out.println(
                "Tool Arguments: "
                        + toolRequest.arguments()
        );

        // Step 3: Execute LLM-selected MCP tool
        String toolResult =
                mcpToolExecutionService.executeTool(
                        toolRequest
                );

        System.out.println(
                "Tool Result: "
                        + toolResult
        );

        System.out.println(
                "============================================\n"
        );

        // Step 4: Save tool result into Agent State
        return Map.of(
                "toolResult",
                toolResult
        );
    }
}