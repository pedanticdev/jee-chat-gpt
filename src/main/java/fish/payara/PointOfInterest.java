package fish.payara;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointOfInterest implements Serializable {
	private String name;
	private String info;
	private BigDecimal cost;
	private String formattedCost;

	@Override
	public String toString() {
		return "PointOfInterest [name=" + name + ", info=" + info + ", cost=" + cost + "]";
	}
}
