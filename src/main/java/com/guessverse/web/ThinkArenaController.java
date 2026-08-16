package com.guessverse.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ThinkArenaController {

    @GetMapping({
            "/",
            "/arena",
            "/categories",
            "/category/{categoryId}",
            "/play/{gameId}",
            "/results/{gameId}",
            "/leaderboard",
            "/tournaments",
            "/tournament/{tournamentId}",
            "/achievements",
            "/friends",
            "/profile/{username}",
            "/settings",
            "/search",
            "/join",
            "/room/{roomCode}",
            "/login",
            "/signup",
            "/forgot-password"
    })
    public String index() {
        return "forward:/index.html";
    }
}
