package com.yoganand.agenticenterpriseassistant.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class McpToolSpecificationProvider {

    private final ToolCallbackProvider toolCallbackProvider;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public McpToolSpecificationProvider(
            ToolCallbackProvider toolCallbackProvider
    ) {
        this.toolCallbackProvider = toolCallbackProvider;
    }

    public List<ToolSpecification> getToolSpecifications() {

        ToolCallback[] toolCallbacks =
                toolCallbackProvider.getToolCallbacks();

        return Arrays.stream(toolCallbacks)
                .map(this::convertToToolSpecification)
                .toList();
    }

    private ToolSpecification convertToToolSpecification(
            ToolCallback toolCallback
    ) {

        String toolName =
                toolCallback.getToolDefinition().name();

        String toolDescription =
                toolCallback.getToolDefinition().description();

        String inputSchema =
                toolCallback.getToolDefinition().inputSchema();

        JsonObjectSchema parameters =
                convertToJsonObjectSchema(inputSchema);

        return ToolSpecification.builder()
                .name(toolName)
                .description(toolDescription)
                .parameters(parameters)
                .build();
    }

    private JsonObjectSchema convertToJsonObjectSchema(
            String inputSchema
    ) {

        try {

            JsonNode rootNode =
                    objectMapper.readTree(inputSchema);

            JsonObjectSchema.Builder schemaBuilder =
                    JsonObjectSchema.builder();

            JsonNode propertiesNode =
                    rootNode.path("properties");

            propertiesNode.fields()
                    .forEachRemaining(entry -> {

                        String propertyName =
                                entry.getKey();

                        JsonNode propertySchema =
                                entry.getValue();

                        schemaBuilder.addProperty(
                                propertyName,
                                convertProperty(
                                        propertySchema
                                )
                        );
                    });

            JsonNode requiredNode =
                    rootNode.path("required");

            if (requiredNode.isArray()) {

                List<String> requiredFields =
                        new java.util.ArrayList<>();

                requiredNode.forEach(node ->
                        requiredFields.add(
                                node.asText()
                        )
                );

                if (!requiredFields.isEmpty()) {

                    schemaBuilder.required(
                            requiredFields.toArray(
                                    new String[0]
                            )
                    );
                }
            }

            return schemaBuilder.build();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to convert MCP input schema",
                    exception
            );
        }
    }

    private JsonSchemaElement convertProperty(
            JsonNode propertySchema
    ) {

        String type =
                propertySchema.path("type")
                        .asText("string");

        String description =
                propertySchema.path("description")
                        .asText(null);

        return switch (type) {

            case "string" ->
                    JsonStringSchema.builder()
                            .description(description)
                            .build();

            case "integer" ->
                    JsonIntegerSchema.builder()
                            .description(description)
                            .build();

            case "number" ->
                    JsonNumberSchema.builder()
                            .description(description)
                            .build();

            case "boolean" ->
                    JsonBooleanSchema.builder()
                            .description(description)
                            .build();

            case "array" ->
                    JsonArraySchema.builder()
                            .description(description)
                            .items(
                                    convertArrayItems(
                                            propertySchema
                                    )
                            )
                            .build();

            case "object" ->
                    convertNestedObject(
                            propertySchema
                    );

            default ->
                    JsonStringSchema.builder()
                            .description(description)
                            .build();
        };
    }

    private JsonSchemaElement convertArrayItems(
            JsonNode propertySchema
    ) {

        JsonNode itemsNode =
                propertySchema.path("items");

        if (itemsNode.isMissingNode()) {

            return JsonStringSchema.builder()
                    .build();
        }

        return convertProperty(itemsNode);
    }

    private JsonObjectSchema convertNestedObject(
            JsonNode objectNode
    ) {

        JsonObjectSchema.Builder schemaBuilder =
                JsonObjectSchema.builder();

        JsonNode propertiesNode =
                objectNode.path("properties");

        propertiesNode.fields()
                .forEachRemaining(entry ->
                        schemaBuilder.addProperty(
                                entry.getKey(),
                                convertProperty(
                                        entry.getValue()
                                )
                        )
                );

        JsonNode requiredNode =
                objectNode.path("required");

        if (requiredNode.isArray()) {

            List<String> requiredFields =
                    new java.util.ArrayList<>();

            requiredNode.forEach(node ->
                    requiredFields.add(
                            node.asText()
                    )
            );

            if (!requiredFields.isEmpty()) {

                schemaBuilder.required(
                        requiredFields.toArray(
                                new String[0]
                        )
                );
            }
        }

        return schemaBuilder.build();
    }

    @PostConstruct
    public void printToolSpecifications() {

        System.out.println(
                "\n========== LANGCHAIN4J TOOL SPECIFICATIONS =========="
        );

        getToolSpecifications()
                .forEach(tool -> {

                    System.out.println(
                            "Tool Name: "
                                    + tool.name()
                    );

                    System.out.println(
                            "Description: "
                                    + tool.description()
                    );

                    System.out.println(
                            "Parameters: "
                                    + tool.parameters()
                    );

                    System.out.println(
                            "--------------------------------"
                    );
                });

        System.out.println(
                "====================================================\n"
        );
    }
}