package org.uvhnael.ktal.service;

import ai.djl.Application;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);
    private static final int EMBEDDING_DIMENSION = 384; // all-MiniLM-L6-v2 dimension

    private ZooModel<String, float[]> model;
    private Predictor<String, float[]> predictor;
    private volatile boolean modelLoaded = false;

    @PostConstruct
    public void init() throws Exception {
        logger.info("Loading PyTorch embedding model: sentence-transformers/all-MiniLM-L6-v2");

        try {
            Criteria<String, float[]> criteria = Criteria.builder()
                    .setTypes(String.class, float[].class)
                    .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
                    .optEngine("PyTorch")
                    .optApplication(Application.NLP.TEXT_EMBEDDING)
                    .build();

            this.model = criteria.loadModel();
            this.predictor = model.newPredictor();
            this.modelLoaded = true;

            logger.info("PyTorch embedding model loaded successfully with dimension: {}", EMBEDDING_DIMENSION);

        } catch (Exception e) {
            logger.error("Failed to load PyTorch embedding model: {}", e.getMessage(), e);
            this.modelLoaded = false;
            throw e;
        }
    }

    public float[] generateEmbedding(String text) throws Exception {
        if (!modelLoaded || predictor == null) {
            throw new IllegalStateException("Embedding model is not initialized");
        }

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        try {
            float[] embedding = predictor.predict(text.trim());

            // Validate embedding dimension
            if (embedding.length != EMBEDDING_DIMENSION) {
                logger.warn("Unexpected embedding dimension: expected {}, got {}",
                        EMBEDDING_DIMENSION, embedding.length);
            }

            return embedding;
        } catch (Exception e) {
            logger.error("Failed to generate embedding for text: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean isModelLoaded() {
        return modelLoaded;
    }

    public int getEmbeddingDimension() {
        return EMBEDDING_DIMENSION;
    }

    @PreDestroy
    public void cleanup() {
        modelLoaded = false;

        if (predictor != null) {
            try {
                predictor.close();
                logger.info("Predictor closed");
            } catch (Exception e) {
                logger.warn("Error closing predictor: {}", e.getMessage());
            }
        }

        if (model != null) {
            try {
                model.close();
                logger.info("Model closed");
            } catch (Exception e) {
                logger.warn("Error closing model: {}", e.getMessage());
            }
        }
    }
}