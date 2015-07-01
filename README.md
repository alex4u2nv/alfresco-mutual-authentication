# Mutual Authentication via Tomcat

This Alfresco module allows for app to app mutual authentication via Trusted Tomcat Certificates.

### Version
1.1

### Updated Info
- Use of web-fragments to define web security context


### Development Usage

```sh
$ git clone [git-repo-url] mutual-auth
$ cd mutual-auth
$ chmod 755 run.sh
$ ./run.sh

curl -k -E keystore/serviceA.p12:alfresco --cert-type PEM \
-H "Content-Type: application/json" -X POST -d '{ "auth_user": "admin"}'  \
https://localhost:8443/alfresco/service/auth/mutual
```
A successful Response will be as follows:
```json
{
    "output":"TICKET_e6543ffd41d4e8ad4d45fcb72c770783a77b9ce0",
    "status":200
}
```
### Brief Usage Instructions
> Brief instructions until a more elaborate technical document can be written. These instructions assumes that the user has knowledge on how Mutual Authentication works, and knowledge with creating certificates and managing digital certificates. If not, please review the information on the following pages:
* [Creating Certificates][1]
* [Two Way Mutual Authentication][2]

#### Instructions
- Install amp into alfresco.war using alfresco-mmt.jar
- Install client certificates and CA that it was signed with (if any) into your application container's truststore
- Restart Alfresco
- Create a service account in Alfresco with that of the CN used in your Client Certificate
- Configure Service Application to send your Client Certificate when a called service requests it
- Configure Service Application to request an Alfresco Token from https://your-alfresco-server/alfresco/service/auth/mutual
- If Sudoing or running services on behalf of other users are allowed, then you can POST the user to sudo to via Content-Type: application/json

```json
{
    auth_user: "username"
}
```

Advanced Configurations

```properties
# Fail if the service is called through a NON SSL protocol I.E Not HTTPS
# This should only be set to false for debugging,
# and SECURITY CONTEXT in resources/META-INF/web-fragment.xml will need to be updated.
mutual.service.enforce.https=true
# Allow Client Authenticated account to authenticate as another user
mutual.service.sudo.allow=true
```

### Todo's
Update documentation
Develop X509 Certificate Handler (So that Client-Auth Users could be handled outside of Tomcat)

[1]:https://www.digicert.com/code-signing/java-code-signing-guide.htm
[2]:http://docs.oracle.com/cd/E19798-01/821-1841/bncbt/index.html

