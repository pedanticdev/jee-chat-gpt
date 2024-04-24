package fish.payara.ai;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.List;

import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.bge.small.en.v15.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PayaraDocsAiChat {

	@Inject
	OpenAiChatModel model;

	@Inject
	@ConfigProperty(name = "OPEN_API_KEY")
	String apiKey;

	PayaraCloudChat payaraCloudChat;

	@PostConstruct
	void init() {
		Document document = FileSystemDocumentLoader.loadDocument("/opt/payara-cloud.pdf", new ApacheTikaDocumentParser());
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

		ingestor.ingest(document);

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


	}

	public String chat(String userMessage) {
		return payaraCloudChat.chat(userMessage);
	}

}
