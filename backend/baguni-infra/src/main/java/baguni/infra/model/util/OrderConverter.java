package baguni.infra.model.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OrderConverter implements AttributeConverter<List<Long>, String> {

	@Override
	public String convertToDatabaseColumn(List<Long> idList) {
		if (idList == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Long id : idList) {
			sb.append(id).append(" ");
		}
		return sb.toString().trim();
	}

	@Override
	public List<Long> convertToEntityAttribute(String s) {
		List<Long> idList = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens()) {
			idList.add(Long.parseLong(st.nextToken()));
		}
		return idList;
	}
}
