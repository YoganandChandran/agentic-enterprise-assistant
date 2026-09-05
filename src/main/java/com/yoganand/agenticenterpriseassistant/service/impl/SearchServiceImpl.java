package com.yoganand.agenticenterpriseassistant.service.impl;

import com.yoganand.agenticenterpriseassistant.service.EmbeddingService;
import com.yoganand.agenticenterpriseassistant.service.SearchService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    private final EmbeddingService embeddingService;

    public SearchServiceImpl(
            EmbeddingService embeddingService
    ) {
        this.embeddingService = embeddingService;
    }

    @Override
    public List<String> search(String query) {

        return embeddingService.search(query);
    }
}