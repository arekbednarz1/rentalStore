package com.arekbednarz.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;


public @Data class ErrorDto {

	private ErrorMessageDto errors;

	public ErrorMessageDto withError(final String type, final String title, final String status, final String details, final String instance) {
		return new ErrorMessageDto(type, title, status, details, instance);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@AllArgsConstructor
	public static @Data class ErrorMessageDto {

		@NotNull
		@JsonProperty("type")
		private final String type;

		@NotNull
		@JsonProperty("title")
		private final String title;

		@NotNull
		@JsonProperty("status")
		private final String status;

		@NotNull
		@JsonProperty("detail")
		private final String message;

		@JsonProperty("object_error")
		private String objectError;

		@JsonProperty("object_error_details")
		private Map<String, List<String>> objectErrorDetails;

		@NotNull
		@JsonProperty("instance")
		private final String instance;

		public ErrorMessageDto(String type, String title, String status, String message, String instance) {
			this.type = type;
			this.title = title;
			this.status = status;
			this.message = message;
			this.instance = instance;
		}
	}
}
