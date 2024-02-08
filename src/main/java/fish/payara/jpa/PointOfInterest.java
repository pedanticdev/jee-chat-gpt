package fish.payara.jpa;

import jakarta.persistence.Entity;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class PointOfInterest extends AbstractEntity {
	private String name;
	private String info;
	private BigDecimal cost;
	private String formattedCost;

	@Override
	public String toString() {
		return "PointOfInterest [name=" + name + ", info=" + info + ", cost=" + cost + "]";
	}
}
