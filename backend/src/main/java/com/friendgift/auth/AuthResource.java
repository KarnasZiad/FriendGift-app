package com.friendgift.auth;

import com.friendgift.data.UserStore;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
	@Inject
	UserStore userStore;

	@POST
	@Path("/register")
	@PermitAll
	public Response register(RegisterRequest request) {
		if (request == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		UserStore.RegistrationOutcome outcome = userStore.registerUserDetailed(request.username, request.password);
		if (outcome == UserStore.RegistrationOutcome.INVALID) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if (outcome == UserStore.RegistrationOutcome.EXISTS) {
			return Response.status(Response.Status.CONFLICT).build();
		}

		String token = Jwt.issuer("friendgift-app")
				.upn(request.username)
				.subject(request.username)
				.groups(Set.of("user"))
				.expiresAt(Instant.now().plus(Duration.ofHours(8)))
				.sign();

		return Response.status(Response.Status.CREATED).entity(new LoginResponse(token)).build();
	}

	@POST
	@Path("/login")
	@PermitAll
	public Response login(LoginRequest request) {
		if (request == null || !userStore.isValidCredentials(request.username, request.password)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		String token = Jwt.issuer("friendgift-app")
				.upn(request.username)
				.subject(request.username)
				.groups(Set.of("user"))
				.expiresAt(Instant.now().plus(Duration.ofHours(8)))
				.sign();

		return Response.ok(new LoginResponse(token)).build();
	}
}
