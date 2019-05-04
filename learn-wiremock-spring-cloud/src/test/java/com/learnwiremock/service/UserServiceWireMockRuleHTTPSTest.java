package com.learnwiremock.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.learnwiremock.domain.User;
import com.learnwiremock.helper.TestHelper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.learnwiremock.constants.WireMockConstants.ALL_USERS_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserServiceWireMockRuleHTTPSTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().httpsPort(8443));
    private UserService userService;
    WebClient webClient;

    static {
        //for localhost testing only
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier(){
                    public boolean verify(String hostname,
                                          javax.net.ssl.SSLSession sslSession) {
                        if (hostname.equals("localhost")) {
                            return true;
                        }
                        return false;
                    }
                });
    }

    @Before
    public void setUp() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        final String baseUrl = String.format("https://localhost:%s", 8443);

        SslContext sslContext = buildSSlContext();
        HttpClient httpClient = HttpClient.create()
                .secure(t -> t.sslContext(sslContext));
              //s  .baseUrl("https://localhost:8443");
        webClient = WebClient.builder()
               .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        userService = new UserService(baseUrl, webClient);

    }

    public SslContext buildSSlContext() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        final KeyStore trustStore;
        final KeyStore keyStore;
        String trustStorePass = "password";
        /**
         * truststore
         */
        trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(new FileInputStream(ResourceUtils.getFile(
                "classpath:cert/wiremock-truststore.jks")), trustStorePass.toCharArray());
        System.out.println("Aliases are : " + trustStore.aliases());
        /**
         * keystore
         */
       /* keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        String keyStorePath = "/Users/z001qgd/Dilip/study/wiremock/downloads/wiremock-standalone-2.23.2/keystore";
        keyStore.load(new FileInputStream(ResourceUtils.getFile(keyStorePath)), "".toCharArray());*/

        List<Certificate> certificateList = getCertificateList(trustStore);
        final X509Certificate[] certificates = certificateList.toArray(new X509Certificate[certificateList.size()]);
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                //.trustManager(certificates)
                .build();
        return sslContext;

    }

    List<Certificate> getCertificateList(KeyStore trustStore) throws KeyStoreException {

        List<Certificate> certificateList = Collections.list(trustStore.aliases())
                .stream()
                .filter(t -> {
                    try {
                        return trustStore.isCertificateEntry(t);
                    } catch (KeyStoreException e1) {
                        throw new RuntimeException("Error reading truststore", e1);
                    }
                })
                .map(t -> {
                    try {
                        return trustStore.getCertificate(t);
                    } catch (KeyStoreException e2) {
                        throw new RuntimeException("Error reading truststore", e2);
                    }
                })
                .collect(Collectors.toList());
        System.out.println("certificateList : " + certificateList);
        return certificateList;

    }

    @Test
    public void getUsersSSL() {

        //Given
        stubFor(WireMock.get(urlPathEqualTo(ALL_USERS_URL))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("multiple_users_response.json"))));

        //When
        List<User> userList = userService.getUsers();

        //then
        assertEquals(3, userList.size());
    }
}
