package com.yoganand.agenticenterpriseassistant.agent.graph;

import com.yoganand.agenticenterpriseassistant.agent.RagAgentState;
import com.yoganand.agenticenterpriseassistant.agent.node.LlmAnswerNode;
import com.yoganand.agenticenterpriseassistant.agent.node.McpToolNode;
import com.yoganand.agenticenterpriseassistant.agent.node.QueryRewriteNode;
import com.yoganand.agenticenterpriseassistant.agent.node.RagRetrievalNode;
import com.yoganand.agenticenterpriseassistant.agent.node.RetrievalEvaluatorNode;
import com.yoganand.agenticenterpriseassistant.agent.node.RouterNode;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Component
public class AgentGraph {

    private final CompiledGraph<RagAgentState> graph;

    public AgentGraph(
            RouterNode routerNode,
            RagRetrievalNode ragRetrievalNode,
            RetrievalEvaluatorNode retrievalEvaluatorNode,
            QueryRewriteNode queryRewriteNode,
            McpToolNode mcpToolNode,
            LlmAnswerNode llmAnswerNode
    ) throws GraphStateException {

        StateGraph<RagAgentState> stateGraph =
                new StateGraph<>(
                        RagAgentState::new
                );

        stateGraph
                .addNode(
                        "router",
                        node_async(routerNode)
                )
                .addNode(
                        "retrieve",
                        node_async(ragRetrievalNode)
                )
                .addNode(
                        "evaluateRetrieval",
                        node_async(retrievalEvaluatorNode)
                )
                .addNode(
                        "queryRewrite",
                        node_async(queryRewriteNode)
                )
                .addNode(
                        "mcpTool",
                        node_async(mcpToolNode)
                )
                .addNode(
                        "generate",
                        node_async(llmAnswerNode)
                )
                .addEdge(
                        START,
                        "router"
                )
                .addConditionalEdges(
                        "router",
                        this::routeQuestion,
                        Map.of(
                                "RAG", "retrieve",
                                "MCP", "mcpTool",
                                "DIRECT", "generate"
                        )
                )
                .addEdge(
                        "retrieve",
                        "evaluateRetrieval"
                )
                .addConditionalEdges(
                        "evaluateRetrieval",
                        this::routeRetrieval,
                        Map.of(
                                "SUFFICIENT", "generate",
                                "RETRY", "queryRewrite",
                                "MAX_RETRIES", "generate"
                        )
                )
                .addEdge(
                        "queryRewrite",
                        "retrieve"
                )
                .addEdge(
                        "mcpTool",
                        "generate"
                )
                .addEdge(
                        "generate",
                        END
                );

        this.graph = stateGraph.compile();
    }

    private CompletableFuture<String> routeQuestion(
            RagAgentState state
    ) {
        return CompletableFuture.completedFuture(
                state.getRoute()
        );
    }

    private CompletableFuture<String> routeRetrieval(
            RagAgentState state
    ) {

        String decision =
                state.getRetrievalDecision();

        int attempt =
                state.getRetrievalAttempt();

        if ("SUFFICIENT".equals(decision)) {
            return CompletableFuture.completedFuture(
                    "SUFFICIENT"
            );
        }

        if ("INSUFFICIENT".equals(decision)
                && attempt < 2) {

            return CompletableFuture.completedFuture(
                    "RETRY"
            );
        }

        return CompletableFuture.completedFuture(
                "MAX_RETRIES"
        );
    }

    public CompiledGraph<RagAgentState> getGraph() {
        return graph;
    }
}