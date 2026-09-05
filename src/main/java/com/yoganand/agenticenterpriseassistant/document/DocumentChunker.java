package com.yoganand.agenticenterpriseassistant.document;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentChunker {

    public List<TextSegment> split(Document document) {

        return DocumentSplitters
                .recursive(500, 50)
                .split(document);
    }
}
