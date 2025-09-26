package org.uvhnael.ktal.service;

import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.FieldData;
import io.milvus.grpc.SearchResultData;
import io.milvus.grpc.SearchResults;
import io.milvus.param.ConnectParam;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class MilvusService {

    private MilvusClient client;
    private final EmbeddingService embeddingService;

    @Value("${milvus.host:localhost}")
    private String milvusHost;

    @Value("${milvus.port:19530}")
    private int milvusPort;

    @Value("${milvus.collection.name:chatbot_collection}")
    private String collectionName;

    // Default embedding dimension for all-MiniLM-L6-v2
    private static final int DEFAULT_EMBEDDING_DIM = 384;
    private int embeddingDim = DEFAULT_EMBEDDING_DIM;

    public MilvusService(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostConstruct
    public void init() {
        try {
            // Initialize Milvus client
            this.client = new MilvusServiceClient(
                    ConnectParam.newBuilder()
                            .withHost(milvusHost)
                            .withPort(milvusPort)
                            .build()
            );

            // Get embedding dimension from service if available
            if (embeddingService != null && embeddingService.isModelLoaded()) {
                this.embeddingDim = embeddingService.getEmbeddingDimension();
            }

            // Create collection if not exists
            createCollectionIfNotExists();

            log.info("Connected to Milvus at {}:{} with embedding dimension: {}",
                    milvusHost, milvusPort, embeddingDim);
        } catch (Exception e) {
            log.error("Failed to connect to Milvus", e);
            throw new RuntimeException("Cannot connect to Milvus", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (client != null) {
            try {
                client.close();
                log.info("Milvus client closed");
            } catch (Exception e) {
                log.warn("Error closing Milvus client: {}", e.getMessage());
            }
        }
    }

    private void createCollectionIfNotExists() {
        try {
            // Check if collection exists
            R<Boolean> hasCollection = client.hasCollection(
                    HasCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build()
            );

            if (hasCollection.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException("Failed to check collection existence: " + hasCollection.getMessage());
            }

            if (hasCollection.getData() != null && hasCollection.getData()) {
                log.info("Collection {} already exists", collectionName);
                loadCollection();
                return;
            }

            // Define schema with dynamic dimension
            List<FieldType> fields = Arrays.asList(
                    FieldType.newBuilder()
                            .withName("id")
                            .withDescription("Primary key")
                            .withDataType(io.milvus.grpc.DataType.VarChar)
                            .withMaxLength(65535)
                            .withPrimaryKey(true)
                            .withAutoID(false)
                            .build(),

                    FieldType.newBuilder()
                            .withName("text")
                            .withDescription("Original text content")
                            .withDataType(io.milvus.grpc.DataType.VarChar)
                            .withMaxLength(65535)
                            .build(),

                    FieldType.newBuilder()
                            .withName("embedding")
                            .withDescription("Text embedding vector")
                            .withDataType(io.milvus.grpc.DataType.FloatVector)
                            .withDimension(embeddingDim)
                            .build()
            );

            // Create collection
            R<RpcStatus> createResult = client.createCollection(
                    CreateCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withDescription("Chatbot knowledge base with DJL embeddings")
                            .withFieldTypes(fields)
                            .build()
            );

            if (createResult.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException("Failed to create collection: " + createResult.getMessage());
            }

            // Create index for vector field
            R<RpcStatus> indexResult = client.createIndex(
                    CreateIndexParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withFieldName("embedding")
                            .withIndexType(io.milvus.param.IndexType.IVF_FLAT)
                            .withMetricType(MetricType.COSINE)
                            .withExtraParam("{\"nlist\":1024}")
                            .build()
            );

            if (indexResult.getStatus() != R.Status.Success.getCode()) {
                log.warn("Failed to create index: {}", indexResult.getMessage());
            }

            // Load collection
            loadCollection();

            log.info("Successfully created collection: {} with embedding dimension: {}",
                    collectionName, embeddingDim);

        } catch (Exception e) {
            log.error("Error creating collection", e);
            throw new RuntimeException("Failed to create Milvus collection", e);
        }
    }

    private void loadCollection() {
        try {
            R<RpcStatus> loadResult = client.loadCollection(
                    LoadCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build()
            );

            if (loadResult.getStatus() != R.Status.Success.getCode()) {
                log.warn("Failed to load collection: {}", loadResult.getMessage());
            } else {
                log.info("Collection {} loaded successfully", collectionName);
            }
        } catch (Exception e) {
            log.warn("Error loading collection: {}", e.getMessage());
        }
    }

    public void insertEmbedding(String id, String text, float[] embedding) {
        try {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("ID cannot be null or empty");
            }

            if (text == null) {
                text = ""; // Allow empty text but not null
            }

            if (embedding == null || embedding.length != embeddingDim) {
                throw new IllegalArgumentException(
                        String.format("Embedding dimension mismatch. Expected: %d, got: %d",
                                embeddingDim, embedding != null ? embedding.length : 0)
                );
            }

            // Convert float[] to List<Float> - THIS IS THE KEY FIX
            List<Float> embeddingList = new ArrayList<>();
            for (float value : embedding) {
                embeddingList.add(value);
            }

            String idString = String.valueOf(id);


            List<InsertParam.Field> fields = Arrays.asList(
                    new InsertParam.Field("id", Collections.singletonList(idString)),  // PK duy nhất
                    new InsertParam.Field("text", Collections.singletonList(text)),
                    new InsertParam.Field("embedding", Collections.singletonList(embeddingList)) // Use List<Float> instead of float[]
            );

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(fields)
                    .build();

            R<io.milvus.grpc.MutationResult> result = client.insert(insertParam);

            if (result.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException("Insert failed: " + result.getMessage());
            }

            log.debug("Successfully inserted embedding with id: {}", id);

        } catch (Exception e) {
            log.error("Error inserting embedding for id: {}", id, e);
            throw new RuntimeException("Failed to insert embedding", e);
        }
    }

    public void deleteEmbedding(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                log.warn("Cannot delete embedding: ID is null or empty");
                return;
            }

            R<io.milvus.grpc.MutationResult> result = client.delete(
                    DeleteParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withExpr("id == \"" + id.trim() + "\"")
                            .build()
            );

            if (result.getStatus() != R.Status.Success.getCode()) {
                log.warn("Delete failed for id {}: {}", id, result.getMessage());
            } else {
                log.debug("Successfully deleted embedding with id: {}", id);
            }

        } catch (Exception e) {
            log.error("Error deleting embedding for id: {}", id, e);
        }
    }

    public List<SimilarityResult> searchSimilar(float[] embedding, int topK) {
        try {
            if (embedding == null || embedding.length != embeddingDim) {
                throw new IllegalArgumentException(
                        String.format("Embedding dimension mismatch. Expected: %d, got: %d",
                                embeddingDim, embedding != null ? embedding.length : 0)
                );
            }

            if (topK <= 0) {
                throw new IllegalArgumentException("TopK must be greater than 0");
            }

            // Convert float[] to List<Float> for search
            List<Float> embeddingList = new ArrayList<>();
            for (float value : embedding) {
                embeddingList.add(value);
            }

            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withMetricType(MetricType.COSINE)  // hoặc L2/IP tuỳ lúc tạo collection
                    .withTopK(topK)
                    .addOutField("text")
                    .addOutField("id")
                    .withVectorFieldName("embedding")   // 🔥 thêm dòng này
                    .withVectors(Collections.singletonList(embeddingList)) // Use List<Float>
                    .withParams("{\"nprobe\":10}")
                    .build();


            R<SearchResults> response = client.search(searchParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException("Search failed: " + response.getMessage());
            }

            if (response.getData() == null || response.getData().getResults() == null) {
                log.debug("Search returned no results");
                return Collections.emptyList();
            }

            return parseSearchResults(response.getData().getResults());

        } catch (Exception e) {
            log.error("Error searching similar embeddings", e);
            throw new RuntimeException("Failed to search similar content", e);
        }
    }

    private List<SimilarityResult> parseSearchResults(SearchResultData results) {
        List<SimilarityResult> similarityResults = new ArrayList<>();

        try {
            // Get scores
            List<Float> scores = results.getScoresList();

            // Get field data
            Map<String, List<String>> fieldData = new HashMap<>();

            for (FieldData field : results.getFieldsDataList()) {
                String fieldName = field.getFieldName();
                List<String> values = new ArrayList<>();

                if (field.getScalars().hasStringData()) {
                    values.addAll(field.getScalars().getStringData().getDataList());
                }

                fieldData.put(fieldName, values);
            }

            // Combine results
            List<String> ids = fieldData.getOrDefault("id", Collections.emptyList());
            List<String> texts = fieldData.getOrDefault("text", Collections.emptyList());

            int resultCount = Math.min(scores.size(), Math.min(ids.size(), texts.size()));

            for (int i = 0; i < resultCount; i++) {
                similarityResults.add(new SimilarityResult(
                        ids.get(i),
                        texts.get(i),
                        scores.get(i)
                ));
            }

            log.debug("Found {} similar results", similarityResults.size());

        } catch (Exception e) {
            log.error("Error parsing search results", e);
        }

        return similarityResults;
    }

    // Helper class for search results
    public static class SimilarityResult {
        private final String id;
        private final String text;
        private final float score;

        public SimilarityResult(String id, String text, float score) {
            this.id = id;
            this.text = text;
            this.score = score;
        }

        public String getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public float getScore() {
            return score;
        }

        @Override
        public String toString() {
            return String.format("SimilarityResult{id='%s', score=%.4f, text='%.50s...'}",
                    id, score, text.length() > 50 ? text.substring(0, 50) : text);
        }
    }

    // Getter for embedding dimension
    public int getEmbeddingDimension() {
        return embeddingDim;
    }
}