package com.friendgift.api;

import com.friendgift.data.GiftIdeaDto;
import com.friendgift.data.NewGiftIdeaRequest;
import com.friendgift.data.UserStore;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/api/friends/{friendId}/ideas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class IdeasResource {
	@Inject
	UserStore userStore;

	@Inject
	JsonWebToken jwt;

	@GET
	@RolesAllowed("user")
	public List<GiftIdeaDto> listIdeas(@PathParam("friendId") String friendId) {
		return userStore.listIdeas(jwt.getName(), friendId);
	}

	@POST
	@RolesAllowed("user")
	public Response addIdea(@PathParam("friendId") String friendId, NewGiftIdeaRequest request) {
		String text = request == null ? null : request.text;
		return userStore.addIdea(jwt.getName(), friendId, text)
				.map(dto -> Response.status(Response.Status.CREATED).entity(dto).build())
				.orElse(Response.status(Response.Status.BAD_REQUEST).build());
	}
}
