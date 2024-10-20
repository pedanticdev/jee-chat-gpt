package fish.payara.ai;

import dev.langchain4j.data.document.Document;
import java.util.List;

public interface DocumentLoader {

    List<Document> loadDocuments();

    Document loadDocument(String documentKey);

    default void moveEmbeddedDocument(List<String> documentKeys) {}
}
