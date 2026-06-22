package server.core;

import server.database.dao.AuthTokenDao;
import server.database.dao.UserDao;
import server.database.model.User;

public class NicknameResolver {

    private NicknameResolver() {}

    public static String resolve(String token, AuthTokenDao authTokenDao, UserDao userDao) {
        if (token != null && !token.isBlank()) {
            return authTokenDao.findUserIdByToken(token)
                    .flatMap(userDao::findById)
                    .map(User::username)
                    .orElseGet(NicknameGenerator::generate);
        }
        return NicknameGenerator.generate();
    }
}