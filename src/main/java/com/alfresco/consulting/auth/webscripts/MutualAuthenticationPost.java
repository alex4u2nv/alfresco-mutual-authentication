package com.alfresco.consulting.auth.webscripts;

import com.alfresco.consulting.auth.MutualAuthenticationException;
import org.alfresco.encryption.AlfrescoKeyStore;
import org.alfresco.encryption.KeyStoreParameters;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.http.MediaType;

import java.io.IOException;

/**
 * Created by alexmahabir on 11/19/14.
 * This service supports the ability to authenticate to alfresco via client-cert authentication.
 */
public class MutualAuthenticationPost extends AbstractWebScript {

    Logger logger = Logger.getLogger(MutualAuthenticationPost.class);
    public static final String AUTH_USER = "auth_user";

    private TicketComponent ticketComponent;
    private AuthorityService authorityService;
    private PersonService personService;
    private Boolean enforceHttps = true;
    private Boolean enforceClientAuth = true;


    private Boolean allowSudoing = false;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String output = "";
        int status = Status.STATUS_BAD_GATEWAY;
        if (logger.isDebugEnabled()) {
            logger.debug("Authenticated to server");
            logger.debug("Invoking Reverse authentication");
        }
        try {
            if (req.getServerPath().indexOf("https") != 0) {
                output = "Two way mutual authentication must be invoked over SSL(HTTPS)";
                logger.warn(output);
                if (logger.isDebugEnabled()) {
                    String debug = "\nHTTPS Enforced: " + enforceHttps;
                    output += debug;
                    logger.debug(debug);

                }
                if (enforceHttps)
                    throw new MutualAuthenticationException(output);
            }
            String resp = req.getContent().getContent();
            String user=null;
            //note that this user should already be created
            String remoteUser = ((WebScriptServletRequest) ((org.springframework.extensions.webscripts
                    .WrappingWebScriptRequest) req).getNext()).getHttpServletRequest().getRemoteUser().split(",")[0]
                    .substring(3);

            JSONObject json = resp.equals("") ? null : (JSONObject) JSONValue.parseWithException(resp);
            if (allowSudoing && json!=null) {
                Object userobj;
                userobj = json.get(AUTH_USER);
                if (userobj != null)
                    user = userobj.toString();
            } else
                user = remoteUser;
            String ticket = authenticate(user);
            output = ticket;
            if (ticket==null)
                status = Status.STATUS_FORBIDDEN;
            else
                status = Status.STATUS_OK;

        } catch (ParseException e) {
            status = Status.STATUS_BAD_GATEWAY;
            String content = req.getContent().getContent();
            if (logger.isDebugEnabled()) {
                logger.debug(e);
                logger.debug(content);
            }
            output += e.getMessage();
        } catch (MutualAuthenticationException e) {
            logger.debug(e);
            status = Status.STATUS_BAD_GATEWAY;
            output += e.getMessage();
        } finally {
            JSONObject json = new JSONObject();
            json.put("output", output);
            json.put("status", status);
            res.setContentType(MediaType.APPLICATION_JSON.toString());
            res.setStatus(status);
            res.getWriter().write(json.toJSONString());
        }
    }




    public void setTicketComponent(TicketComponent ticketComponent) {
        this.ticketComponent = ticketComponent;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @param userid
     * @return
     */
    private String authenticate(final String userid)  {
        final Boolean userExist = personExists(userid);
        return TenantUtil.runAsSystemTenant(
                new TenantUtil.TenantRunAsWork<String>() {
                    @Override
                    public String doWork() throws Exception {
                        if (userExist) {
                            logger.debug("User Exists: " + userid);
                            return getTicket(userid);
                        } else {
                            logger.warn("Attempt to authenticate System User [ " + userid + " ] Failed");
                        }
                        return null;
                    }

                    private String getTicket(String userid) {
                        String ticket = ticketComponent.getCurrentTicket(userid, true);
                        ticketComponent.validateTicket(ticket);
                        logger.debug("Returning Ticket: " + ticket);
                        return ticket;
                    }
                }, TenantUtil.getCurrentDomain()
        );

    }

    /**
     * @param userId
     * @return
     */
    public Boolean personExists(final String userId) {

        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>() {
            @Override
            public Boolean doWork() throws Exception {
                if (personService.personExists(userId)) {
                    return true;
                }
                return false;
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    public void setAllowSudoing(Boolean allowSudoing) {
        this.allowSudoing = allowSudoing;
    }
    public void setEnforceHttps(Boolean enforceHttps) {
        this.enforceHttps = enforceHttps;
    }

    public void setEnforceClientAuth(Boolean enforceClientAuth) {
        this.enforceClientAuth = enforceClientAuth;
    }
}

