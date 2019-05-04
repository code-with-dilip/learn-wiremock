package com.learnwiremock.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.learnwiremock.domain.User;
import com.learnwiremock.helper.TestHelper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.learnwiremock.constants.WireMockConstants.ALL_USERS_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserServiceWireMockRuleHTTPSSelfSignClientAuthTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .httpsPort(8443)
            .keystorePath("src/test/resources/cert/wiremock-keystore.jks")
            .keystorePassword("password")
            .trustStorePath("src/test/resources/cert/wiremockclient-truststore.jks")
            .trustStorePassword("password")
            .needClientAuth(true)
            );

    private UserService userService;
    WebClient webClient;

    @Before
    public void setUp() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, UnrecoverableKeyException {
        final String baseUrl = String.format("https://localhost:%s", 8443);
        SslContext sslContext = buildSSlContext();
        HttpClient httpClient = HttpClient.create()
                .secure(t -> t.sslContext(sslContext));
        webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        userService = new UserService(baseUrl, webClient);

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

    public SslContext buildSSlContext() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, UnrecoverableKeyException {

        final KeyStore trustStore = null;
        KeyStore keyStore = null;
        String password = "password";
        String keyAlias = "client";
        final PrivateKey privateKey;
        keyStore = buildKeyStore(keyStore, password);
        List<Certificate> clientCertificateList = buiildTrustStoreCerts(trustStore, password);
        final X509Certificate[] trustCertificates = clientCertificateList.toArray(new X509Certificate[clientCertificateList.size()]);
        privateKey = (PrivateKey) keyStore.getKey(keyAlias, password.toCharArray());
        X509Certificate[] x509CertificateChain = getx509CertificateChain(keyStore, keyAlias);
        SslContext sslContext = SslContextBuilder.forClient()
                .keyManager(privateKey, password, x509CertificateChain)
                .trustManager(trustCertificates)
                .build();
        return sslContext;

    }

    public X509Certificate[] getx509CertificateChain(KeyStore keyStore, String keyAlias) throws KeyStoreException {
        Certificate[] certChain = keyStore.getCertificateChain(keyAlias);
        X509Certificate[] x509CertificateChain = Arrays.stream(certChain)
                .map(certificate -> (X509Certificate) certificate)
                .collect(Collectors.toList())
                .toArray(new X509Certificate[certChain.length]);

        return x509CertificateChain;
    }

    public List<Certificate> buiildTrustStoreCerts(KeyStore trustStore, String password) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(new FileInputStream(ResourceUtils.getFile(
                "classpath:cert/wiremock-truststore.jks")), password.toCharArray());
        return getCertificateList(trustStore);

    }

    public KeyStore buildKeyStore(KeyStore keyStore, String password) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(ResourceUtils.getFile(
                "classpath:cert/wiremockclient-keystore.jks")), password.toCharArray());
        return keyStore;
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


}
