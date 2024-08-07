package com.example.livealone.user.entity;

import com.example.livealone.global.entity.Timestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User extends Timestamp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String username;

	private String nickname;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserRole role = UserRole.USER;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Social social;

	private LocalDate birthDay;

	private String address;

	@Builder
	public User(String username, String nickname, String email, Social social, LocalDate birthDay, String address) {
		this.username = username;
		this.nickname = nickname;
		this.email = email;
		this.social = social;
		this.birthDay = birthDay;
		this.address = address;
	}

	public void updateUser(String nickname, LocalDate birthDay, String address) {
		this.nickname = nickname;
		this.birthDay = birthDay;
		this.address = address;
	}

	public void registerAdmin() {
		this.role = UserRole.ADMIN;
	}
}
