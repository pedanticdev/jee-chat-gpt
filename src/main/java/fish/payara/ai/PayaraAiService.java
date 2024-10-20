package fish.payara.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import fish.payara.EmbeddingService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Log
public class PayaraAiService {

    @Inject OpenAiChatModel model;

    @Inject
    @ConfigProperty(name = "open.api.key")
    String apiKey;

    @Inject EmbeddingService embeddingService;

    PayaraChat payaraCloudChat;
    GeneralPayaraChat generalPayaraChat;
    EmbeddingModel embeddingModel;
    EmbeddingStore<TextSegment> embeddingStore;

    @PostConstruct
    void init() {

        ContentRetriever contentRetriever = embeddingService.getContentRetriever();

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

        return payaraCloudChat.ask(question).content();
    }
}
