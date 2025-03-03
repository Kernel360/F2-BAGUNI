package baguni.domain.model.util;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/**
 * VO for ID Token
 */
@Getter
public class IDToken {

	private final UUID uuid;

	@JsonCreator
	public static IDToken fromString(@JsonProperty("uuid") String raw) throws IdTokenConversionException {
		try {
			var uuid = UUID.fromString(raw);
			return new IDToken(uuid);
		} catch (Exception e) {
			throw new IdTokenConversionException(raw + ": ID 토큰의 값이 UUID 가 아닙니다!");
		}
	}

	public static IDToken makeNew() {
		return new IDToken(UUID.randomUUID());
	}

	public String value() {
		return this.uuid.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof IDToken token)) {
			return false;
		}
		return this.uuid.compareTo(token.uuid) == 0;
	}

	private IDToken(UUID uuid) {
		this.uuid = uuid;
	}
}
