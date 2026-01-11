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
@Table(name = "gift_idea")
public class GiftIdea {
	@Id
	@Column(length = 36)
	public String id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "friend_id", nullable = false)
	public Friend friend;

	@Column(nullable = false, length = 400)
	public String text;

	@Column(nullable = false)
	public Instant createdAt;

	public GiftIdea() {
	}

	public GiftIdea(String id, Friend friend, String text, Instant createdAt) {
		this.id = id;
		this.friend = friend;
		this.text = text;
		this.createdAt = createdAt;
	}
}
