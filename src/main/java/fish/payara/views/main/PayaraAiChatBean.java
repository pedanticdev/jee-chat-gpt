package fish.payara.views.main;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.bind.JsonbBuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import org.apache.commons.lang3.StringUtils;
import org.omnifaces.cdi.ViewScoped;

import fish.payara.ai.PayaraAiService;

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

	public void handleChat() {

		if (StringUtils.isNotBlank(userMessage)) {
			MessageResponse messageResponse = MessageResponse.of();
			messageResponse.setUserMessage(new String(userMessage) + "\n");
			if (pattern.matcher(userMessage).find()) {
				messageResponse.setModelMessage("Hello there! Welcome to Payara. How can I help you today?");
			} else {

				messageResponse.setModelMessage(payaraAiService.genericModelChat(userMessage) + "\n\n");
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
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);
			return dateTimeFormatter.format(chatTime);
		}

		public String getModelTime() {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
			return dateTimeFormatter.format(modelResponseTime);
		}

		private String userMessage;
		private String modelMessage;

		public static MessageResponse of() {
			return new MessageResponse();
		}
	}

}
