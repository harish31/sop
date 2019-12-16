# supplier-order-processor
Supplier Order Processor

# Maven Release
To release artifacts call below API URL
curl -X POST -H "Content-Type: application/json" -d @body.json "https://circleci.com/api/v1.1/project/github/scoperetail/supplier-order-processor?circle-token={TOKEN}"

{TOKEN} = CircleCi API Token which has build invocation permission

Contents of body.json
```
{
   "build_parameters": {
                         "RELEASE": "Yes",
                         "GIT_USER_EMAIL": "",
                         "GIT_USER_NAME": ""
                       }
}
```
<h3>Note:</h3> Update username and email in body.json
