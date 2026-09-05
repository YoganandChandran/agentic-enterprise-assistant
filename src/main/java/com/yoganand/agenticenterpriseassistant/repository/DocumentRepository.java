package com.yoganand.agenticenterpriseassistant.repository;

import com.yoganand.agenticenterpriseassistant.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}