package se.sprinto.hakan.chatapp.dao;


import se.sprinto.hakan.chatapp.model.User;
import se.sprinto.hakan.chatapp.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserListDAO implements UserDAO {

    private final List<User> users = new ArrayList<>();
    private int nextId = 1;

    @Override
    public User register(User user) {
        String hashed = PasswordUtil.hashPassword(user.getPassword());

        user.setId(nextId++);
        users.add(user);
        return user;
    }

    @Override
    public User login(String username, String password) {
       Optional<User> found = users.stream()
                .filter(u -> u.getUsername().equals(username) && PasswordUtil.verifyPassword(password,u.getPassword()))
                .findFirst();
        return found.orElse(null);
           }

    // valfritt: för testsyfte
    public void seedUsers() {
        register(new User("test", "123"));
        register(new User("hakan", "password"));
    }
}


