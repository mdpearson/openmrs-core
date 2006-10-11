package org.openmrs.api.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.UserDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * User-related services
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class UserServiceImpl implements UserService {
	
	//private Context Context;
	//private DAOContext daoContext;
	
	private UserDAO dao;
	
	public UserServiceImpl() { }
	
	private UserDAO getUserDAO() {
		return dao;
	}
	
	public void setUserDAO(UserDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.UserService#createUser(org.openmrs.User, java.lang.String)
	 */
	public void createUser(User user, String password) throws APIException {
		checkPrivileges(user);
		getUserDAO().createUser(user, password);
	}

	/**
	 * Get user by internal user identifier
	 * @param userId internal identifier
	 * @return requested user
	 * @throws APIException
	 */
	public User getUser(Integer userId) throws APIException {
		return getUserDAO().getUser(userId);
	}
	
	/**
	 * Get user by username (user's login identifier)
	 * @param username user's identifier used for authentication
	 * @return requested user
	 * @throws APIException
	 */
	public User getUserByUsername(String username) throws APIException {
		return getUserDAO().getUserByUsername(username);
	}

	/**
	 * true/false if username or systemId is already in db in username or system_id columns
	 * @param User to compare
	 * @return boolean
	 * @throws APIException
	 */
	public boolean hasDuplicateUsername(User user) throws APIException {
		return getUserDAO().hasDuplicateUsername(user);
	}
	
	/**
	 * Get users by role granted
	 * @param Role role that the Users must have to be returned 
	 * @return users with requested role
	 * @throws APIException
	 */
	public List<User> getUsersByRole(Role role) throws APIException {
		List<Role> roles = new Vector<Role>();
		roles.add(role);
		
		return getAllUsers(roles, false);
	}
	
	/**
	 * Save changes to user
	 * @param user
	 * @throws APIException
	 */
	public void updateUser(User user) throws APIException {
		checkPrivileges(user);
		getUserDAO().updateUser(user);
	}
	
	/**
	 * Grant roles for user
	 * @param user
	 * @param role
	 * @throws APIException
	 */
	public void grantUserRole(User user, Role role) throws APIException {
		user.addRole(role);
		updateUser(user);
	}
	
	/**
	 * Revoke roles from user
	 * @param user
	 * @param role
	 * @throws APIException
	 */
	public void revokeUserRole(User user, Role role) throws APIException {
		user.removeRole(role);
		updateUser(user);
	}

	/** 
	 * Mark user as voided (effectively deleting user without removing
	 * their data &mdash; since anything the user touched in the database
	 * will still have their internal identifier and point to the voided
	 * user for historical tracking purposes.
	 * 
	 * @param user
	 * @param reason
	 * @throws APIException
	 */
	public void voidUser(User user, String reason) throws APIException {
		user.setVoided(true);
		user.setVoidReason(reason);
		user.setVoidedBy(Context.getAuthenticatedUser());
		user.setDateVoided(new Date());
		updateUser(user);
	}
	
	/**
	 * Clear voided flag for user (equivalent to an "undelete" or
	 * Lazarus Effect for user)
	 * 
	 * @param user
	 * @throws APIException
	 */
	public void unvoidUser(User user) throws APIException {
		user.setVoided(false);
		user.setVoidReason(null);
		user.setVoidedBy(null);
		user.setDateVoided(null);
		updateUser(user);
	}
	
	/**
	 * Delete user from database. This is included for troubleshooting and
	 * low-level system administration. Ideally, this method should <b>never</b>
	 * be called &mdash; <code>Users</code> should be <em>voided</em> and
	 * not <em>deleted</em> altogether (since many foreign key constraints
	 * depend on users, deleting a user would require deleting all traces, and
	 * any historical trail would be lost).
	 * 
	 * This method only clears user roles and attempts to delete the user
	 * record. If the user has been included in any other parts of the database
	 * (through a foreign key), the attempt to delete the user will violate
	 * foreign key constraints and fail.
	 * 
	 * @param user
	 * @throws APIException
	 * @see #voidUser(User, String)
	 */
	public void deleteUser(User user) throws APIException {
		getUserDAO().deleteUser(user);
	}
	
	/**
	 * Returns all privileges currently possible for any User
	 * @return Global list of privileges
	 * @throws APIException
	 */
	public List<Privilege> getPrivileges() throws APIException {
		return getUserDAO().getPrivileges();
	}
	
	/**
	 * Returns all roles currently possible for any User
	 * @return Global list of roles
	 * @throws APIException
	 */
	public List<Role> getRoles() throws APIException {
		return getUserDAO().getRoles();
	}
	
	/**
	 * Returns roles that inherit from this role
	 * @return inheriting roles
	 * @throws APIException
	 */
	public List<Role> getInheritingRoles(Role role) throws APIException {
		return getUserDAO().getInheritingRoles(role);
	}

	/**
	 * Returns all users in the system
	 * @return Global list of users
	 * @throws APIException
	 */
	public List<User> getUsers() throws APIException {
		return getUserDAO().getUsers();
	}

	/**
	 * Returns role object with given string role
	 * @return Role
	 * @throws APIException
	 */
	public Role getRole(String r) throws APIException {
		return getUserDAO().getRole(r);
	}

	/**
	 * Returns Privilege in the system with given String privilege
	 * @return Privilege
	 * @throws APIException
	 */
	public Privilege getPrivilege(String p) throws APIException {
		return getUserDAO().getPrivilege(p);
	}
	
	public void changePassword(User u, String pw) throws APIException {
		getUserDAO().changePassword(u, pw);
	}
	
	/**
	 * Changes the current user's password
	 * @param pw
	 * @param pw2
	 * @throws APIException
	 */
	public void changePassword(String pw, String pw2) throws APIException {
		getUserDAO().changePassword(pw, pw2);
	}
	
	public void changeQuestionAnswer(String pw, String q, String a) {
		getUserDAO().changeQuestionAnswer(pw, q, a);
	}
	
	public boolean isSecretAnswer(User u, String answer) {
		return getUserDAO().isSecretAnswer(u, answer);
	}
	
	/**
	 * Return a user if any part of the search matches first/last/system id and the user
	 * has one of the roles supplied
	 * @param name
	 * @param roles
	 * @param includeVoided
	 * @return
	 */
	public List<User> findUsers(String name, List<String> roles, boolean includeVoided) {
		name = name.replace(", ", " ");
		return getUserDAO().findUsers(name, roles, includeVoided);
	}
	
	/**
	 * Find a user by exact first name and last name
	 * @param firstName
	 * @param lastName
	 * @param includeVoided
	 * @return
	 */
	public List<User> findUsers(String firstName, String lastName, boolean includeVoided) {
		return getUserDAO().findUsers(firstName, lastName, includeVoided);
	}
	
	/**
	 * Get all users that have at least one of the roles in <code>roles</code>
	 * 
	 * @param roles
	 * @param includeVoided
	 * @return list of users
	 */
	public List<User> getAllUsers(List<Role> roles, boolean includeVoided) {
		Role auth_role = getRole(OpenmrsConstants.AUTHENTICATED_ROLE);
		
		if (roles.contains(auth_role))
			return getUserDAO().getAllUsers(getRoles(), includeVoided);
		
		return getUserDAO().getAllUsers(roles, includeVoided);
	}
	
	/**
	 * This function checks if the authenticated user has all privileges they are giving out
	 * @param new user that has privileges 
	 */
	private void checkPrivileges(User user) {
		Collection<Role> roles = user.getAllRoles();
		User authUser = Context.getAuthenticatedUser();
		
		List<String> requiredPrivs = new Vector<String>();
		
		for (Role r : roles) {
			if (r.getRole().equals(OpenmrsConstants.SUPERUSER_ROLE) &&
				!authUser.hasRole(OpenmrsConstants.SUPERUSER_ROLE))
					throw new APIException("You must have the role '" + OpenmrsConstants.SUPERUSER_ROLE + "' in order to assign it.");
			for (Privilege p : r.getPrivileges())
				if (!authUser.hasPrivilege(p.getPrivilege()))
					requiredPrivs.add(p.getPrivilege());
		}
		
		if (requiredPrivs.size() == 1) {
			throw new APIException("You must have privilege '" + requiredPrivs.get(0) + "' in order to assign it.");
		} 
		else if (requiredPrivs.size() > 1) {
			String txt = "You must have the following privileges in order to assign them: ";
			for (String s : requiredPrivs) 
				txt += s + ", ";
			txt = txt.substring(0, txt.length() - 2);
			throw new APIException(txt);
		}
	}
	
	
	
	/**
	 * @see org.openmrs.api.UserService#addUserProperty(org.openmrs.User, java.lang.String, java.lang.String)
	 */
	public void setUserProperty(User user, String key, String value) {
		if (user != null) {
			if (!user.hasPrivilege(OpenmrsConstants.PRIV_EDIT_USERS) &&
					!user.equals(Context.getAuthenticatedUser()))
					throw new APIException("You are not authorized to change " + user.getUserId() + "'s properties");

			Context.addProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
			user.setProperty(key, value);
			updateUser(user);
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
		}
	}

	/**
	 * @see org.openmrs.api.UserService#removeUserProperty(org.openmrs.User, java.lang.String)
	 */
	public void removeUserProperty(User user, String key) {
		if (user != null) {
			if (!user.hasPrivilege(OpenmrsConstants.PRIV_EDIT_USERS) &&
					!user.equals(Context.getAuthenticatedUser()))
					throw new APIException("You are not authorized to change " + user.getUserId() + "'s properties");

			Context.addProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
			user.removeProperty(key);
			updateUser(user);
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
		}
	}

	/**
	 * Get/generate/find the next system id to be doled out.  Assume check digit /not/ applied
	 * in this method
	 * @return new system id
	 */
	public String generateSystemId() {
		return getUserDAO().generateSystemId();
	}
}
