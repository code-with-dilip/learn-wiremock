# WireMock

-  WireMock is a Java Web Server which helps to simulate the responses from a API. This will simplify the testing of modules that depends upon HTTP Apis.

-  How do we reliably test applications that depend on APIs which we do not own or do not exist?

-   **WireMock** is a simulator for **httpBased** APIs. It is going to behave as a webserver ane we can make Httprequests as like a real Http API.
    -   This will help test the edge case scenarios.
    -   This will allow us to develop against the API that did not exist.


## WireMock Use Cases

-   The Third Party API that our application depends upon normally provides a Sandbox environment and we make a call to them to verify the functionalities and errors.
    -   The problem with this is that the sandbox env is very brittle and unreliable.
    -   Testing Faulty scenarios can be challenging as well.
        -   Testing Timeouts
        -   Error Handling
        -   Circuit Breakers


## Setting Up WireMock in Junit

- Adding the below line in the JUNIT sets up the wiremock server.
- This takes care of starting the wiremock server and shutting it down.
```
@Rule
public WireMockRule wireMockRule = new WireMockRule();
```   
- Always remember the wiremock server runs on localhost and by default it runs on the port **8080**.

- Using the below code snippet we can get the actual port it runs at. For this use case its going to return **8080**. But the code snippet will be helpful if the port is dynamic.

```
final String baseUrl = String.format("http://localhost:%s", wireMockRule.port());
```

## Stubbing

### GET http call

- Stub for a get call. The below call matches the url and if the call is **GET** then it will return the below response.

```
@Test
public void getUsers(){

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

```

### POST Http Call

- Stub for a post call. It matches the URL and it matches type of the http request
```
@Test
    public void addUser() throws IOException {

        //Given
        stubFor(WireMock.post(urlPathEqualTo(USER_URL))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));
        User user = objectMapper.readValue(TestHelper.readFromPath("user_request.json"),User.class);

        //When
        User addedUser = userService.addUser(user);

        //Then
        assertEquals(12345,addedUser.getId().intValue());
    }```
