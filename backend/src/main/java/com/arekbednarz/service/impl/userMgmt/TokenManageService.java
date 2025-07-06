package com.arekbednarz.service.impl.userMgmt;

import com.arekbednarz.dto.tokenMgmt.TokenDto;
import com.arekbednarz.dto.tokenMgmt.TokenUpdateDto;
import com.arekbednarz.enums.TokenType;
import com.arekbednarz.enums.TokenUpdateAction;
import com.arekbednarz.exception.EntityCreationException;
import com.arekbednarz.exception.EntityNotFoundException;
import com.arekbednarz.model.entity.Token;
import com.arekbednarz.model.repository.TokenRepository;
import com.arekbednarz.service.IManageService;
import io.vavr.control.Option;
import jakarta.ws.rs.BadRequestException;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service("tokenCreationService")
public class TokenManageService implements IManageService {
	private static final Logger LOG = Logger.getLogger(TokenManageService.class);

	private final TokenRepository tokenRepository;

	@Autowired
	public TokenManageService(TokenRepository tokenRepository) {
		this.tokenRepository = tokenRepository;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getAll() {
		return (List<T>) tokenRepository.findAll();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, S> T create(S dto) {
		var tokenCreationDto = dtoParser(dto, TokenDto.class);
		try {
			return (T) tokenRepository.save(creatTokenObject(tokenCreationDto));
		} catch (Exception e) {
			LOG.errorf("Error creating token: %s", e.getMessage());
			throw new EntityCreationException("Token creation failed, caused by: " + e.getMessage());
		}
	}

	@Override
	public <S> void delete(S value) {
		throw new BadRequestException("Not implemented yet");
	}

	@Override
	@Transactional
	public <T, S> T update(S dto) {
		var tokenUpdateDto = dtoParser(dto, TokenUpdateDto.class);
		if (tokenUpdateDto.action().contains(TokenUpdateAction.REVOKE)) {

			if (tokenUpdateDto.tokenValue() != null) {
				LOG.infof("Revoking token: %s", tokenUpdateDto.tokenValue());
				revokeOneUserTokens(tokenUpdateDto.tokenValue());

			}
			if (tokenUpdateDto.userId() != null) {
				LOG.infof("Revoking tokens assigned to user : %s", tokenUpdateDto.userId());
				revokeAllUserTokens(tokenUpdateDto.userId());
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, S> T getOne(S value) {
		var tokenValue = dtoParser(value, String.class);
		return (T) getTokenByValue(tokenValue);
	}

	private void revokeAllUserTokens(final Long userId) {
		var validUserTokens = getTokensByUserId(userId);
		if (validUserTokens.isEmpty())
			return;
		validUserTokens.forEach(token -> {
			token.setExpired(true);
			token.setRevoked(true);
		});
		tokenRepository.saveAll(validUserTokens);
	}

	private void revokeOneUserTokens(final String token) {
		var validUserToken = getTokenByValue(token);
		validUserToken.setExpired(true);
		validUserToken.setRevoked(true);
		tokenRepository.save(validUserToken);
	}

	private List<Token> getTokensByUserId(final Long userId) {
		return Option.of(tokenRepository.findAllValidTokenByUser(userId))
			.filter(Objects::nonNull)
			.getOrElseThrow(() -> new EntityNotFoundException(Token.class, "userId", userId.toString()));
	}

	private Token creatTokenObject(final TokenDto tokenDto) {
		return Token.builder()
			.user(tokenDto.getUser())
			.token(tokenDto.getToken())
			.tokenType(TokenType.BEARER)
			.expired(false)
			.revoked(false)
			.build();
	}

	private Token getTokenByValue(final String tokenValue) {
		return tokenRepository.findByToken(tokenValue).orElseThrow(() -> new EntityNotFoundException(Token.class, "value", tokenValue));
	}

}
