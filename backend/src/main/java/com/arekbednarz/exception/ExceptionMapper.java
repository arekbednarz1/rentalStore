package com.arekbednarz.exception;

import com.arekbednarz.dto.ErrorDto;
import jakarta.ws.rs.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static com.arekbednarz.exception.ExceptionMapper.MapperValues.INCORRECT_VALUE;


@ControllerAdvice
public class ExceptionMapper extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = { BadRequestException.class })
	protected ResponseEntity<Object> handleConflict(BadRequestException e, WebRequest request) {
		String URI = "uri=";
		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(
				new ErrorDto().withError(INCORRECT_VALUE.getValue(),
					HttpStatus.BAD_REQUEST.getReasonPhrase(),
					String.valueOf(HttpStatus.BAD_REQUEST.value()),
					e.getMessage(),
					request.getDescription(false).replace(URI, StringUtils.EMPTY)));
	}

	@AllArgsConstructor
	@Getter
	enum MapperValues {
		INCORRECT_VALUE("about:incorrect");
		private String value;
	}
}
