package com.friendgift.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class AppUser {
	@Id
	@Column(length = 32)
	public String username;

	@Column(nullable = false, length = 72)
	public String password;

	public AppUser() {
	}

	public AppUser(String username, String password) {
		this.username = username;
		this.password = password;
	}
}
