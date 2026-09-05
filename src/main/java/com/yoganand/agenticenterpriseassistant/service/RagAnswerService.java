package com.yoganand.agenticenterpriseassistant.service;

public interface RagAnswerService {

    String getAnswer(
            String userId,
            String question
    );

}