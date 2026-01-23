package Service;

import DAO.UserDAO;
import Entities.User;
import util.PasswordUtil;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public void register(User user) throws Exception {

        if (user.getUsername().isEmpty() ||
                user.getEmail().isEmpty() ||
                user.getPassword().isEmpty()) {
            throw new Exception("All fields are required.");
        }

        if (userDAO.usernameExists(user.getUsername())) {
            throw new Exception("Username already exists.");
        }
        if(user.getPassword().length() < 6){
            throw new Exception("Password must be at least 6 characters long.");
        }
        if (userDAO.emailExists(user.getEmail()))
        {
            throw new Exception("Email already exists.");
        }
        String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);


        // Later: hash password here

        userDAO.save(user);
    }

}