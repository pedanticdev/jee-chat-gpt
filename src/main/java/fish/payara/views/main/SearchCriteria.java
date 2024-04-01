package fish.payara.views.main;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SearchCriteria {
    private String city;
    private BigDecimal budget;

    public static SearchCriteria of() {
        return new SearchCriteria();
    }
}
