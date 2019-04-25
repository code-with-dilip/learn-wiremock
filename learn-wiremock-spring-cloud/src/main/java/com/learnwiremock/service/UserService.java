package com.learnwiremock.service;

import com.learnwiremock.domain.User;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static com.learnwiremock.constants.WireMockConstants.ALL_USERS_URL;
import static com.learnwiremock.constants.WireMockConstants.USER_URL;

public class UserService {
    private String url;
    private User user;
    private WebClient webClient;

    public UserService(String _url, WebClient _webClient) {
        this.url = _url;
        this.webClient=_webClient;
    }

    public UserService(String _url, User _user, WebClient _webClient) {
        this.url = _url;
        this.user = _user;
        this.webClient=_webClient;
    }

    public List<User> getUsers(){
        return webClient.get().uri(url+ ALL_USERS_URL)
                .retrieve()
                .bodyToFlux(User.class)
                .collectList()
                .block();
    }

    public User addUser(User user) {
        return webClient.post().uri(url+USER_URL)
                .syncBody(user)
                .retrieve()
                .bodyToMono(User.class)
                .block();

    }
}
