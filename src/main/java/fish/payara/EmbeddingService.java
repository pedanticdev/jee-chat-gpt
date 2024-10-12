package fish.payara;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
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
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Stateless
public class EmbeddingService {
    Logger log = Logger.getLogger(EmbeddingService.class.getName());

    @Inject OpenAiChatModel model;

    @Inject
    @ConfigProperty(name = "OPEN_API_KEY")
    String apiKey;

    @Inject
    @ConfigProperty(name = "gpt.model")
    String gptModel;

    @Inject
    @ConfigProperty(name = "openai.text-embedding")
    String textEmbedding;

    @Inject S3BucketManager s3BucketManager;

    EmbeddingModel embeddingModel;
    EmbeddingStore<TextSegment> embeddingStore;
    DocumentSplitter splitter;

    @PostConstruct
    void init() {
        embeddingStore = new InMemoryEmbeddingStore<>();
        // embeddingStore = OpenSearchEmbeddingStore
        // .builder()
        // .serverUrl("opensearch.mi-sika.com")
        // .userName("admin")
        // .password("AVG!bq**wN2kyN*Vn!ynYgufMx*o7b")
        // .build();
        embeddingModel =
                OpenAiEmbeddingModel.builder()
                        .apiKey(apiKey)
                        .modelName(textEmbedding)
                        .logRequests(true)
                        .logResponses(true)
                        .build();

        splitter = DocumentSplitters.recursive(100, 0, new OpenAiTokenizer(gptModel));
    }

    @Schedule(minute = "*/1", hour = "*")
    public void embedNewDocs() {
        log.log(Level.INFO, "Starting document embedding");
        List<String> strings = s3BucketManager.listNewObjects();
        log.log(Level.INFO, "About to embed found objects {0}", strings);

        List<Document> documents = new ArrayList<>();
        List<String> embeddedObjects = new ArrayList<>();
        try {
            for (String string : strings) {
                Path newFilePath = s3BucketManager.getNewFilePath(string);
                if (newFilePath != null) {
                    documents.add(FileSystemDocumentLoader.loadDocument(newFilePath));
                    embeddedObjects.add(string);
                    Files.deleteIfExists(newFilePath);
                }
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
            s3BucketManager.moveEmbeddedFiles(embeddedObjects);
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
