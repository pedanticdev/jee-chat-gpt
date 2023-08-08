package fish.payara.views.main;

import java.math.BigDecimal;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

public class DoubleToBigDecimalConverter implements Converter<Double, BigDecimal> {

	@Override
	public Result<BigDecimal> convertToModel(Double presentation, ValueContext valueContext) {
		return Result.ok(BigDecimal.valueOf(presentation));
	}

	@Override
	public Double convertToPresentation(BigDecimal model, ValueContext valueContext) {
		return model.doubleValue();
	}
}
