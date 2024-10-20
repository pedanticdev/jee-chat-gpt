package fish.payara;

import dev.langchain4j.rag.content.retriever.ContentRetriever;

public interface EmbeddingService {

    ContentRetriever getContentRetriever();

    void embedNewDocs();
}
