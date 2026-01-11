package com.friendgift;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class AuthAndFriendsTest {
	private String loginToken() {
		return given()
				.contentType(ContentType.JSON)
				.body("{\"username\":\"omar\",\"password\":\"password\"}")
				.when()
				.post("/api/auth/login")
				.then()
				.statusCode(200)
				.body("token", notNullValue())
				.extract()
				.path("token");
	}

	@Test
	void login_then_list_friends() {
		String token = loginToken();

		given()
				.header("Authorization", "Bearer " + token)
				.when()
				.get("/api/friends")
				.then()
				.statusCode(200)
				.body("size()", greaterThanOrEqualTo(1));
	}

	@Test
	void crud_friend() {
		String token = loginToken();

		String friendId = given()
				.header("Authorization", "Bearer " + token)
				.contentType(ContentType.JSON)
				.body("{\"name\":\"John\"}")
				.when()
				.post("/api/friends")
				.then()
				.statusCode(201)
				.body("id", notNullValue())
				.body("name", equalTo("John"))
				.extract()
				.path("id");

		given()
				.header("Authorization", "Bearer " + token)
				.when()
				.get("/api/friends")
				.then()
				.statusCode(200)
				.body("id", hasItem(friendId));

		given()
				.header("Authorization", "Bearer " + token)
				.contentType(ContentType.JSON)
				.body("{\"name\":\"John Doe\"}")
				.when()
				.put("/api/friends/" + friendId)
				.then()
				.statusCode(200)
				.body("id", equalTo(friendId))
				.body("name", equalTo("John Doe"));

		given()
				.header("Authorization", "Bearer " + token)
				.when()
				.delete("/api/friends/" + friendId)
				.then()
				.statusCode(204);

		given()
				.header("Authorization", "Bearer " + token)
				.when()
				.get("/api/friends")
				.then()
				.statusCode(200)
				.body("id", not(hasItem(friendId)));
	}

	@Test
	void register_then_access_protected_resources() {
		String username = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		String token = given()
				.contentType(ContentType.JSON)
				.body("{\"username\":\"" + username + "\",\"password\":\"password123\"}")
				.when()
				.post("/api/auth/register")
				.then()
				.statusCode(201)
				.body("token", notNullValue())
				.extract()
				.path("token");

		given()
				.header("Authorization", "Bearer " + token)
				.when()
				.get("/api/friends")
				.then()
				.statusCode(200)
				.body("size()", equalTo(0));
	}
}
