package fish.payara.views.main;

import fish.payara.ai.PayaraDocsAiChat;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.omnifaces.cdi.ViewScoped;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Named
@ViewScoped
@Getter
@Setter
public class PayaraCloudChatBean implements Serializable {

    @Inject
    PayaraDocsAiChat payaraDocsAiChat;
    Pattern pattern;

    @PostConstruct
    void init() {
         pattern = Pattern.compile("(?i)\\b(hello|hi)\\b");
    }

    public String handleChat(String message, String[] params) {
        if (StringUtils.isNoneBlank(message) && pattern.matcher(message).find()) {
            return "Hello there! Welcome to Payara. How can I help you today?";
        }
        return payaraDocsAiChat.chat(message);
    }

}
