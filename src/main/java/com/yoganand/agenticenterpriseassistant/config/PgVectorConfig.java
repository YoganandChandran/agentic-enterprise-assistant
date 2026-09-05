package com.yoganand.agenticenterpriseassistant.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PgVectorConfig {

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {

        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .database("agentic_assistant")
                .user("postgres")
                .password("Yoganand@30")
                .table("document_embeddings")
                .dimension(384)
                .build();
    }
}