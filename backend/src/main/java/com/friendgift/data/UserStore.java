package com.friendgift.data;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserStore {
	@Inject
	AppUserRepository userRepository;

	@Inject
	FriendRepository friendRepository;

	@Inject
	GiftIdeaRepository giftIdeaRepository;

	public boolean isValidCredentials(String username, String password) {
		if (username == null || password == null) {
			return false;
		}
		AppUser user = userRepository.findById(username);
		return user != null && password.equals(user.password);
	}

	public boolean registerUser(String username, String password) {
		return registerUserDetailed(username, password) == RegistrationOutcome.CREATED;
	}

	@Transactional
	public RegistrationOutcome registerUserDetailed(String username, String password) {
		String u = normalizeUsername(username);
		String p = normalizePassword(password);
		if (u == null || p == null) {
			return RegistrationOutcome.INVALID;
		}
		if (userRepository.findById(u) != null) {
			return RegistrationOutcome.EXISTS;
		}

		userRepository.persist(new AppUser(u, p));
		return RegistrationOutcome.CREATED;
	}

	public enum RegistrationOutcome {
		CREATED,
		EXISTS,
		INVALID
	}

	public List<FriendDto> listFriends(String username) {
		return friendRepository.listByOwnerUsername(username).stream()
				.map(f -> new FriendDto(f.id, f.name))
				.toList();
	}

	@Transactional
	public Optional<FriendDto> addFriend(String username, String name) {
		String clean = normalizeName(name);
		if (clean == null) {
			return Optional.empty();
		}
		AppUser owner = userRepository.findById(username);
		if (owner == null) {
			return Optional.empty();
		}

		Friend record = new Friend(UUID.randomUUID().toString(), owner, clean, Instant.now());
		friendRepository.persist(record);
		return Optional.of(new FriendDto(record.id, record.name));
	}

	@Transactional
	public Optional<FriendDto> updateFriend(String username, String friendId, String name) {
		String clean = normalizeName(name);
		if (clean == null) {
			return Optional.empty();
		}

		Optional<Friend> friend = findFriend(username, friendId);
		if (friend.isEmpty()) {
			return Optional.empty();
		}

		friend.get().name = clean;
		return Optional.of(new FriendDto(friend.get().id, friend.get().name));
	}

	@Transactional
	public boolean deleteFriend(String username, String friendId) {
		Optional<Friend> friend = findFriend(username, friendId);
		if (friend.isEmpty()) {
			return false;
		}
		friendRepository.delete(friend.get());
		return true;
	}

	public Optional<Friend> findFriend(String username, String friendId) {
		return friendRepository.findByOwnerAndId(username, friendId);
	}

	public List<GiftIdeaDto> listIdeas(String username, String friendId) {
		Optional<Friend> friend = findFriend(username, friendId);
		if (friend.isEmpty()) {
			return List.of();
		}
		return giftIdeaRepository.listByFriendIdNewestFirst(friend.get().id).stream()
				.map(ir -> new GiftIdeaDto(ir.id, ir.text, ir.createdAt.toString()))
				.toList();
	}

	@Transactional
	public Optional<GiftIdeaDto> addIdea(String username, String friendId, String text) {
		if (text == null || text.trim().isEmpty()) {
			return Optional.empty();
		}
		Optional<Friend> friend = findFriend(username, friendId);
		if (friend.isEmpty()) {
			return Optional.empty();
		}

		GiftIdea record = new GiftIdea(UUID.randomUUID().toString(), friend.get(), text.trim(), Instant.now());
		giftIdeaRepository.persist(record);
		return Optional.of(new GiftIdeaDto(record.id, record.text, record.createdAt.toString()));
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
}
