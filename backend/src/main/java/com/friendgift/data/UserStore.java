package com.friendgift.data;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserStore {
	private final Map<String, String> passwordsByUsername = new LinkedHashMap<>();
	private final Map<String, List<FriendRecord>> friendsByUsername = new LinkedHashMap<>();

	public UserStore() {
		seedData();
	}

	public boolean isValidCredentials(String username, String password) {
		if (username == null || password == null) {
			return false;
		}
		return password.equals(passwordsByUsername.get(username));
	}

	public boolean registerUser(String username, String password) {
		return registerUserDetailed(username, password) == RegistrationOutcome.CREATED;
	}

	public RegistrationOutcome registerUserDetailed(String username, String password) {
		String u = normalizeUsername(username);
		String p = normalizePassword(password);
		if (u == null || p == null) {
			return RegistrationOutcome.INVALID;
		}
		if (passwordsByUsername.containsKey(u)) {
			return RegistrationOutcome.EXISTS;
		}

		passwordsByUsername.put(u, p);
		friendsByUsername.putIfAbsent(u, new ArrayList<>());
		return RegistrationOutcome.CREATED;
	}

	public enum RegistrationOutcome {
		CREATED,
		EXISTS,
		INVALID
	}

	public List<FriendDto> listFriends(String username) {
		return friendsByUsername.getOrDefault(username, List.of()).stream()
				.map(fr -> new FriendDto(fr.id, fr.name))
				.toList();
	}

	public Optional<FriendDto> addFriend(String username, String name) {
		String clean = normalizeName(name);
		if (clean == null) {
			return Optional.empty();
		}

		FriendRecord record = new FriendRecord(UUID.randomUUID().toString(), clean);
		friendsByUsername.computeIfAbsent(username, ignored -> new ArrayList<>()).add(record);
		return Optional.of(new FriendDto(record.id, record.name));
	}

	public Optional<FriendDto> updateFriend(String username, String friendId, String name) {
		String clean = normalizeName(name);
		if (clean == null) {
			return Optional.empty();
		}

		Optional<FriendRecord> friend = findFriend(username, friendId);
		if (friend.isEmpty()) {
			return Optional.empty();
		}

		friend.get().name = clean;
		return Optional.of(new FriendDto(friend.get().id, friend.get().name));
	}

	public boolean deleteFriend(String username, String friendId) {
		List<FriendRecord> friends = friendsByUsername.get(username);
		if (friends == null || friends.isEmpty()) {
			return false;
		}
		return friends.removeIf(fr -> fr.id.equals(friendId));
	}

	public Optional<FriendRecord> findFriend(String username, String friendId) {
		return friendsByUsername.getOrDefault(username, List.of()).stream()
				.filter(fr -> fr.id.equals(friendId))
				.findFirst();
	}

	public List<GiftIdeaDto> listIdeas(String username, String friendId) {
		return findFriend(username, friendId)
				.map(fr -> fr.ideas.stream()
						.map(ir -> new GiftIdeaDto(ir.id, ir.text, ir.createdAt.toString()))
						.toList())
				.orElse(List.of());
	}

	public Optional<GiftIdeaDto> addIdea(String username, String friendId, String text) {
		if (text == null || text.trim().isEmpty()) {
			return Optional.empty();
		}

		Optional<FriendRecord> friend = findFriend(username, friendId);
		if (friend.isEmpty()) {
			return Optional.empty();
		}

		GiftIdeaRecord record = new GiftIdeaRecord(UUID.randomUUID().toString(), text.trim(), Instant.now());
		friend.get().ideas.add(0, record);
		return Optional.of(new GiftIdeaDto(record.id, record.text, record.createdAt.toString()));
	}

	private void seedData() {
		passwordsByUsername.put("omar", "password");
		passwordsByUsername.put("alice", "password");

		FriendRecord hassan = new FriendRecord(UUID.randomUUID().toString(), "Hassan");
		hassan.ideas.add(new GiftIdeaRecord(UUID.randomUUID().toString(), "Montre connectée", Instant.now().minusSeconds(86400)));

		FriendRecord sarah = new FriendRecord(UUID.randomUUID().toString(), "Sarah");
		sarah.ideas.add(new GiftIdeaRecord(UUID.randomUUID().toString(), "Livre de cuisine", Instant.now().minusSeconds(3600)));

		friendsByUsername.put("omar", new ArrayList<>(List.of(hassan, sarah)));

		FriendRecord bob = new FriendRecord(UUID.randomUUID().toString(), "Bob");
		friendsByUsername.put("alice", new ArrayList<>(List.of(bob)));
	}

	private static String normalizeUsername(String username) {
		if (username == null) {
			return null;
		}
		String u = username.trim();
		if (u.length() < 3 || u.length() > 32) {
			return null;
		}
		// simple safe charset for demo app
		if (!u.matches("[A-Za-z0-9._-]+")) {
			return null;
		}
		return u;
	}

	private static String normalizePassword(String password) {
		if (password == null) {
			return null;
		}
		String p = password.trim();
		if (p.length() < 6 || p.length() > 72) {
			return null;
		}
		return p;
	}

	private static String normalizeName(String name) {
		if (name == null) {
			return null;
		}
		String clean = name.trim();
		if (clean.isEmpty()) {
			return null;
		}
		if (clean.length() > 80) {
			return null;
		}
		return clean;
	}

	public static class FriendRecord {
		public final String id;
		public String name;
		public final List<GiftIdeaRecord> ideas = new ArrayList<>();

		public FriendRecord(String id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	public static class GiftIdeaRecord {
		public final String id;
		public final String text;
		public final Instant createdAt;

		public GiftIdeaRecord(String id, String text, Instant createdAt) {
			this.id = id;
			this.text = text;
			this.createdAt = createdAt;
		}
	}
}
