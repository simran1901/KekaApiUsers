package com.apps.keka.api.users.service;

import java.util.ArrayList;
import java.util.UUID;

import com.apps.keka.api.users.ui.model.UserRole;
import io.jsonwebtoken.Jwts;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.apps.keka.api.users.data.UsersRepository;

import com.apps.keka.api.users.shared.UserDto;

import com.apps.keka.api.users.data.*;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsersServiceImpl implements UsersService {

    UsersRepository usersRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    //RestTemplate restTemplate;
    Environment environment;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UsersServiceImpl(UsersRepository usersRepository,
                            BCryptPasswordEncoder bCryptPasswordEncoder,
                            Environment environment) {
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.environment = environment;
    }

    @Override
    public UserDto createUser(UserDto userDetails) {

        userDetails.setUserId(UUID.randomUUID().toString());
        userDetails.setEncryptedPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserEntity userEntity = modelMapper.map(userDetails, UserEntity.class);

        usersRepository.save(userEntity);

        return modelMapper.map(userEntity, UserDto.class);

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = usersRepository.findByEmail(username);

        if (userEntity == null) throw new UsernameNotFoundException(username);

        return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true, new ArrayList<>());
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = usersRepository.findByEmail(email);

        if (userEntity == null) throw new UsernameNotFoundException(email);


        return new ModelMapper().map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {

        UserEntity userEntity = usersRepository.findByUserId(userId);
        if (userEntity == null) throw new UsernameNotFoundException("User not found");

        return new ModelMapper().map(userEntity, UserDto.class);

    }

    @Override
    public ResponseEntity<HttpStatus> deleteUserByUserId(String userId) {
        UserEntity userEntity = usersRepository.findByUserId(userId);
        if (userEntity == null) throw new UsernameNotFoundException("User not found");
        if (userEntity.getRole().equals(UserRole.ADMIN.toString())) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        usersRepository.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<HttpStatus> updateUserByUserId(String userId, UserDto userDetails) {
        UserEntity userEntity = usersRepository.findByUserId(userId);
        if (userEntity == null) throw new UsernameNotFoundException("User not found");
        if (userEntity.getRole().equals(UserRole.ADMIN.toString())) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        if (userDetails.getFirstName() != null) {
            userEntity.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null) {
            userEntity.setLastName(userDetails.getLastName());
        }
        if (userDetails.getEmail() != null) {
            userEntity.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPassword() != null) {
            userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
        }
        usersRepository.save(userEntity);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public Boolean isAdmin(String authHeader) {
        String jwt = authHeader.replace("Bearer", "");
        String subject;
        try {
            subject = Jwts.parser().setSigningKey(environment.getProperty("token.secret")).parseClaimsJws(jwt).getBody()
                    .getSubject();
            if (!usersRepository.findByUserId(subject).getRole().equals(UserRole.ADMIN.toString())) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean isUser(String authHeader) {
        String jwt = authHeader.replace("Bearer", "");
        String subject;
        try {
            subject = Jwts.parser().setSigningKey(environment.getProperty("token.secret")).parseClaimsJws(jwt).getBody()
                    .getSubject();
            if (!usersRepository.findByUserId(subject).getRole().equals(UserRole.USER.toString())) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    @Override
    public String getUserIdFromToken(String authHeader) {
        String jwt = authHeader.replace("Bearer", "");
        String userId;

        try {

            userId = Jwts.parser().setSigningKey(environment.getProperty("token.secret")).parseClaimsJws(jwt).getBody()
                    .getSubject();
        } catch (Exception ex) {
            return null;
        }

        return userId;

    }


}
