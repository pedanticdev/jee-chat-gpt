package fish.payara.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@NamedQuery(name = PointsOfInterestResponse.QUERY_NAME, query = "select p from PointsOfInterestResponse p where p.computedHashCode = :"
		+ PointsOfInterestResponse.PARAM_NAME)
@NamedQuery(name = RecipeSuggestion.QUERY_NAME, query = "select r from PointsOfInterestResponse r where r.computedHashCode in :"
		+ PointsOfInterestResponse.PARAM_NAME)
public class PointsOfInterestResponse extends AbstractEntity {

	public static final String QUERY_NAME = "PointsOfInterestResponse.getByKey";
	public static final String QUERY_NAME_ALL = "PointsOfInterestResponse.getAllByKey";

	public static final String PARAM_NAME = "paramName";

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<PointOfInterest> pointsOfInterest = new ArrayList<>();

	private String totalCostOfTrip;

	private String error;

	public BigDecimal getTotalCost() {
		return pointsOfInterest
				.stream()
				.map(PointOfInterest::getCost)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

}
