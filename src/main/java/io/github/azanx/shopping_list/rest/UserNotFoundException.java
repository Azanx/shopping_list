package io.github.azanx.shopping_list.rest;

//@ResponseStatus(value=HttpStatus.NOT_FOUND,reason="Could not find requested user")
public class UserNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4321875051376072725L;

	public UserNotFoundException(String userName) {
		super("Couldn't find user: '" + userName + "'");
	}
}
