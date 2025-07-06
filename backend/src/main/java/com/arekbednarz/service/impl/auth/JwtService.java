package com.arekbednarz.service.impl.auth;

import com.arekbednarz.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService {
	private static final Logger LOG = Logger.getLogger(JwtService.class);

	@Value("${application.security.jwt.expiration}")
	private long jwtExpiration;

	@Value("${application.security.refresh-token.expiration}")
	private long refreshExpiration;

	@Value("${application.security.refresh-token.secret-key}")
	private String secretKey;

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(((User)userDetails).getEmail())) && !isTokenExpired(token);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String generateToken(UserDetails userDetails) {
		LOG.infof("Generating a new token for %s", userDetails.getUsername());
		return generateToken(Map.of("role",((User)userDetails).getRole().name()), userDetails);
	}

	public String generateRefreshToken(
		UserDetails userDetails) {
		LOG.infof("Generating a new refresh token for %s", userDetails.getUsername());
		return buildToken(new HashMap<>(), userDetails, refreshExpiration);
	}

	private String generateToken(
		Map<String, Object> extraClaims,
		UserDetails userDetails) {
		return buildToken(extraClaims, userDetails, jwtExpiration);
	}

	private String buildToken(
		Map<String, Object> extraClaims,
		UserDetails userDetails,
		long expiration) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expiration);

		return Jwts.builder()
			.claims().add(extraClaims).and()
			.subject(((User)userDetails).getEmail())
			.issuedAt(now)
			.expiration(expiry)
			.signWith(getSignInKey(), Jwts.SIG.HS256)
			.compact();
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
			.verifyWith(getSignInKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private SecretKey getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
