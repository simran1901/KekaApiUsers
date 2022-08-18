package com.apps.keka.api.users.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UsersRepository extends CrudRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);

    UserEntity findByUserId(String userId);

    void deleteByUserId(String userId);

    @Query(value = "select * from users where role = 'USER'", nativeQuery = true)
    List<UserEntity> findUsers();
}
