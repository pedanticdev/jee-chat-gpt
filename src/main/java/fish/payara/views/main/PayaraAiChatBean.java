package fish.payara.views.main;

import fish.payara.ai.PayaraAiService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.bind.JsonbBuilder;
import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.omnifaces.cdi.ViewScoped;

@Named
@ViewScoped
@Getter
@Setter
@Log
public class PayaraAiChatBean implements Serializable {

    @Inject PayaraAiService payaraAiService;
    Pattern pattern;
    String userMessage;
    private List<MessageResponse> chatMessages = new ArrayList<>();

    Parser parser;
    HtmlRenderer renderer;

    @PostConstruct
    void init() {
        pattern = Pattern.compile("(?i)\\b(hello|hi)\\b");
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();
    }

    public void handleChat() {
        if (StringUtils.isNotBlank(userMessage)) {
            MessageResponse messageResponse = MessageResponse.of();
            messageResponse.setUserMessage(new String(userMessage) + "\n");
            if (pattern.matcher(userMessage).find()) {
                messageResponse.setModelMessage(
                        "Hello there! Welcome to Payara. How can I help you today?");
            } else {

                Node parse = parser.parse(payaraAiService.genericModelChat(userMessage));
                String render = renderer.render(parse);
                messageResponse.setModelMessage(render + "<br/>");
            }
            messageResponse.setModelResponseTime(ZonedDateTime.now(ZoneOffset.UTC));
            chatMessages.add(messageResponse);

            userMessage = null;
            log.log(Level.INFO, JsonbBuilder.create().toJson(messageResponse));
        }
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
            DateTimeFormatter dateTimeFormatter =
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);
            return dateTimeFormatter.format(chatTime);
        }

        public String getModelTime() {
            DateTimeFormatter dateTimeFormatter =
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
            return dateTimeFormatter.format(modelResponseTime);
        }

        private String userMessage;
        private String modelMessage;

        public static MessageResponse of() {
            return new MessageResponse();
        }
    }
}
