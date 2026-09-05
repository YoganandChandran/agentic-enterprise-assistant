package com.yoganand.agenticenterpriseassistant.agent.graph;

import com.yoganand.agenticenterpriseassistant.agent.RagAgentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class AgentGraphTest {

    @Autowired
    private AgentGraph agentGraph;

    @Test
    void shouldExecuteAgenticRagGraph() throws Exception {

        // 1. Initial state
        Map<String, Object> initialState = Map.of(
                "question", "What is the annual leave policy?"
        );

        // 2. Execute LangGraph
        var result =
                agentGraph
                        .getGraph()
                        .invoke(initialState);

        // 3. Print final state
        System.out.println("FINAL STATE:");
        System.out.println(result);
    }
}