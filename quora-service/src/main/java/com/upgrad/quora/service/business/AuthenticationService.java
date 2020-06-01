package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider CryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity authenticate(final String username, final String password) throws AuthenticationFailedException {
        UserEntity userEntity = userDao.getUserByUserName(username);
        if (userEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This username does not exist");
        }
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime expiresAt = now.plusHours(8);
        UserAuthEntity existingUserAuth = userDao.getUserAuthByUser(userEntity);
        final String encryptedPassword = CryptographyProvider.encrypt(password, userEntity.getSalt());
        if (encryptedPassword.equals(userEntity.getPassword())) {
            if(existingUserAuth != null && existingUserAuth.getExpiresAt().isAfter(ZonedDateTime.now())){
                existingUserAuth.setLoginAt(now);
                existingUserAuth.setExpiresAt(expiresAt);
                existingUserAuth.setLogoutAt(null);
                userDao.updateUserAuth(existingUserAuth);
                return existingUserAuth;
            }else {

                JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
                UserAuthEntity userAuthTokenEntity = new UserAuthEntity();
                userAuthTokenEntity.setUser(userEntity);


                userAuthTokenEntity.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));

                userAuthTokenEntity.setLoginAt(now);
                userAuthTokenEntity.setExpiresAt(expiresAt);
                userAuthTokenEntity.setUuid(UUID.randomUUID().toString());


                userDao.createAuthToken(userAuthTokenEntity);
                return userAuthTokenEntity;
            }
        } else {
            throw new AuthenticationFailedException("ATH-002", "Password failed");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity logoutUser(final String authToken) throws SignOutRestrictedException {
        UserAuthEntity userAuth = userDao.getUserAuthByToken(authToken);
        if (userAuth == null) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        }
        userAuth.setLogoutAt(ZonedDateTime.now());
        userDao.updateUserAuth(userAuth);
        return userAuth;

    }

}
