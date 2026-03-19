package com.capstone.pronunciation.domain.user.entity;

import jakarta.persistence.*;

@Table(name = "users",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_users_email", columnNames = {"email"})
		})
@Entity
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String email;

	// 비밀번호 원문이 아니라 해시(암호화 결과)만 저장.
	@Column(nullable = false, length = 255)
	private String password;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, columnDefinition = "integer default 1")
	private int level;

	protected User() {
	}

	public User(String email, String password, String name, int level) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.level = level;
	}

	@PrePersist
	protected void applyDefaultLevel() {
		if (level <= 0) {
			level = 1;
		}
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}
