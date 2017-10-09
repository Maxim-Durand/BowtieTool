package repository.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import models.User;
import repository.IUserRepository;
import repository.MySQLAccess;

public class UserRepositoryImpl implements IUserRepository{
    private final int SALT_SIZE = 16;
    private MySQLAccess access;

	public UserRepositoryImpl(MySQLAccess access) {
		this.access = access;
	}
	
	private String hash_pw(String password) {
	    SecureRandom random = new SecureRandom();
	    byte[] salt = new byte[SALT_SIZE];
	    random.nextBytes(salt);
	        
	    MessageDigest d;
	    byte[] tmp = null;
	    try {
	        d = MessageDigest.getInstance("SHA-256");
	        d.update(salt);
	        d.update(password.getBytes());
	        byte[] hash = d.digest();
	            
	        tmp = new byte[salt.length + hash.length];
	        System.arraycopy(salt, 0, tmp, 0, salt.length);
	        System.arraycopy(hash, 0, tmp, salt.length, hash.length);
	    } catch (NoSuchAlgorithmException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	    return new String(Base64.getEncoder().encode(tmp));
	}

	@Override
	public void createUser(String username, String password) {
        String insert_user = "INSERT KPRO.User(username, hash_pw) VALUES (?, ?);";
        access.query(insert_user, username, hash_pw(password));
	}

	@Override
	public void deleteUser(User user) {
		//XXX: Vi trenger noen constraints i skjemaet for dette, cascade osv.
	    String insert_user = "DELETE FROM KPRO.User WHERE id = ?;";
        access.query(insert_user, user.getId());
	}

	@Override
	public void updateUser(int id, String username, String password) {
		// TODO Auto-generated method stub
	    String update_user = "UPDATE KPRO.User SET username = ?, hash_pw = ? WHERE id = ?;";
        access.query(update_user, username, hash_pw(password), id);
	}

	@Override
	public User getUserById(int id) {
	    User user = null;
	    String query = "SELECT * FROM KPRO.User WHERE id=?";
	    try {
	        ResultSet rs = access.query(query, id);
	        while (rs.next()) {
	            String username = rs.getString("username");
	            String hash_pw = rs.getString("hash_pw");
	            String token = rs.getString("token");
	            user = new User(id, username, hash_pw, token);
	        }
	    } catch (SQLException e ) {
	        System.out.println("Quering form db failed");
	    }
	    return user;
	}

    @Override
    public User getUserByToken(String token) {
        User user = null;
        String query = "SELECT * FROM KPRO.User WHERE token=?";
        try {
            ResultSet rs = access.query(query, token);
            while (rs.next()) {
                int user_id = rs.getInt("id");
                String username = rs.getString("username");
                String hash_pw = rs.getString("hash_pw");
                user = new User(user_id, username, hash_pw, token);
            }
        } catch (SQLException e ) {
            System.out.println("Quering form db failed");
        }
        return user;
    }
	
}