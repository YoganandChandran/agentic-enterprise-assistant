package com.yoganand.agenticenterpriseassistant.agent;

import org.bsc.langgraph4j.state.AgentState;

import java.util.List;
import java.util.Map;

public class RagAgentState extends AgentState {

    public RagAgentState(Map<String, Object> initState) {
        super(initState);
    }

    public String getUserId() {
        return (String) value("userId").orElse("");
    }

    public String getQuestion() {
        return (String) value("question").orElse("");
    }

    public String getConversationSummary() {
        return (String) value("conversationSummary").orElse("");
    }

    @SuppressWarnings("unchecked")
    public List<String> getRecentMessages() {
        return (List<String>) value("recentMessages")
                .orElse(List.of());
    }

    public String getRetrievedContext() {
        return (String) value("retrievedContext").orElse("");
    }

    public String getEmployeeId() {
        return (String) value("employeeId").orElse("");
    }

    public String getToolResult() {
        return (String) value("toolResult").orElse("");
    }

    public String getRoute() {
        return (String) value("route").orElse("");
    }

    public String getRetrievalDecision() {
        return (String) value("retrievalDecision")
                .orElse("");
    }

    public String getRewrittenQuery() {
        return (String) value("rewrittenQuery").orElse("");
    }

    public int getRetrievalAttempt() {
        return (Integer) value("retrievalAttempt").orElse(0);
    }

    public String getAnswer() {
        return (String) value("answer").orElse("");
    }
}