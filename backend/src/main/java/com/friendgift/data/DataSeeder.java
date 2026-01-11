package com.friendgift.data;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class DataSeeder {
	@Inject
	AppUserRepository userRepository;

	@Inject
	FriendRepository friendRepository;

	@Inject
	GiftIdeaRepository giftIdeaRepository;

	void onStart(@Observes StartupEvent event) {
		seedIfMissing();
	}

	@Transactional
	void seedIfMissing() {
		if (userRepository.findById("omar") != null) {
			return;
		}

		AppUser omar = new AppUser("omar", "password");
		AppUser alice = new AppUser("alice", "password");
		userRepository.persist(omar);
		userRepository.persist(alice);

		Instant now = Instant.now();

		Friend hassan = new Friend(UUID.randomUUID().toString(), omar, "Hassan", now.minusSeconds(2 * 86400L));
		Friend sarah = new Friend(UUID.randomUUID().toString(), omar, "Sarah", now.minusSeconds(86400L));
		friendRepository.persist(hassan);
		friendRepository.persist(sarah);

		giftIdeaRepository.persist(new GiftIdea(UUID.randomUUID().toString(), hassan, "Montre connectée", now.minusSeconds(86400L)));
		giftIdeaRepository.persist(new GiftIdea(UUID.randomUUID().toString(), sarah, "Livre de cuisine", now.minusSeconds(3600L)));

		Friend bob = new Friend(UUID.randomUUID().toString(), alice, "Bob", now.minusSeconds(7200L));
		friendRepository.persist(bob);
	}
}
