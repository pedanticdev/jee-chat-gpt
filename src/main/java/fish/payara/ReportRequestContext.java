package fish.payara;

import java.io.OutputStream;

import fish.payara.views.main.SearchCriteria;
import lombok.Getter;
import lombok.Setter;

import fish.payara.jpa.PointsOfInterestResponse;

@Getter
@Setter
public class ReportRequestContext {

	private SearchCriteria searchCriteria;
	private PointsOfInterestResponse response;
	private OutputStream outputStream;

}
