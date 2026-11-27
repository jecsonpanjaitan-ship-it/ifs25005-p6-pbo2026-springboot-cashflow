package org.delcom.ifs25005springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Port generik CRUD. Implementasi: InMemory (unit test) dan JDBC (runtime). */
public interface IRepository<T, K> {

    Optional<T> findById(K id);

    List<T> findAll();

    List<T> findBy(Predicate<? super T> predicate);

    T update(T entity);

    boolean deleteById(K id);
}