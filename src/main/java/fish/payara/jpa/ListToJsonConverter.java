package fish.payara.jpa;

import java.util.List;

import jakarta.enterprise.util.TypeLiteral;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ListToJsonConverter implements AttributeConverter<List<String>, String> {
	private static final Jsonb JSONB = JsonbBuilder.create();

	@Override
	public String convertToDatabaseColumn(List<String> attribute) {
		if (attribute != null && !attribute.isEmpty()) {
			return JSONB.toJson(attribute);
		}
		return "{}";
	}

	@Override
	public List<String> convertToEntityAttribute(String dbData) {
		if (dbData != null && !dbData.isBlank()) {
			return JSONB.fromJson(dbData, new TypeLiteral<List<String>>() {
			}.getType());
		}
		return List.of();
	}

}
