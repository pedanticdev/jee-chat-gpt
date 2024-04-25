package fish.payara.ai;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

@ApplicationScoped
@Log
public class PayaraAiService {

	@Inject
	OpenAiChatModel model;

	@Inject
	@ConfigProperty(name = "OPEN_API_KEY")
	String apiKey;

	PayaraCloudChat payaraCloudChat;
	GeneralPayaraChat generalPayaraChat;

	@PostConstruct
	void init() {
		// List<Document> documents = FileSystemDocumentLoader.loadDocuments("/opt/docs", new
		// ApacheTikaDocumentParser());
//		Document document = FileSystemDocumentLoader.loadDocument("/opt/docs/payara-cloud.pdf");
		Document document2 = FileSystemDocumentLoader.loadDocument("/opt/docs/payara-cloud-future-proofing.pdf");
		Document document3 = FileSystemDocumentLoader.loadDocument("/opt/docs/payara-cloud-github.pdf");
		Document document4 = FileSystemDocumentLoader.loadDocument("/opt/docs/payara-cloud-neon-postgres.pdf");
		Document document5 = FileSystemDocumentLoader.loadDocument("/opt/docs/payara-cloud-scalability.pdf");
		Document document6 = FileSystemDocumentLoader.loadDocument("/opt/docs/payara-cloud-troubleshooting.pdf");
		List<Document> documents = List.of(document2, document4, document3,document5, document6);
		InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
		EmbeddingModel embeddingModel = OpenAiEmbeddingModel
				.builder()
				.apiKey(apiKey)
				.modelName("text-embedding-3-small")
				.logRequests(true)
				.logResponses(true)
				.build();

		EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
				.documentSplitter(DocumentSplitters.recursive(300, 0))
				.embeddingModel(embeddingModel)
				.embeddingStore(embeddingStore)
				.build();

		ingestor.ingest(documents);

		ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
				.embeddingStore(embeddingStore)
				.embeddingModel(embeddingModel)
				.maxResults(1)
				.build();
		payaraCloudChat = AiServices.builder(PayaraCloudChat.class)
				.chatLanguageModel(model)
				.contentRetriever(contentRetriever)
				.chatMemory(MessageWindowChatMemory.withMaxMessages(10))
				.build();

		generalPayaraChat = AiServices.builder(GeneralPayaraChat.class)
				.chatLanguageModel(model)
				.chatMemory(MessageWindowChatMemory.withMaxMessages(10))
				.build();
	}

	public String chat(String userMessage) {

		return payaraCloudChat.chat(userMessage)+"\n";
	}

	public String generalChat(String message) {
		Response<AiMessage> chat = generalPayaraChat.chat(message);

		return chat.content().text();
	}


}
