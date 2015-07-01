package com.alfresco.consulting.auth;

import org.alfresco.repo.security.authentication.AuthenticationDiagnostic;
import org.alfresco.repo.security.authentication.AuthenticationException;

/**
 * Created by alexmahabir on 11/23/14.
 */
public class MutualAuthenticationException extends AuthenticationException {

    public MutualAuthenticationException(String msg) {
        super(msg);
    }

    public MutualAuthenticationException(String msg, Object[] args) {
        super(msg, args);
    }

    public MutualAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public MutualAuthenticationException(String msg, Object[] args, Throwable cause) {
        super(msg, args, cause);
    }

    public MutualAuthenticationException(String msg, AuthenticationDiagnostic diagnostic) {
        super(msg, diagnostic);
    }

    public MutualAuthenticationException(String msg, Object[] args, AuthenticationDiagnostic diagnostic) {
        super(msg, args, diagnostic);
    }

    public MutualAuthenticationException(String msg, AuthenticationDiagnostic diagnostic, Throwable cause) {
        super(msg, diagnostic, cause);
    }

    public MutualAuthenticationException(String msg, AuthenticationDiagnostic diagnostic, Object[] args, Throwable cause) {
        super(msg, diagnostic, args, cause);
    }
}
