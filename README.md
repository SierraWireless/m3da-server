M3DA server
===========

A very simple M3DA TCP server. M3DA is a secure and bandwith efficient M2M protocol.

The specification : http://wiki.eclipse.org/Mihini/M3DA_Specification

The client : http://www.eclipse.org/mihini

Compiling 
---------

Compile all the project

> mvn install

With maven generate a runnable uber jar using the command :

> cd server

> mvn assembly:assembly -DdescriptorId=jar-with-dependencies
 

start it using the command 

> java -jar target/m3da-server-1.0-SNAPSHOT-jar-with-dependencies.jar

You can start connecting your M3DA client on the TCP port 44900 (IANA official port for M3DA).

REST READ API
--------

You can see all the received data for a given client by GETing the URL : http://127.0.0.1:8080/clients/{client-identifier}/data
 
The client identifier is the value of "agent.config.agent.deviceId" in your mihini installation.

Examples: 
> GET http://127.0.0.1:8080/clients/01121979/data

RESULT: 
 
```javascript
{
   "@sys.foo.Timestamp":[
      {
         "timestamp":"246977562322",
         "value":[
            1361975530
         ]
      }
   ],
   "@sys.foo.bar":[
      {
         "timestamp":"246977562322",
         "value":[
            123
         ]
      }
   ]
}
```

REST WRITE API
--------

You can push data to a given client by POSTing to the following URL : http://127.0.0.1:8080/clients/{client-identifier}/data
 
The client identifier is the value of "agent.config.agent.deviceId" in your mihini installation.

Examples: 
> POST  http://127.0.0.1:8080/clients/01121979/data

Content: 
 
```javascript
{
   "settings" : [{
      "key" : "@sys.commands.ReadNode.key1",
      "value" : "key1value"
   }, {
      "key" : "@sys.commands.ReadNode.key2",
      "value" : "key2value"   
   }]
}
```

REST CLIENTS API
--------

You can get the list of connect client by GETing the URL : http://127.0.0.1:8080/clients . 
You'll received the list of "in" clients (those that sent data) and "out" clients (those for which data is waiting to be pushed on the server.) 

Example:
> GET  http://127.0.0.1:8080/clients

RESULT: 
```javascript
{
   "in" : ["12131", "client1", "foobar"],
   "out" : ["12131", "other-client"]
}
```

SETUP SECURITY
--------

You can enable secure communication with your client. the client and the server will share a password for ciphering and authenticating communitions.
Like TLS/SSL but is a more bandwidth friendly way (see the M3DA security specification : http://wiki.eclipse.org/Mihini/M3DA_Specification ).

Setup security : 

Post the security information on http://127.0.0.1:8080/clients/{client-identifier}/security
 
The client identifier is the value of "agent.config.agent.deviceId" in your mihini installation.

Examples:
> POST http://127.0.01:8080/clients/01121979/security

Content: 
```javascript
{
   "authentication" : "HMAC_SHA1",
   "encryption" : "AES_CTR_256",
   "password" : "mySecurePassword$$$$"
}


The different authentication schemes are: NONE, HMAC_MD5, HMAC_SHA1

The different encryption shcemes are: NONE, AES_CTR_128, AES_CTR_256, AES_CBC_128, AES_CBC_256

The different used algorithm are explicited in the M3DA security specification.

