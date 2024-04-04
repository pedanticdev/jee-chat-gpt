package fish.payara.views.main;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SearchCriteria {

    @NotNull
    private String city;
    @NotNull
    private BigDecimal budget;

    public static SearchCriteria of() {
        return new SearchCriteria();
    }
}
