package fish.payara.views.main;

import ai.djl.util.JsonUtils;
import fish.payara.ai.PayaraAiService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.omnifaces.cdi.ViewScoped;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

@Named
@ViewScoped
@Getter
@Setter
@Log
public class PayaraAiChatBean implements Serializable {

    @Inject
    PayaraAiService payaraAiService;
    Pattern pattern;
    String userMessage;
    private List<MessageResponse> chatMessages = new ArrayList<>();

    @PostConstruct
    void init() {
         pattern = Pattern.compile("(?i)\\b(hello|hi)\\b");
    }

    public String handleChat(String message, String[] params) {
        if (StringUtils.isNoneBlank(message) && pattern.matcher(message).find()) {
            return "Hello there! Welcome to Payara. How can I help you today?";
        }
        return payaraAiService.chat(message);
    }

    public void handleGeneralChat() {
        if (StringUtils.isNotBlank(userMessage)) {
            MessageResponse messageResponse = MessageResponse.of();
            messageResponse.setUserMessage(new String(userMessage) + "\n");
            messageResponse.setModelMessage(payaraAiService.generalChat(userMessage) + "\n\n");
            messageResponse.setModelResponseTime(ZonedDateTime.now(ZoneOffset.UTC));
            chatMessages.add(messageResponse);
            userMessage = null;

            log.log(Level.INFO, JsonbBuilder.create().toJson(messageResponse));
        }
    }

    @Getter
    @Setter
    public static class MessageResponse {
        private ZonedDateTime chatTime = ZonedDateTime.now(ZoneOffset.UTC);

        private ZonedDateTime modelResponseTime;

        public String getUserTime() {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
            return dateTimeFormatter.format(chatTime);
        }

        public String getModelTime() {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
            return dateTimeFormatter.format(modelResponseTime);
        }

        private String userMessage;
        private String modelMessage;

        public static MessageResponse of() {
            return new MessageResponse();
        }
    }

}
