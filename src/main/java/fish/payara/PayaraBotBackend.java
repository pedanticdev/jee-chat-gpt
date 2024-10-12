package fish.payara;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import fish.payara.ai.PayaraAiService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.io.IOException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.omnifaces.cdi.Startup;

@Startup
public class PayaraBotBackend {

    @Inject
    @ConfigProperty(name = "TELEGRAM_BOT_KEY")
    String telegramBotKey;

    @Inject PayaraAiService aiService;

    TelegramBot bot;

    @PostConstruct
    void init() {
        //		 bot = new TelegramBot(telegramBotKey);
        //		 bot.setUpdatesListener(updates -> {
        //
        //		 for (Update update : updates) {
        //
        //		 updateAction(update);
        //
        //		 }
        //		 return UpdatesListener.CONFIRMED_UPDATES_ALL;
        //		 });

    }

    private void sendMessage(SendMessage sendMessage) {

        bot.execute(
                sendMessage,
                new Callback<SendMessage, SendResponse>() {
                    @Override
                    public void onResponse(SendMessage request, SendResponse response) {}

                    @Override
                    public void onFailure(SendMessage request, IOException e) {}
                });
    }

    private void updateAction(Update update) {

        initialGreetingResponse(update);

        companyUpdateCallback(update);
        productsUpdateCallback(update);
        serverUpdateCallback(update);
        contactUpdateCallback(update);
        aiChat(update);
    }

    private void initialGreetingResponse(Update update) {
        if (update.message() != null && update.message().text() != null) {
            if (isHelloHiGreeting(update.message().text())) {

                InlineKeyboardMarkup inlineKeyboard =
                        new InlineKeyboardMarkup(
                                new InlineKeyboardButton("Products").callbackData("products"),
                                new InlineKeyboardButton("Company").callbackData("company"),
                                new InlineKeyboardButton("Contact").callbackData("contact"));

                SendMessage request =
                        new SendMessage(
                                        update.message().chat().id(),
                                        "Hello there. Welcome to Payara. What can I do for you"
                                                + " today?")
                                .parseMode(ParseMode.HTML)
                                .disableWebPagePreview(true)
                                .disableNotification(true)
                                .replyMarkup(inlineKeyboard);

                sendMessage(request);
            }
        }
    }

    private void contactUpdateCallback(Update update) {
        if (update.message() == null
                && update.callbackQuery() != null
                && "contact".equalsIgnoreCase(update.callbackQuery().data())) {
            InlineKeyboardMarkup inlineKeyboard =
                    new InlineKeyboardMarkup(
                            new InlineKeyboardButton("Contact Us")
                                    .url("https://www.payara.fish/about/contact-us/"));

            SendMessage request =
                    new SendMessage(
                                    update.callbackQuery().maybeInaccessibleMessage().chat().id(),
                                    "We'll be more than happy to hear from you. Don't have anything"
                                            + " to say? Just say hi \uD83D\uDE42")
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(true)
                            .disableNotification(true)
                            .replyMarkup(inlineKeyboard);
            sendMessage(request);
        }
    }

    private void companyUpdateCallback(Update update) {
        if (update.message() == null
                && update.callbackQuery() != null
                && "company".equalsIgnoreCase(update.callbackQuery().data())) {
            System.out.println(update.callbackQuery().toString());
            InlineKeyboardMarkup inlineKeyboard =
                    new InlineKeyboardMarkup(
                            new InlineKeyboardButton("More About Payara")
                                    .url("https://www.payara.fish/about/"),
                            new InlineKeyboardButton("Join Us")
                                    .url("https://www.payara.fish/join-us/"));

            SendMessage request =
                    new SendMessage(
                                    update.callbackQuery().maybeInaccessibleMessage().chat().id(),
                                    "Payara Services is the globally distributed company behind the"
                                        + " open source Payara Server Jakarta EE and MicroProfile"
                                        + " Runtime. Payara deeply believes enterprises building"
                                        + " solutions on the Jakarta EE Platform deserve a modern,"
                                        + " highly optimized runtime that saves them money, time"
                                        + " and helps them delight their users. So we strive to"
                                        + " deliver that to our users.")
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(true)
                            .disableNotification(true)
                            .replyMarkup(inlineKeyboard);
            sendMessage(request);
        }
    }

    private void serverUpdateCallback(Update update) {
        if (update.message() == null
                && update.callbackQuery() != null
                && "server".equalsIgnoreCase(update.callbackQuery().data())) {
            System.out.println(update.callbackQuery().toString());
            InlineKeyboardMarkup inlineKeyboard =
                    new InlineKeyboardMarkup(
                            new InlineKeyboardButton("Read More")
                                    .url("https://www.payara.fish/products/payara-server/"),
                            new InlineKeyboardButton("Download A Trial")
                                    .url(
                                            "https://www.payara.fish/page/payara-enterprise-downloads/"),
                            new InlineKeyboardButton("Speak With Sales")
                                    .url("https://www.payara.fish/about/contact-us/"));

            SendMessage request =
                    new SendMessage(
                                    update.callbackQuery().maybeInaccessibleMessage().chat().id(),
                                    "Payara Server Enterprise is our fully supported, production"
                                        + " ready Jakarta EE and MicroProfile runtime for deploying"
                                        + " modern applications.")
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(true)
                            .disableNotification(true)
                            .replyMarkup(inlineKeyboard);
            sendMessage(request);
        }
    }

    private void productsUpdateCallback(Update update) {
        if (update.message() == null
                && update.callbackQuery() != null
                && "products".equalsIgnoreCase(update.callbackQuery().data())) {

            System.out.println(update.callbackQuery().toString());
            InlineKeyboardMarkup inlineKeyboard =
                    new InlineKeyboardMarkup(
                            new InlineKeyboardButton("Payara Server Enterprise")
                                    .callbackData("server"),
                            new InlineKeyboardButton("Payara Cloud").callbackData("cloud"),
                            new InlineKeyboardButton("Payara Server Community")
                                    .callbackData("community"));

            SendMessage request =
                    new SendMessage(
                                    update.callbackQuery().maybeInaccessibleMessage().chat().id(),
                                    "Which of these products would you be interested in??")
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(true)
                            .disableNotification(true)
                            .replyMarkup(inlineKeyboard);
            sendMessage(request);
        }
    }

    private void aiChat(Update update) {
        if (update.message() != null && !isHelloHiGreeting(update.message().text())) {
            String generalChat = aiService.generalChat(update.message().text());
            SendMessage request =
                    new SendMessage(update.message().chat().id(), generalChat)
                            .parseMode(ParseMode.HTML)
                            .disableWebPagePreview(true)
                            .disableNotification(true);
            // @payarafishbot
            sendMessage(request);
        }
    }

    private boolean isHelloHiGreeting(String text) {
        return "Hi".equalsIgnoreCase(text) || "Hello".equalsIgnoreCase(text);
    }
}
