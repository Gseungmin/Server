package personal.project.exception;

import lombok.Getter;

import org.springframework.security.core.AuthenticationException;

@Getter
public class LoginException extends AuthenticationException {

    private ErrorType errorType;
    private int code;
    private String errorMessage;

    public LoginException(ErrorType errorType, int code, String errorMessage) {
        super("");
        this.errorType = errorType;
        this.code = code;
        this.errorMessage = errorMessage;
    }
}
