package fish.payara.views.main;

import fish.payara.GptRequestContext;
import fish.payara.ai.GptService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.util.StringUtil;
import org.omnifaces.cdi.ViewScoped;

@Named
@ViewScoped
@Getter
@Setter
public class ImageBean implements Serializable {

    @Inject GptService gptService;

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
