package fish.payara.ai;

import static java.util.stream.Collectors.joining;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.langchain4j.data.message.SystemMessage;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import lombok.extern.java.Log;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
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
	EmbeddingModel embeddingModel;
	EmbeddingStore<TextSegment> embeddingStore;

	@PostConstruct
	void init() {

		Document document = FileSystemDocumentLoader.loadDocument("/opt/docs/payara-cloud.txt",
				new TextDocumentParser());

		List<Document> documents = List.of(document);

		// Split document into segments 100 tokens each
		DocumentSplitter splitter = DocumentSplitters.recursive(
				100,
				0,
				new OpenAiTokenizer("gpt-3.5-turbo"));
		List<TextSegment> segments = splitter.split(document);

		// Embed segments (convert them into vectors that represent the meaning) using embedding model
		embeddingModel = OpenAiEmbeddingModel
				.builder()
				.apiKey(apiKey)
				.modelName("text-embedding-3-small")
				.logRequests(true)
				.logResponses(true)
				.build();

		List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

		// Store embeddings into embedding store for further search / retrieval
		embeddingStore = new InMemoryEmbeddingStore<>();
		embeddingStore.addAll(embeddings, segments);

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

		return payaraCloudChat.chat(userMessage) + "\n";
	}

	public String generalChat(String message) {
		Response<AiMessage> chat = generalPayaraChat.chat(message);

		return chat.content().text();
	}

	public String genericModelChat(String question) {
		// Specify the question you want to ask the model

		// Embed the question
		Embedding questionEmbedding = embeddingModel.embed(question).content();

		// Find relevant embeddings in embedding store by semantic similarity
		// You can play with parameters below to find a sweet spot for your specific use case
		int maxResults = 3;
		double minScore = 0.7;
		List<EmbeddingMatch<TextSegment>> relevantEmbeddings = embeddingStore.findRelevant(questionEmbedding,
				maxResults, minScore);

		// Create a prompt for the model that includes question and relevant embeddings
		PromptTemplate promptTemplate = PromptTemplate.from(
				"""
					Answer the following question to the best of your ability: \n
					\n
					"Question: \n
					{{question}} \n
					\n
					Base your answer on the following information: \n
					{{information}}
				""");

		String information = relevantEmbeddings.stream()
				.map(match -> match.embedded().text())
				.collect(joining("\n\n"));

		Map<String, Object> variables = new HashMap<>();
		variables.put("question", question);
		variables.put("information", information);

		Prompt prompt = promptTemplate.apply(variables);
		// Send the prompt to the OpenAI chat model

		AiMessage aiMessage = model.generate(SystemMessage.systemMessage(PayaraCloudChat.SYSTEM_MESSAGE), prompt.toUserMessage()).content();
		return aiMessage.text();
	}

}
