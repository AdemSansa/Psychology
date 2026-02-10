package Service;

import Entities.User;
import util.PasswordUtil;

public class AuthService {

    private final UserService userDAO = new UserService();

    public void register(User user) throws Exception {

        if (user.getUsername().isEmpty() ||
                user.getEmail().isEmpty() ||
                user.getPassword().isEmpty()) {
            throw new Exception("All fields are required.");
        }

        if (user.getUsername().length() < 3) {
            throw new Exception("Username must be at least 3 characters long.");
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new Exception("Invalid email format.");
        }
        if (userDAO.UserNameExists(user.getUsername())) {
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

        userDAO.create(user);

    }

}