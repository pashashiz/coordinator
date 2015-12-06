### How to try

To a add new member, just run the following shell command:
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

To get the group which was initiated by added member:
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

To get all existing groups:
```
curl http://localhost:8080/registrationAndDiscovery
```

To make the member unavailable:
```
curl -X PATCH http://localhost:8080/registrationAndDiscovery/test-apl/setUnavailable?node=master
```

To unregister the added member:
```
curl -X DELETE http://localhost:8080/registrationAndDiscovery/test-apl?node=master
```
or whole group (if there are more nodes)
```
curl -X DELETE http://localhost:8080/registrationAndDiscovery/test-apl
```

To found all groups by type (for example, SERVICE):
```
curl http://localhost:8080/registrationAndDiscovery/search/findByType?type=SERVICE
```

To found all groups by type and subtype (for example, SERVICE and some-apl):
```
curl http://localhost:8080/registrationAndDiscovery/search/findByTypeAndSubtype?type=SERVICE&&subtype=some-apl
```