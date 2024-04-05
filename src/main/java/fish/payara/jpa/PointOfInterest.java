package fish.payara.jpa;

import java.math.BigDecimal;

import jakarta.persistence.Entity;

import lombok.*;

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

	public static PointOfInterest of() {
		return new PointOfInterest();
	}

	@Override
	public String toString() {
		return "PointOfInterest [name=" + name + ", info=" + info + ", cost=" + cost + "]";
	}
}
