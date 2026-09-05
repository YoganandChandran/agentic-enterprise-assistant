# Agentic Enterprise Assistant

An enterprise-focused AI assistant built with **Spring Boot 4.1.0** and **Java 26**. The project combines **LLM-based routing, Agentic RAG, MCP tool integration, query rewriting, retrieval evaluation, PostgreSQL/pgvector, and persistent conversation memory** into a stateful LangGraph4j workflow.

> **Note:** The MCP server implementation is intentionally **not included/committed in this repository**. The assistant connects to an externally running MCP server through Spring AI MCP Client configuration.

## What This Project Does

The assistant accepts a user's question and dynamically decides how the request should be handled:

- **RAG** — retrieve information from enterprise documents stored as embeddings in PostgreSQL/pgvector.
- **MCP** — use an external enterprise tool through an MCP server.
- **DIRECT** — answer directly using the LLM and available conversation memory.

For RAG requests, the workflow does not blindly generate an answer after the first retrieval. It evaluates whether the retrieved context is sufficient. If it is insufficient, an LLM rewrites the search query and the system performs a second retrieval attempt. The retry is capped at **two retrieval attempts**.

## Architecture

The main `/api/rag/ask` flow is implemented using **LangGraph4j** and a shared `RagAgentState`.

```text
                         ┌─────────────────────┐
                         │    User Question    │
                         │    POST /api/rag/ask│
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    Router Node      │
                         │        (LLM)         │
                         │ RAG / MCP / DIRECT  │
                         └──────┬─────┬─────┬──┘
                                │     │     │
                    ┌───────────┘     │     └────────────┐
                    ▼                 ▼                  ▼
             ┌────────────┐    ┌──────────────┐   ┌──────────────┐
             │ RAG        │    │ MCP Tool     │   │ Direct LLM   │
             │ Retrieval  │    │ Decision     │   │ Answer       │
             └─────┬──────┘    │ (LLM)        │   └──────┬───────┘
                   │           └──────┬───────┘          │
                   ▼                  ▼                  │
             ┌──────────────┐  ┌──────────────┐          │
             │ Retrieval    │  │ MCP Tool     │          │
             │ Evaluator    │  │ Execution    │          │
             │ (LLM)        │  └──────┬───────┘          │
             └──────┬───────┘         │                  │
                    │                  │                  │
             ┌──────┴───────┐         │                  │
             │              │         │                  │
         SUFFICIENT     INSUFFICIENT  │                  │
             │              │         │                  │
             │              ▼         │                  │
             │       ┌──────────────┐  │                  │
             │       │ Query Rewrite│  │                  │
             │       │    (LLM)     │  │                  │
             │       └──────┬───────┘  │                  │
             │              │          │                  │
             │              ▼          │                  │
             │       ┌──────────────┐  │                  │
             │       │ Retrieve     │  │                  │
             │       │ Attempt #2   │  │                  │
             │       └──────┬───────┘  │                  │
             │              │          │                  │
             │              ▼          │                  │
             │       ┌──────────────┐  │                  │
             │       │ Evaluate     │  │                  │
             │       │ Again        │  │                  │
             │       └──────┬───────┘  │                  │
             │              │          │                  │
             └──────────────┴──────────┴──────────────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │ Answer Generation   │
                         │        (LLM)         │
                         │ Context + Tools +    │
                         │ Conversation Memory  │
                         └──────────┬──────────┘
                                    │
                                    ▼
                              ┌───────────┐
                              │  Response │
                              └───────────┘
```

### Agentic RAG Decision Loop

The RAG branch is intentionally limited to two retrieval attempts:

```text
Original question
       │
       ▼
Retrieve
       │
       ▼
Evaluate retrieved context
       │
   ┌───┴───────────────┐
   │                   │
Sufficient          Insufficient
   │                   │
   ▼                   ▼
Generate          Rewrite query
                       │
                       ▼
                   Retrieve #2
                       │
                       ▼
                   Evaluate again
                       │
                 ┌─────┴─────┐
                 │           │
             Sufficient   Insufficient
                 │           │
                 ▼           ▼
             Generate    Max retries
                             │
                             ▼
                          Generate
```

The evaluator always evaluates the retrieved context against the **original user question**. Query rewriting changes the retrieval query, not the user's actual question.

## Key Components

### LangGraph4j Agent Workflow

`AgentGraph` defines the stateful workflow and conditional transitions.

Nodes currently used:

- `RouterNode`
- `RagRetrievalNode`
- `RetrievalEvaluatorNode`
- `QueryRewriteNode`
- `McpToolNode`
- `LlmAnswerNode`

`RagAgentState` carries information such as:

- user ID
- original question
- conversation summary
- recent messages
- route
- retrieved context
- retrieval decision
- rewritten query
- retrieval attempt
- MCP tool result
- final answer

### Intelligent Routing

`RouterNode` uses an LLM to select exactly one route:

```text
RAG
MCP
DIRECT
```

A safety fallback sends unexpected LLM output to `DIRECT`.

### Agentic RAG

The RAG branch:

1. Embeds the retrieval query.
2. Searches PostgreSQL/pgvector.
3. Retrieves up to five matches.
4. Filters matches using a minimum similarity score of `0.50`.
5. Uses an LLM to evaluate whether the context is sufficient.
6. If insufficient, rewrites the query.
7. Performs one retry.
8. Generates the final answer after success or reaching the retry limit.

The embedding model configured in the source is:

`AllMiniLmL6V2EmbeddingModel`

The configured vector dimension is `384`.

### MCP Tool Integration

The application uses **Spring AI MCP Client** configuration to connect to an external MCP server.

The assistant:

1. Gets available MCP tool callbacks.
2. Converts their schemas into LangChain4j `ToolSpecification` objects.
3. Lets the LLM decide which tool to invoke.
4. Executes the selected tool through `ToolCallback`.
5. Stores the tool result in agent state.
6. Passes the result to the final answer-generation node.

This design avoids hardcoding a specific MCP tool schema inside the tool specification provider.

**The MCP server itself is not part of this repository.**

### Conversation Memory

Conversation state is persisted in PostgreSQL.

The system:

- stores user and assistant messages;
- loads the latest 10 messages for the conversation;
- keeps a conversation summary;
- summarizes older messages after the configured threshold;
- retains recent messages while older messages are compressed into the summary.

Current configuration:

```yaml
conversation:
  memory:
    max-recent-messages: 10
    summarization-threshold: 20
```

### Document Ingestion

The document pipeline currently supports **PDF files**.

Flow:

```text
PDF Upload
   ↓
Validation
   ↓
Metadata stored in PostgreSQL
   ↓
Apache Tika document parsing
   ↓
Recursive chunking
   ↓
Embeddings
   ↓
PostgreSQL / pgvector
```

The current chunking configuration uses LangChain4j's recursive splitter with:

- chunk size: `500`
- overlap: `50`

## Technology Stack

Verified from the uploaded source:

- **Java 26**
- **Spring Boot 4.1.0**
- Spring Web
- Spring Data JPA
- Spring Security / validation-related Spring imports as applicable to the project build
- **Spring AI MCP Client**
- **LangGraph4j**
- **LangChain4j**
- PostgreSQL
- PostgreSQL `pgvector`
- Apache Tika document parser
- LangChain4j ONNX `AllMiniLmL6V2EmbeddingModel`
- Lombok
- JPA / Hibernate
- OpenRouter through the OpenAI-compatible LangChain4j chat model integration

### LLM Configuration

The source configures LangChain4j's `OpenAiChatModel` against OpenRouter:

```yaml
openrouter:
  api:
    key: ${OPENROUTER_API_KEY}
    base-url: https://openrouter.ai/api/v1
    model-name: openrouter/free
```

The API key is expected through the `OPENROUTER_API_KEY` environment variable.

## Project Structure

```text
src/main/java/com/yoganand/agenticenterpriseassistant
│
├── agent
│   ├── RagAgentState.java
│   ├── graph
│   │   └── AgentGraph.java
│   ├── node
│   │   ├── RouterNode.java
│   │   ├── RagRetrievalNode.java
│   │   ├── RetrievalEvaluatorNode.java
│   │   ├── QueryRewriteNode.java
│   │   ├── McpToolNode.java
│   │   └── LlmAnswerNode.java
│   └── tool
│       ├── LlmToolDecisionService.java
│       └── McpToolSpecificationProvider.java
│
├── config
│   ├── EmbeddingConfig.java
│   ├── OpenRouterConfig.java
│   └── PgVectorConfig.java
│
├── controller
│   ├── ChatController.java
│   ├── DocumentController.java
│   ├── RagController.java
│   └── SearchController.java
│
├── document
│   ├── DocumentChunker.java
│   ├── DocumentValidator.java
│   └── LangChainDocumentLoader.java
│
├── dto
├── exception
├── model
├── repository
├── service
│   └── impl
│
└── tool
    └── McpToolExecutionService.java
```

## Requirements

Before starting the application, make sure you have:

1. **Java 26**
2. **Spring Boot 4.1.0 compatible build setup**
3. **PostgreSQL**
4. PostgreSQL with **pgvector** support
5. A PostgreSQL database named `agentic_assistant`
6. The required application tables
7. An **OpenRouter API key**
8. An MCP server application running separately
9. The MCP server URL configured in `application.yaml`

## Database

The application uses PostgreSQL for both relational application data and vector storage.

### Relational tables used by the application

The JPA entities map to:

```text
conversations
conversation_messages
documents
```

The vector store is configured to use:

```text
document_embeddings
```

The vector store configuration currently expects:

```text
host      = localhost
port      = 5432
database  = agentic_assistant
user      = postgres
table     = document_embeddings
dimension = 384
```

Because the source uses `PgVectorEmbeddingStore`, the PostgreSQL environment must support pgvector.

> The exact vector-table DDL is intentionally not duplicated here because the source delegates vector-store handling to LangChain4j's `PgVectorEmbeddingStore`. Use the schema required by the version of the LangChain4j pgvector module used by your build.

### JPA schema behavior

The application uses:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Therefore, **the required tables must already exist**. Hibernate will validate the schema rather than create it.

## Configuration

The main configuration is in:

```text
src/main/resources/application.yaml
```

You need to configure at least:

### 1. PostgreSQL

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/agentic_assistant
    username: <your-postgres-user>
    password: <your-postgres-password>
```

Also update the PostgreSQL connection in:

```text
src/main/java/com/yoganand/agenticenterpriseassistant/config/PgVectorConfig.java
```

The current source has the pgvector connection values directly in that configuration class.

### 2. OpenRouter API Key

Set:

```bash
OPENROUTER_API_KEY=<your-api-key>
```

The application reads it through:

```yaml
openrouter:
  api:
    key: ${OPENROUTER_API_KEY}
```

Do **not** commit API keys, passwords, or other secrets to GitHub.

### 3. MCP Server

The assistant expects an external MCP server.

Current configuration:

```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        name: agentic-enterprise-assistant
        version: 1.0.0
        request-timeout: 30s
        type: SYNC

        streamable-http:
          connections:
            employee-server:
              url: http://localhost:8081
```

Start the MCP server separately and change:

```yaml
url: http://localhost:8081
```

to the URL where your MCP server is running.

Again, **the MCP server source is not included in this repository**.

## Running the Project

### 1. Start PostgreSQL

Create the database:

```sql
CREATE DATABASE agentic_assistant;
```

Make sure PostgreSQL has pgvector support.

### 2. Prepare the required tables

Create the JPA tables:

```text
conversations
conversation_messages
documents
```

and prepare the pgvector-backed:

```text
document_embeddings
```

table according to the LangChain4j `PgVectorEmbeddingStore` schema for the dependency version used by your project.

### 3. Configure `application.yaml`

Set:

- PostgreSQL connection
- MCP server URL
- memory configuration if required

### 4. Configure the OpenRouter key

Linux/macOS:

```bash
export OPENROUTER_API_KEY="your-api-key"
```

Windows PowerShell:

```powershell
$env:OPENROUTER_API_KEY="your-api-key"
```

### 5. Start the MCP server

Run your separate MCP server application on the configured URL/port.

### 6. Start this application

Run the Spring Boot application from your IDE or your project's build command.

The API runs on:

```text
http://localhost:8080
```

## API Endpoints

### Agentic RAG / Main Assistant

```http
POST /api/rag/ask
Content-Type: application/json
```

Request:

```json
{
  "userId": "EMP-102",
  "question": "What is the annual leave policy?"
}
```

Response:

```json
{
  "answer": "..."
}
```

This endpoint executes the complete LangGraph agent workflow.

### Direct RAG Retrieval

```http
GET /api/rag/retrieve?query=annual%20leave
```

### Semantic Search

```http
GET /api/search?query=annual%20leave
```

### Document Upload

```http
POST /api/documents/upload
```

Multipart field:

```text
file
```

### Extract PDF Text

```http
POST /api/documents/extract
```

### Chunk PDF

```http
POST /api/documents/chunk
```

### Ingest Document

```http
POST /api/documents/ingest
```

### Ingest, Embed and Store

```http
POST /api/documents/ingest-and-store
```

This is the endpoint to use when the PDF should be processed into embeddings and stored in pgvector.

### Basic Chat

```http
POST /api/chat
Content-Type: application/json
```

Request:

```json
{
  "message": "Hello"
}
```

This endpoint uses the configured chat model directly and is separate from the full Agentic RAG graph.

## Example Agentic Scenarios

### RAG Question

```text
User:
What is the company's leave policy?
```

The router selects:

```text
RAG
```

Then:

```text
Retrieve → Evaluate → Generate
```

### Insufficient Retrieval

```text
User:
What is the company's policy regarding moon travel?
```

Possible workflow:

```text
Retrieve original query
        ↓
INSUFFICIENT
        ↓
LLM rewrites retrieval query
        ↓
Retrieve again
        ↓
Evaluate against original question
        ↓
Still INSUFFICIENT
        ↓
Maximum retrieval attempts reached
        ↓
Generate an answer without inventing information
```

### MCP Question

For a question requiring employee-system data, the router can select:

```text
MCP
```

Then the MCP branch:

```text
LLM Tool Decision
       ↓
Select MCP Tool
       ↓
Execute Tool
       ↓
Store Tool Result
       ↓
Generate Final Answer
```

### Direct Question

For general conversation:

```text
User → Router → DIRECT → LLM Answer
```

Conversation memory is still available to the final answer-generation node.

## Where Google Cloud Fits

The current uploaded source **does not contain Google Cloud SDK dependencies or GCP-specific service implementations**. The application currently uses local PostgreSQL/pgvector and a locally configured MCP server URL.

Therefore, the GCP elements shown in any architecture illustration should be treated as **deployment/integration options**, not as services currently implemented in this repository.

### If you want to deploy this application on Google Cloud

The main areas that would need to change are:

#### PostgreSQL / pgvector

Current:

```text
PgVectorConfig.java
→ localhost:5432
→ agentic_assistant
```

For a cloud deployment, replace the local database connection with the connection details of your chosen PostgreSQL/pgvector environment.

Depending on the deployment architecture, this could be a managed PostgreSQL service or another PostgreSQL environment that supports pgvector.

#### Application configuration

Move environment-specific values out of source code and into deployment configuration/environment variables:

```text
database URL
database username
database password
OpenRouter API key
MCP server URL
```

#### MCP Server

The current MCP URL is:

```text
http://localhost:8081
```

A cloud deployment would point this to the deployed MCP server endpoint instead.

#### Container deployment

The Spring Boot application can be containerized and deployed to a Google Cloud compute/container platform. The application code itself does not currently contain a dedicated GCP SDK integration.

> In short: **GCP is a deployment/integration extension point for the current codebase, not a currently implemented application feature.**

## Who Can Benefit From This Project?

This architecture is useful for teams building:

- **Enterprise Knowledge Assistants**
- **HR policy and employee support assistants**
- **Internal document Q&A systems**
- **IT helpdesk assistants**
- **Enterprise search and knowledge discovery**
- **Tool-enabled AI assistants**
- **MCP-based enterprise automation**
- **Customer/internal support copilots**
- **Document-grounded AI applications**
- **AI assistants that need persistent conversation context**

It is especially relevant when an AI assistant needs to combine **knowledge retrieval + enterprise tools + conversational context** rather than relying only on a standalone LLM.

## Design Highlights

### Stateful Agent Workflow

LangGraph4j keeps the execution state across nodes so routing, retrieval, evaluation, rewriting, tool results, memory and final generation can participate in the same workflow.

### Retrieval Quality Check

Instead of assuming that retrieved documents are sufficient, an LLM evaluates whether they can answer the **original user question**.

### Controlled Retry

Query rewriting enables a second retrieval attempt while the maximum of two retrieval attempts prevents an uncontrolled retrieval loop.

### Dynamic MCP Tool Discovery

MCP tool definitions are read from Spring AI `ToolCallback`s and converted into LangChain4j tool specifications, allowing the tool-decision layer to work from the available tool metadata instead of a hardcoded employee-tool schema.

### Persistent Conversation Memory

Conversation messages and summaries are persisted in PostgreSQL, allowing the assistant to retain useful context across requests.

## Security / Secrets

Do not commit:

- OpenRouter API keys
- Database passwords
- Production credentials
- MCP authentication secrets

The uploaded source currently contains a database password in `application.yaml` and hardcoded PostgreSQL connection details in `PgVectorConfig.java`. **Before publishing this repository publicly, replace those values with environment/configuration-based secrets and rotate any credential that has already been exposed.**

## Testing

The repository contains an integration-style Spring Boot test for the LangGraph workflow:

```text
src/test/java/com/yoganand/agenticenterpriseassistant/agent/graph/AgentGraphTest.java
```

The test boots the Spring application, obtains `AgentGraph`, invokes the graph with an initial question, and prints the final state.

## Repository

GitHub:

https://github.com/YoganandChandran

LinkedIn:

https://www.linkedin.com/in/yoganand-chandran-a181a520

## Author

**Yoganand Chandran**

Java Backend Engineer | Spring Boot | Microservices | GCP | AI Engineering

---

### Project Summary

This project demonstrates how a traditional enterprise backend can be extended into a stateful AI system where an LLM can **route requests, retrieve and evaluate knowledge, rewrite queries when retrieval is insufficient, invoke enterprise tools through MCP, and use persistent conversation memory before generating the final response**.
