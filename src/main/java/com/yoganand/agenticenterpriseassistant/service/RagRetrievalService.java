package com.yoganand.agenticenterpriseassistant.service;

import com.yoganand.agenticenterpriseassistant.dto.RagRetrievalResponse;

public interface RagRetrievalService {

    RagRetrievalResponse retrieve(String query);
}