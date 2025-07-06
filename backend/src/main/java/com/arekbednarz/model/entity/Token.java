package com.arekbednarz.model.entity;

import com.arekbednarz.enums.TokenType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "token", uniqueConstraints = { @UniqueConstraint(columnNames = "id") })
public @Data class Token {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Long id;

	@Column(name = "value", nullable = false)
	public String token;

	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	public TokenType tokenType = TokenType.BEARER;

	@Column(name = "revoked")
	public boolean revoked;

	@Column(name = "expired")
	public boolean expired;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	public User user;
}
