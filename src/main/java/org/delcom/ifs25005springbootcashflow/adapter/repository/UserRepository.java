package org.delcom.ifs25005springbootcashflow.adapter.repository;

import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import java.util.Optional;

/** Implementasi in-memory IUserRepository (unit test). */
public class UserRepository extends InMemoryRepository<User> implements IUserRepository {

    @Override
    protected int idOf(User entity) {
        return entity.getId();
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        User user = new User(nextId(), username, passwordHash, fullName, role, null);
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return store.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }
}
