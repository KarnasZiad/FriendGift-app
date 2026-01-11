package com.friendgift.data;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class GiftIdeaRepository implements PanacheRepositoryBase<GiftIdea, String> {
	public List<GiftIdea> listByFriendIdNewestFirst(String friendId) {
		return find("friend.id", Sort.by("createdAt").descending(), friendId).list();
	}
}
