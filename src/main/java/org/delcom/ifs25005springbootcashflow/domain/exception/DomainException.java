package org.delcom.ifs25005springbootcashflow.domain.exception;

/** Exception (checked) dasar untuk seluruh kegagalan aturan bisnis. */
public class DomainException extends Exception {

    private static final long serialVersionUID = 1L;

    public DomainException(String message) {
        super(message);
    }
}
