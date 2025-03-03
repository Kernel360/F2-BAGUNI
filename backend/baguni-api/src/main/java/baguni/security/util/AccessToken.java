package baguni.security.util;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import baguni.infra.model.user.Role;
import baguni.infra.model.util.IDToken;
import baguni.security.config.JwtProperties;
import baguni.security.exception.SecurityException;
import baguni.security.exception.AuthErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * VO for Access Token
 */
public class AccessToken {

	private final Jws<Claims> raw;

	public static final SignatureAlgorithm SIG_AL = SignatureAlgorithm.HS256;

	public static final Duration EXPIRY_DAY = Duration.ofDays(7);

	public Claims getClaims() {
		return raw.getBody();
	}

	public Role getUserRole() {
		return Role.valueOf(getClaims().get("role", String.class));
	}

	public IDToken getUserIdToken() {
		try {
			var raw = getClaims().get("id", String.class);
			return IDToken.fromString(raw);
		} catch (Exception e) {
			throw new SecurityException(AuthErrorCode.AUTH_INVALID_ID_TOKEN);
		}
	}

	public UsernamePasswordAuthenticationToken toAuthenticationToken() {
		return new UsernamePasswordAuthenticationToken(
			this.getUserIdToken(), this,
			List.of(new SimpleGrantedAuthority(this.getUserRole().name()))
		);
	}

	public static String makeNew(JwtProperties props, IDToken idToken, Role userRole) {
		var now = new Date();
		var expiration = new Date(now.getTime() + EXPIRY_DAY.toMillis());
		return Jwts
			.builder()
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.setIssuer(props.getIssuer())
			.setIssuedAt(now)
			.setExpiration(expiration)
			.claim("id", idToken.value())
			.claim("role", userRole)
			.signWith(SIG_AL, props.getSecret())
			.compact();
	}

	public static AccessToken fromString(JwtProperties props, String tokenRaw) {
		return new AccessToken(props, tokenRaw);
	}

	private AccessToken(JwtProperties props, String tokenRaw) throws SecurityException {
		try {
			this.raw = Jwts.parser()
						   .setSigningKey(props.getSecret())
						   .parseClaimsJws(tokenRaw);
		} catch (Exception e) {
			throw new SecurityException(AuthErrorCode.AUTH_INVALID_AUTHENTICATION);
		}
	}
}
