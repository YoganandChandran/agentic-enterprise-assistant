package com.yoganand.agenticenterpriseassistant.controller;

import com.yoganand.agenticenterpriseassistant.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<String>> search(
            @RequestParam String query
    ) {

        return ResponseEntity.ok(
                searchService.search(query)
        );
    }
}