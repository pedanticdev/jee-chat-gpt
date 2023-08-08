package fish.payara;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointsOfInterestResponse implements Serializable {
	private List<PointOfInterest> pointsOfInterest;
	private String totalCostOfTrip;
	private String error;

	public BigDecimal getTotalCost() {
		return pointsOfInterest
				.stream()
				.map(PointOfInterest::getCost)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

}
