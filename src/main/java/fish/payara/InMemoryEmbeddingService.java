package fish.payara;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import fish.payara.ai.EmbeddingDocumentLoader;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
@Startup
public class InMemoryEmbeddingService {
    Logger log = Logger.getLogger(InMemoryEmbeddingService.class.getName());

    @Inject OpenAiChatModel model;

    @Inject
    @ConfigProperty(name = "OPEN_API_KEY")
    String apiKey;

    @Inject
    @ConfigProperty(name = "gpt.model")
    String gptModel;

    @Inject
    @ConfigProperty(name = "gpt.embedding.mode")
    String gptEmbeddingMode;

    @Inject
    @ConfigProperty(name = "openai.text-embedding")
    String textEmbedding;

    @Inject EmbeddingDocumentLoader documentLoader;

    EmbeddingModel embeddingModel;
    EmbeddingStore<TextSegment> embeddingStore;
    DocumentSplitter splitter;

    @PostConstruct
    void init() {
        //        embeddingStore = new InMemoryEmbeddingStore<>();
        //        public ResultSet doStatement(Connection conn) throws SQLException {
        //            conn = DriverManager
        //
        // .getConnection("jdbc:postgresql://eu-central-1.db.thenile.dev:5432/penta",
        // "01929084-15b1-7748-ad01-62421c778de0", "9867099d-f76e-4141-9d77-6a84f40d0f32");
        //            String query = "${COMMAND}";
        //            try (Statement stmt = conn.createStatement()) {
        //                return stmt.executeQuery(query);
        //            } catch (SQLException e) {
        //                throw e;
        //            }
        //        }
        embeddingStore =
                PgVectorEmbeddingStore.builder()
                        .host("db")
                        .port(5432)
                        .database("vectordb")
                        .user("vectoruser")
                        .password("vectorpass")
                        .table("embeddings")
                        .dimension(384)
                        .build();

        embeddingModel =
                OpenAiEmbeddingModel.builder()
                        .apiKey(apiKey)
                        .modelName(textEmbedding)
                        .logRequests(true)
                        .logResponses(true)
                        .build();

        //        embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

        splitter = DocumentSplitters.recursive(100, 0, new OpenAiTokenizer(gptEmbeddingMode));
        embedNewDocs();
    }

    @Schedule(minute = "*/1", hour = "*")
    public void embedNewDocs() {
        log.log(Level.INFO, "Starting document embedding");
        List<String> strings = documentLoader.listObjects();
        log.log(Level.INFO, "About to embed found objects {0}", strings);

        List<Document> documents = new ArrayList<>();
        List<String> embeddedObjects = new ArrayList<>();

        try {
            for (String string : strings) {
                documents.add(documentLoader.loadDocument(string));
                embeddedObjects.add(string);
            }
            List<TextSegment> textSegments = new ArrayList<>();
            for (Document document : documents) {
                textSegments.addAll(splitter.split(document));
                log.log(Level.INFO, "About to embed text segments {0}", textSegments);
                List<Embedding> embeddings = embeddingModel.embedAll(textSegments).content();

                log.log(Level.INFO, "About to embed {0}", embeddings);
                embeddingStore.addAll(embeddings, textSegments);
                EmbeddingStoreIngestor ingestor =
                        EmbeddingStoreIngestor.builder()
                                .documentSplitter(DocumentSplitters.recursive(300, 0))
                                .embeddingModel(embeddingModel)
                                .embeddingStore(embeddingStore)
                                .build();
                ingestor.ingest(documents);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "An error occurred while processing embeddings", e);
        }
        if (!embeddedObjects.isEmpty()) {
            log.log(Level.INFO, "Moving embedded objects {0}", embeddedObjects);
            documentLoader.moveEmbeddedDocument(embeddedObjects);
        }
    }

    public ContentRetriever getContentRetriever() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(1)
                .build();
    }

    public EmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }

    public EmbeddingStore<TextSegment> getEmbeddingStore() {
        return embeddingStore;
    }
}
