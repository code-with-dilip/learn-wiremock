package com.learnwiremock.service;

import com.learnwiremock.domain.User;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

import static com.learnwiremock.constants.WireMockConstants.ALL_USERS_URL;
import static com.learnwiremock.constants.WireMockConstants.USER_ID_PATH_PARAM;
import static com.learnwiremock.constants.WireMockConstants.USER_URL;

public class UserService {
    private String url;
    private WebClient webClient;

    public UserService(String _url, WebClient _webClient) {
        this.url = _url;
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

        User exisingUser = getUserById(user.getId());
        if(exisingUser!=null){
            return exisingUser;
        }
        String randomID = UUID.randomUUID().toString();
        user.setUniqueId(randomID);
        return webClient.post().uri(url+USER_URL)
                .syncBody(user)
                .retrieve()
                .bodyToMono(User.class)
                .block();

    }


    public User getUserById(Integer id){

        if(id!=null) {
            User user1 = webClient.get().uri(url+USER_URL+USER_ID_PATH_PARAM, id)
                    .retrieve()
                    .bodyToMono(User.class)
                    .block();
            return user1;
        }

        return null;

    }
}
