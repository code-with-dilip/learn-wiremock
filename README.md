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

### Creating a STUB

#### GET http call

- Stub for a get call. The below call matches the url and if the call is **GET** then it will return the below response.

- Matching a **GET** call with the url Mapping and this can be done using  **urlPathEqualTo**.

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

#### POST Http Call

- Stub for a post call. It matches the URL and it matches type of the http request

- Matching a **POST** call with the url Mapping and this can be done using  **urlPathEqualTo**.
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
    }
```

- Matching a **POST** call with the url Mapping and the request body and this can be done using  **urlPathEqualTo**.

```
@Test
   public void addUser_withMatchingBody() throws IOException {

       //Given
       stubFor(WireMock.post(urlPathEqualTo(USER_URL))
               .withRequestBody(equalTo(TestHelper.readFromPath("user_request.json"))) // request body is populated here
               .willReturn(WireMock.aResponse()
                       .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                       .withBody(TestHelper.readFromPath("user_response.json"))));
       User user = objectMapper.readValue(TestHelper.readFromPath("user_request.json"),User.class);

       //When
       User addedUser = userService.addUser(user);

       //Then
       assertEquals(12345,addedUser.getId().intValue());
   }

```

### Multiple Stub Matching

- Matching a **POST** with multiple API calls. **Path Param** matching is done below.

```
@Test
    public void addUser_WithMultipleMappings_DuplicateCheck() throws IOException {

        //Given
        stubFor(WireMock.post(urlPathEqualTo(USER_URL))
                .withRequestBody(equalTo(TestHelper.readFromPath("user_request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));

        stubFor(WireMock.get(urlPathEqualTo(USER_URL+"{123}"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));

        User user = objectMapper.readValue(TestHelper.readFromPath("user_request.json"),User.class);

        //When
        User addedUser = userService.addUser(user);
        assertEquals(12345,addedUser.getId().intValue());
    }
```


### NonDeterminism

- Lets say you have a dynamic value in the request everytime a call is made.
- Examples of dynamic values are:
  - TimeStamp
  - UUID

- When you have **dynamic values** then you can make use of **matchingJsonPath** to match each properties in the JSON.
- The advantage with this one is that we can either check the value of each property or we can check whether the property is present.

#### Checks just the property is present

```
withRequestBody(matchingJsonPath("uniqueId"))
```

#### Checks the property is present with the give value
```
withRequestBody(matchingJsonPath("name",equalTo("dilip")))
```

#### Url Path param with any value using **regex**.

```
urlMatching(USER_URL+"/.*")
```
**Example 1 - POST**

```
@Test
   public void addUser_WithDynamicValues() throws IOException {

       //Given
       stubFor(WireMock.post(urlPathEqualTo(USER_URL))
               .withRequestBody(matchingJsonPath("id", equalTo(null)))
               .withRequestBody(matchingJsonPath("name",equalTo("dilip")))
               .withRequestBody(matchingJsonPath("age",equalTo("32")))
               .withRequestBody(matchingJsonPath("uniqueId"))
               .willReturn(WireMock.aResponse()
                       .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                       .withBody(TestHelper.readFromPath("user_response.json"))));

       stubFor(WireMock.get(urlPathEqualTo(USER_URL+"123"))
               .willReturn(WireMock.aResponse()
                       .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                       .withBody(TestHelper.readFromPath("user_response.json"))));

       User user = objectMapper.readValue(TestHelper.readFromPath("user_request.json"),User.class);

       //When
       User addedUser = userService.addUser(user);
       assertEquals(12345,addedUser.getId().intValue());
   }

```  
**Example 2 - GET - urlMatching**

```
@Test
   public void getUserById(){

       //given
       stubFor(WireMock.get(urlMatching(USER_URL+"/.*"))
               .willReturn(WireMock.aResponse()
                       .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                       .withBody(TestHelper.readFromPath("user_response.json"))));

       //when
       User user = userService.getUserById(123);
       User user1 = userService.getUserById(456);
       //then
       assertEquals(12345,user.getId().intValue());
       assertEquals(12345,user1.getId().intValue());
   }
```

### Stub Prioritization

- This comes in to picture when you have multiple stub matching for the same request.
- By default the prority is set as zero. So when you have multiple stubs then the last stub will be selected.
- We can assign a priority number for the request.
  - Lower the priority number, higher the priority.
  - Example:
    - Priority value **1** is greater then priority value **2**.

**Example 1**
- In this example the URL pattern has a regex and a specific id.

- By default the **regex** stub will be selected for any value that is sent without explicitly specifying the priority.
  - To make it work we need to add the priority explicitly like the example below.
```
@Test
    public void getUserById_with_priority(){

        //given
        stubFor(WireMock.get(urlMatching(USER_URL+"/12345"))
                .atPriority(1)
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response-1.json"))));

        stubFor(WireMock.get(urlMatching(USER_URL+"/.*"))
                .atPriority(2)
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));

        //when
        User user = userService.getUserById(12345);
        User user1 = userService.getUserById(34234);

        //then
        assertEquals(45678,user.getId().intValue());
        assertEquals(12345,user1.getId().intValue());
    }
```    
