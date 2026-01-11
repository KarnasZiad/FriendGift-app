package com.friendgift.api;

import com.friendgift.data.FriendDto;
import com.friendgift.data.FriendUpsertRequest;
import com.friendgift.data.UserStore;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/api/friends")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FriendsResource {
	@Inject
	UserStore userStore;

	@Inject
	JsonWebToken jwt;

	@GET
	@RolesAllowed("user")
	public List<FriendDto> listFriends() {
		return userStore.listFriends(jwt.getName());
	}

	@POST
	@RolesAllowed("user")
	public Response addFriend(FriendUpsertRequest request) {
		if (request == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		return userStore.addFriend(jwt.getName(), request.name)
				.map(dto -> Response.status(Response.Status.CREATED).entity(dto).build())
				.orElseGet(() -> Response.status(Response.Status.BAD_REQUEST).build());
	}

	@PUT
	@Path("/{friendId}")
	@RolesAllowed("user")
	public Response updateFriend(@PathParam("friendId") String friendId, FriendUpsertRequest request) {
		if (request == null || friendId == null || friendId.isBlank()) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		// Distinguish between "not found" and "bad input" for a nicer client UX.
		if (userStore.findFriend(jwt.getName(), friendId).isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		return userStore.updateFriend(jwt.getName(), friendId, request.name)
				.map(dto -> Response.ok(dto).build())
				.orElseGet(() -> Response.status(Response.Status.BAD_REQUEST).build());
	}

	@DELETE
	@Path("/{friendId}")
	@RolesAllowed("user")
	public Response deleteFriend(@PathParam("friendId") String friendId) {
		if (friendId == null || friendId.isBlank()) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		boolean deleted = userStore.deleteFriend(jwt.getName(), friendId);
		return deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
	}
}
