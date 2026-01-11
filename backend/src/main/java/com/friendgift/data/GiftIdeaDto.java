package com.friendgift.data;

public class GiftIdeaDto {
	public String id;
	public String text;
	public String createdAt;

	public GiftIdeaDto() {
	}

	public GiftIdeaDto(String id, String text, String createdAt) {
		this.id = id;
		this.text = text;
		this.createdAt = createdAt;
	}
}
