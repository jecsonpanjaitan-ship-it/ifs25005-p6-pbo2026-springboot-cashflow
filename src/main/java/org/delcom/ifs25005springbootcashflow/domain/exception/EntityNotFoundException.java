package org.delcom.ifs25005springbootcashflow.domain.exception;

/** Entitas tidak ditemukan berdasarkan ID. */
public class EntityNotFoundException extends DomainException {

    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String entityName, int id) {
        super(entityName + " dengan ID " + id + " tidak ditemukan");
    }
}
