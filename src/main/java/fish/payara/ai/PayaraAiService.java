package fish.payara.ai;

import static java.util.stream.Collectors.joining;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import fish.payara.InMemoryEmbeddingService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Log
public class PayaraAiService {

    @Inject OpenAiChatModel model;

    @Inject
    @ConfigProperty(name = "open.api.key")
    String apiKey;

    @Inject InMemoryEmbeddingService inMemoryEmbeddingService;

    PayaraChat payaraCloudChat;
    GeneralPayaraChat generalPayaraChat;
    EmbeddingModel embeddingModel;
    EmbeddingStore<TextSegment> embeddingStore;

    @PostConstruct
    void init() {

        //        Document document =
        //                FileSystemDocumentLoader.loadDocument(
        //                        PayaraAiService.class.getResource("/payara-cloud.txt").getPath(),
        //                        new TextDocumentParser());
        //
        //        List<Document> documents = List.of(document);
        //
        //        // Split document into segments 100 tokens each
        //        DocumentSplitter splitter =
        //                DocumentSplitters.recursive(100, 0, new OpenAiTokenizer("gpt-3.5-turbo"));
        //        List<TextSegment> segments = splitter.split(document);
        //
        //        // Embed segments (convert them into vectors that represent the meaning) using
        // embedding
        //        // model
        //        embeddingModel =
        //                OpenAiEmbeddingModel.builder()
        //                        .apiKey(apiKey)
        //                        .modelName("text-embedding-3-small")
        //                        .logRequests(true)
        //                        .logResponses(true)
        //                        .build();
        //
        //        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        //
        //        // Store embeddings into embedding store for further search / retrieval
        //        embeddingStore = new InMemoryEmbeddingStore<>();
        //        embeddingStore.addAll(embeddings, segments);
        //
        //        EmbeddingStoreIngestor ingestor =
        //                EmbeddingStoreIngestor.builder()
        //                        .documentSplitter(DocumentSplitters.recursive(300, 0))
        //                        .embeddingModel(embeddingModel)
        //                        .embeddingStore(embeddingStore)
        //                        .build();
        //
        //        ingestor.ingest(documents);

        ContentRetriever contentRetriever = inMemoryEmbeddingService.getContentRetriever();

        payaraCloudChat =
                AiServices.builder(PayaraChat.class)
                        .chatLanguageModel(model)
                        .contentRetriever(contentRetriever)
                        .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                        .build();

        generalPayaraChat =
                AiServices.builder(GeneralPayaraChat.class)
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
        List<EmbeddingMatch<TextSegment>> relevantEmbeddings =
                embeddingStore.findRelevant(questionEmbedding, maxResults, minScore);

        // Create a prompt for the model that includes question and relevant embeddings
        PromptTemplate promptTemplate =
                PromptTemplate.from(
                        """
		You are a senior Java, Java EE, and Jakarta EE engineer with extensive experience in DevSecOps, Docker, Kubernetes, and Cloud Computing.\n
           Additionally, you have in-depth knowledge of cloud providers like AWS, Google Cloud, and Microsoft Azure.\n

Your primary role is to serve as a Product Marketer and Developer Advocate for Payara Services Ltd.\n
In this capacity, you will advise users based on your comprehensive expertise, providing guidance on Java, Jakarta EE, Payara Cloud and Payara Server.\n

When users inquire about technical aspects related to these technologies, you should provide detailed and insightful responses, leveraging the provided {{information}}.\n
However, if the user's question is more business-oriented, your answer should highlight the benefits and value propositions that align with their specific needs.\n

For instance, if a user asks, "Why should I use Payara Cloud?" your response should focus on the key business advantages they can expect, such as reduced cloud costs, streamlined DevOps processes, faster turnaround times, and enhanced scalability and flexibility.\n

If you are unsure how to respond to a user's query, you should default to suggesting they visit the Payara website at https://payara.fish for more comprehensive information and resources.\n

Throughout your interactions, maintain a professional and knowledgeable demeanor, aiming to provide users with valuable insights and guidance that empower them to make informed decisions aligning with their technical and business objectives.\n
Do NOT entertain questions, discussions or anything whatsoever outside of the above mentioned technologies.\n
		\n
		"Question: \n
		{{question}} \n
		\n
		Base your answer on the following information: \n
		{{information}}
""");

        String information =
                relevantEmbeddings.stream()
                        .map(match -> match.embedded().text())
                        .collect(joining("\n\n"));

        Map<String, Object> variables = new HashMap<>();

        variables.put("question", question);
        variables.put("information", information);

        Prompt prompt = promptTemplate.apply(variables);
        // Send the prompt to the OpenAI chat model

        AiMessage aiMessage =
                model.generate(
                                SystemMessage.systemMessage(PayaraChat.SYSTEM_MESSAGE),
                                prompt.toUserMessage())
                        .content();
        return payaraCloudChat.ask(question).content();
        //        return payaraCloudChat.chat(question);
        //        return aiMessage.text();
    }
}
