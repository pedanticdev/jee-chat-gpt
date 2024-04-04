package fish.payara.views.main;

import java.io.Serializable;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import lombok.Getter;
import lombok.Setter;

import org.apache.poi.util.StringUtil;
import org.omnifaces.cdi.ViewScoped;

import fish.payara.GptRequestContext;
import fish.payara.GptService;

@Named
@ViewScoped
@Getter
@Setter
public class ImageBean implements Serializable {

    @Inject
    GptService gptService;

    String imagePrompt;

    String generatedImage;

    public void generateImage() {
        if (StringUtil.isNotBlank(imagePrompt)) {
            GptRequestContext gptRequestContext = GptRequestContext.of();
            gptRequestContext.setPrompt(imagePrompt);

            generatedImage = gptService.generateImage(gptRequestContext);
            imagePrompt = null;
        }
    }



}
