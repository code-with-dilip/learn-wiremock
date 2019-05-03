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

## Verification

- Verification is calling WireMock directly to verify the expected requests have been made to it directly.

- The below code snippet makes sure that the call is made to the stub.

```
verify(deleteRequestedFor(urlMatching(USER_URL+"/.*"))); // This will make sure the stub was invoked.
```

```
@Test
   public void deleteUser_with_verification(){

       //given
       stubFor(delete(urlMatching(USER_URL+"/.*"))
               .willReturn(ok())
       );

       //when
       userService.deleteUser(123);

       //then
       verify(deleteRequestedFor(urlMatching(USER_URL+"/.*"))); // by default it checks the call was made once.


   }
```   

### Verification Count

- This is important to make sure that the given endpoint is invoked only **N** number of times.

- The below call is going to make sure that the call is made exactly n number of times.

```
verify(exactly(2), deleteRequestedFor(urlMatching(USER_URL+"/.*")));
```

**Example**

```
@Test
   public void deleteMultipleUser_with_verification(){

       //given
       stubFor(delete(urlMatching(USER_URL+"/.*"))
               .willReturn(ok())
       );

       //when
       userService.deleteUsers(Arrays.asList(1,2));

       //then
       verify(exactly(2), deleteRequestedFor(urlMatching(USER_URL+"/.*")));


   }
```

## Request Journal

- Wire Mock stores all the request and response in a request Journal.
- Request Journal is an in memory commit log of every single request and response made to wire mock.

- The below code snippet gives access to all of the Server Events that are stubbed for the given test.

```
final List<ServeEvent> allServeEvents = WireMock.getAllServeEvents();
```
- You can use the **debug + evaluate** option in the **IntelliJ** to see whats the response from the above call.

**Example**

```
@Test
   public void requestJournal(){

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

       final List<ServeEvent> allServeEvents = WireMock.getAllServeEvents(); // gives you access to all of the request Journal

       //then
       assertEquals(45678,user.getId().intValue());
       assertEquals(12345,user1.getId().intValue());
   }
```

## Running WireMock as StandAlone

- Check this section in **PluralSight** it really explained very well.

### How to run it as standalone?

```
java -jar wiremock-standalone-2.23.2.jar
```

- Go to the below link to access the local instance of running wire mock.

```
http://localhost:8080/__admin/docs
```

## Recording and Proxying

- Recording is the concept where the the call will be actually made to the API having **wiremock** in between recording the API interactions.

### Why record an API ?

- Stub Accuracy.
- Manual Stub creations may be prone to error.
- Stub creation will be faster.

### Recording the wiremock using UI
- Start up the wiremock stand alone in your local.

- Go to the below link.

```
http://localhost:8080/__admin/recorder/
```

- In the **Target URL** provide the below url and click on the **Record** button.

```
https://www.pluralsight.com
```

- Type in **loclhost:8080** , this will take you to the pluralsight website. By then all the rest api calls thats made in that screen would have been captured by the Wiremock.

- Click on the stop button and the a message of number of stubs that are recorded will be displayed.

- The one advantage of doing this will help avoid the manual effort of building these stubs.

### Recording using the WireMock - Code

- If you add the below piece of code in the test case then this takes care of recording the request and response stubs for you.
- But to generate the stubs you need to actually connect to the api and have the below code enabled it takes care of capturing the stubs and the recorded stubs will be stored in the **src/test/resources/mappings** folder.
- Check the m5 and d2 in the pluralsight code base.

```
@Before
public void startRecording() {
    WireMock.startRecording("http://localhost:8081");
}

@After
public void stopRecording() {
    WireMock.stopRecording();
}
```

### Selective Proxying

- In general we have different types of endpoints.
  - deterministic endpoints
  - nondeterministic endpoints
  - notimplemested endpoints.
  - Never happen endpoints in SandBox environment.

#### Selective Proxying setUp

- If there are no stub set up available in the test method then the below call will invoke the actual service thats running in the below address.
- For the below example , it connects to localhost and the port it runs against is **8081**.

```
stubFor(any(anyUrl()).willReturn(aResponse().proxiedFrom("http://localhost:8081")));
```
- Running a stub for the endpoint thats not proxied can be run by using the below set up.

```
@Test
public void updateUser() throws IOException {

    stubFor(WireMock.put(urlMatching(USER_URL+"/.*"))
            .willReturn(WireMock.aResponse()
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(TestHelper.readFromPath("user_update_response.json"))));


    User user = objectMapper.readValue(TestHelper.readFromPath("user_update_request.json"),User.class);
    User updatedUser = userService.updateUser(user);
    assertEquals(33,updatedUser.getAge().intValue());

}
```

## Fault Simulation

- We need to always have the failure scenarios covered as this might happen in the real environment.

- Some of the HTTP Errors are listed below.
  - Timeouts and Latency
  - Server Error
  - Invalid Response
- **Wiremock** comes in handy under these scenarios to simulate the responses.

### Error Simulation

**Example 1:**

- Calling the **serverError()** method will return the 500 response.

```
stubFor(WireMock.put(urlMatching(USER_URL+"/.*"))
                .willReturn(serverError()));
```
**Example 2:**

- We can return **Fault** also to simulate the error response.

```
@Test(expected = Exception.class)
    public void updateUser_withFault() throws IOException {

        stubFor(WireMock.put(urlMatching(USER_URL+"/.*"))
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE))); // returns server error with no body.


        User user = objectMapper.readValue(TestHelper.readFromPath("user_update_request.json"),User.class);
        User updatedUser = userService.updateUser(user);

    }
```
**Example 3:**

- Using the Fault object to simulate the **CONNECTION_RESET_BY_PEER**.

```
@Test(expected = Exception.class)
    public void updateUser_withFault_connectionreset() throws IOException {

        stubFor(WireMock.put(urlMatching(USER_URL+"/.*"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))); // returns server error with no body.


        User user = objectMapper.readValue(TestHelper.readFromPath("user_update_request.json"),User.class);
        User updatedUser = userService.updateUser(user);

    }
```

### Latency Simulation

**withFixedDelay** - This function simulates the delay with the given time.
```
@Test
   public void getUserById_withFixedDelay(){

       //given
       stubFor(WireMock.get(urlMatching(USER_URL+"/.*"))
               .willReturn(WireMock.aResponse()
                       .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                       .withBody(TestHelper.readFromPath("user_response.json"))
               .withFixedDelay(1000)));//this gives a delay of 100ms

       //when
       User user = userService.getUserById(123);
       User user1 = userService.getUserById(456);
       //then
       assertEquals(12345,user.getId().intValue());
       assertEquals(12345,user1.getId().intValue());
   }

```
**withUniformRandomDelay** - This function simulates the delay between the values mentioned in the arguments.
```
   @Test
   public void getUserById_withRandomDelay(){

       //given
       stubFor(WireMock.get(urlMatching(USER_URL+"/.*"))
               .willReturn(WireMock.aResponse()
                       .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                       .withBody(TestHelper.readFromPath("user_response.json"))
                       .withUniformRandomDelay(10, 1000)));//this gives a delay of 100ms

       //when
       User user = userService.getUserById(123);
       User user1 = userService.getUserById(456);
       //then
       assertEquals(12345,user.getId().intValue());
       assertEquals(12345,user1.getId().intValue());
   }
```

## HTTPS with WireMock

### How HTTPS Works ?

- Watch this video to understand more on how https works.
  - https://www.youtube.com/watch?v=iQsKdtjwtYI

  - https://www.khanacademy.org/computing/computer-science/internet-intro/internet-works-intro/v/the-internet-encryption-and-public-keys

### Configure the wiremock to run in a HTTPS port.

- Configuring the JUNIT rule to run against it.

```
@Rule
public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().httpsPort(8443));
```

- Setting up the url to run against the https port and **https** instead of **http**

```
final String baseUrl = String.format("https://localhost:%s", wireMockRule.port());
```

#### Exploring the privatekey and publickey of wiremock

- Unzip the **wiremock-stand-alone** jar file. You will find a keystore file which has the selfsigned certificate.

- Using the java **keytool** command we will look in to the certificate.
  - No password is necessary.
```
keytool -list -keystore keystore
```
- You will get the below result after the above command run.

- You see that we have a private key entry to decrypt the traffic.

```
Keystore type: jks
Keystore provider: SUN

Your keystore contains 1 entry

wiremock, Feb 24, 2015, PrivateKeyEntry,
Certificate fingerprint (SHA1): 6F:B3:CC:4F:53:F3:89:4C:70:6F:CC:1D:43:F4:EA:B3:55:F0:AD:95

Warning:
The JKS keystore uses a proprietary format. It is recommended to migrate to PKCS12 which is an industry standard format using "keytool -importkeystore -srckeystore keystore -destkeystore keystore -deststoretype pkcs12".
```

- Lets create the public key.  The below command will create the **wiremock.crt** file which has the **OWNER** and **ISSUER** details.

```
keytool -exportcert -alias wiremock -file wiremock.crt -keystore keystore
```
- Run the below command to see whats inside the **crt** file.

```
keytool -printcert -file wiremock.crt
```

- You will get the below result for the above command.

```
Owner: CN=Tom Akehurst, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown
Issuer: CN=Tom Akehurst, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown
Serial number: 1d892e4f
Valid from: Tue Feb 24 07:58:50 CST 2015 until: Thu Jan 31 07:58:50 CST 2115
Certificate fingerprints:
	 MD5:  A8:CB:72:1E:19:E2:3D:A1:D1:80:9C:E8:08:48:04:B3
	 SHA1: 6F:B3:CC:4F:53:F3:89:4C:70:6F:CC:1D:43:F4:EA:B3:55:F0:AD:95
	 SHA256: 66:02:81:F6:DD:F9:C1:DE:66:21:10:8B:69:A9:85:42:9A:5B:CE:17:9A:4A:6C:F4:89:59:A1:BD:A6:E1:9C:31
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3

Extensions:

#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: 36 7A B2 85 D1 C4 3F CD   DA 7E 5B FB E0 95 AD 66  6z....?...[....f
0010: EB 5C B2 96                                        .\..
]
]
```

- Creating the **client-truststore.jks** file.

```
keytool -import -file wiremock.crt -alias wiremock -keystore /Users/z001qgd/Dilip/study/wiremock/codebase/learn-wiremock-spring-cloud/src/test/resources/cert/client-truststore.jks
```

### Self Signing the certificates

#### Generating our OWN certificate

- This is to generate our own certificate and use it instead of the one thats available in the jar file.

- The below command is used to generate a new certificate and this can be used for authentication. The below command will create a file named **wiremock-keystore.jks**.

```
keytool -genkey -keyalg RSA -alias wiremock -keystore wiremock-keystore.jks
```
- You will get the below result after you run that command.

```
Enter keystore password:
Re-enter new password:
What is your first and last name?
  [Unknown]:  localhost
What is the name of your organizational unit?
  [Unknown]:  Authors
What is the name of your organization?
  [Unknown]:  Pluralsight
What is the name of your City or Locality?
  [Unknown]:  Minneapolis
What is the name of your State or Province?
  [Unknown]:  MN
What is the two-letter country code for this unit?
  [Unknown]:  US
Is CN=localhost, OU=Authors, O=Pluralsight, L=Minneapolis, ST=MN, C=US correct?
  [no]:  yes

Enter key password for <wiremock>
	(RETURN if same as keystore password):
Re-enter new password:
```

#### Generating the public key from wiremock-keystore.jks

- The below command generates the **wiremock-new.crt** file and it stores the certificate in that file.
```
keytool -exportcert -alias wiremock -file wiremock-new.crt -keystore wiremock-keystore.jks
```
- You will get the below result after you run that command.

```
Enter keystore password:
Certificate stored in file <wiremock-new.crt>

Warning:
The JKS keystore uses a proprietary format. It is recommended to migrate to PKCS12 which is an industry standard format using "keytool -importkeystore -srckeystore wiremock-keystore.jks -destkeystore wiremock-keystore.jks -deststoretype pkcs12".
```

- Now its time to trust it.

```
keytool -import -file wiremock-new.crt -alias wiremock -keystore client-truststore.jks
```
- You will get the below result after you run that command. As you can see that we have Owner and Issuer matches with the one that was present when the actual certificate was generated.

```
Owner: CN=localhost, OU=Authors, O=Pluralsight, L=Minneapolis, ST=MN, C=US
Issuer: CN=localhost, OU=Authors, O=Pluralsight, L=Minneapolis, ST=MN, C=US
Serial number: 6af7bda
Valid from: Fri May 03 06:56:24 CDT 2019 until: Thu Aug 01 06:56:24 CDT 2019
Certificate fingerprints:
	 MD5:  68:0F:B7:41:93:04:11:75:4F:E5:20:B0:88:3A:C9:0C
	 SHA1: 5A:2A:21:0E:46:15:22:53:3D:04:D2:C4:E2:95:13:7C:12:76:94:69
	 SHA256: 4A:17:AD:7A:E8:45:79:11:BF:FA:1A:63:1F:2E:56:12:5D:FF:64:02:22:7C:D5:50:44:0B:87:A3:AA:CC:51:F0
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3

Extensions:

#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: A9 84 FB C3 02 FE 25 F6   F8 89 FF 4C B9 94 6C 50  ......%....L..lP
0010: 59 A1 E3 66                                        Y..f
]
]

Trust this certificate? [no]:  yes
```
