package com.friendgift.data;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FriendRepository implements PanacheRepositoryBase<Friend, String> {
	public List<Friend> listByOwnerUsername(String username) {
		return find("owner.username", Sort.by("createdAt"), username).list();
	}

	public Optional<Friend> findByOwnerAndId(String username, String friendId) {
		return find("owner.username = ?1 and id = ?2", username, friendId).firstResultOptional();
	}
}
