package com.learnwiremock.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class NameService {

    private String url;


    public NameService(String _url, WebClient _webClient) {
        this.url = _url;
        this.webClient = _webClient;
    }

    @Autowired
    private WebClient webClient;


    public String getName() {
        return webClient.get().uri(url.concat("/getName"))
                .retrieve()
                .bodyToMono(String.class)
                .block();

    }


    public String getNameWithPathParam(String name) {
        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

    }

    public String getNameWithRequestParam(String name) {

        URI uri = buildURI(name);
        String finalUrl = url.concat(uri.toString());
        System.out.println("finalUrl :" + finalUrl) ;
        return webClient.get().uri(finalUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();

    }

    public String postName(String json) {
        return webClient.post().uri(url.concat("/postName"))
                .syncBody(json)
                .retrieve()
                .bodyToMono(String.class)
                .block();

    }

    public URI buildURI(String name){

        URI uri = UriComponentsBuilder.fromPath("/getName")
                .queryParam("name", name)
                .encode()
                .buildAndExpand(name)
                .toUri();
        return uri;
    }
}
