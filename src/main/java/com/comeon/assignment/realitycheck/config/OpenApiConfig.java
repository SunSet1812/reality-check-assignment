package com.comeon.assignment.realitycheck.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reality Check API")
                        .version("1.0")
                        .description("API for managing player reality-check sessions."));
    }

    @Bean
    public OpenApiCustomizer playerSessionDocumentation() {
        return openAPI -> {

            PathItem getStatusPath =
                    openAPI.getPaths().get("/getStatus/{playerId}");

            if (getStatusPath != null && getStatusPath.getGet() != null) {
                getStatusPath.getGet().setSummary(
                        "Get player session status"
                );

                getStatusPath.getGet().setDescription("""
                        This endpoint returns the current status of the player's active session.
                        It has a path variable of type integer.

                        Flow:
                        1. After Backend Receives player ID.
                        2. Backend trie to retrieve associated active session to the player Id.
                        3. If associated active session was found then it return String response(ACTIVE)
                        4. If no active session was found then it returns exception with status code 404(Not found).
                        """);
            }


            PathItem createOrUpdatePath =
                    openAPI.getPaths().get("/{playerId}/{intervalMinutes}");

            if (createOrUpdatePath != null
                    && createOrUpdatePath.getPost() != null) {

                createOrUpdatePath.getPost().setSummary(
                        "Create or update player session"
                );

                createOrUpdatePath.getPost().setDescription("""
                        Creates a new player session or updates
                        an existing active session.

                        Flow:
                        1. After Backend Receives player ID and interval_minutes.
                        2. Backend check if a active session associated with the player Id.
                        3.If it exists then :-
                            1. Checks if the franchiseId is same in player record and associated active session(otherwise throws a exception).
                            2. If franchiseId is same, then it updates the session.
                        4. Creates a session with playerId and interval_minutes, if none exists.
                        5. Then returns the updated or create player session.
                        """);
            }


            PathItem acknowledgePath =
                    openAPI.getPaths().get("/acknowledge/{playerId}");

            if (acknowledgePath != null
                    && acknowledgePath.getPut() != null) {

                acknowledgePath.getPut().setSummary(
                        "Acknowledge reality check"
                );

                acknowledgePath.getPut().setDescription("""
                        Acknowledges the current reality check.

                        Flow:
                        1. Retrieve the active session associated with the player Id.
                        2. Set acknowledged to true.
                        3. Create an acknowledgement record.
                        4. Persist the updated session.
                        5. Return the updated session.
                        """);
            }
        };
    }
}