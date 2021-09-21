package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

/** Exception thrown when an hcert can't be decoded, mainly used to reuse code across controllers */
public class DecodingException extends RuntimeException {

    private final String msg;

    public DecodingException(String msg) {

        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
