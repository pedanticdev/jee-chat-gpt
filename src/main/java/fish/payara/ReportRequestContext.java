package fish.payara;

import java.io.OutputStream;

import fish.payara.jpa.PointsOfInterestResponse;
import lombok.Getter;
import lombok.Setter;

import fish.payara.views.main.PointsOfInterestView;

@Getter
@Setter
public class ReportRequestContext {

	private PointsOfInterestView.SearchCriteria searchCriteria;
	private PointsOfInterestResponse response;
	private OutputStream outputStream;

}
