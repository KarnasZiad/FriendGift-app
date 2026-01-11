package com.friendgift.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "friend")
public class Friend {
	@Id
	@Column(length = 36)
	public String id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_username", nullable = false)
	public AppUser owner;

	@Column(nullable = false, length = 80)
	public String name;

	@Column(nullable = false)
	public Instant createdAt;

	public Friend() {
	}

	public Friend(String id, AppUser owner, String name, Instant createdAt) {
		this.id = id;
		this.owner = owner;
		this.name = name;
		this.createdAt = createdAt;
	}
}
