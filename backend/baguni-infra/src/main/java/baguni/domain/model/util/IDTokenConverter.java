package baguni.domain.model.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class IDTokenConverter implements AttributeConverter<IDToken, String> {

	@Override
	public String convertToDatabaseColumn(IDToken attribute) {
		return attribute != null ? attribute.value() : null;
	}

	@Override
	public IDToken convertToEntityAttribute(String dbData) {
		return IDToken.fromString(dbData);
	}
}
