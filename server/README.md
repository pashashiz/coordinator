### How to try

To add new member, just run the following shell command:
```
curl -X POST -H "Content-Type:application/json" \
  -d "{ \
        \"name\": \"test-apl\", \
        \"node\" : \"master\", \
        \"owner\" : "", \
        \"type\" : \"SERVICE\", \
        \"subtype\" : \"some-apl\", \
        \"address\" : \"https:some-host:443\", \
        \"available\" : \"true\
     \"}" \
  http://localhost:8080/registrationAndDiscovery
```

To get the added member:
```
curl http://localhost:8080/registrationAndDiscovery/test-apl
```
You should get the following response:
```json
{
  "name": "test-apl",
  "type": "SERVICE",
  "subtype": "some-apl",
  "address": "https:some-host:443",
  "members": {
    "master": {
      "owner": "",
      "available": true
    }
  },
  "available": true
}

```