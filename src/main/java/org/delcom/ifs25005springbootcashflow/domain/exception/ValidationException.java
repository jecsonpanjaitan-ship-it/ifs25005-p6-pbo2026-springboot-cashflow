package org.delcom.ifs25005springbootcashflow.domain.exception;

/** Validasi input bisnis gagal. */
public class ValidationException extends DomainException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
