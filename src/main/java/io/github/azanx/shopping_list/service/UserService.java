package io.github.azanx.shopping_list.service;

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.azanx.shopping_list.domain.AppUser;
import io.github.azanx.shopping_list.domain.ListItem;
import io.github.azanx.shopping_list.domain.ShoppingList;
import io.github.azanx.shopping_list.domain.exception.ListTooLongException;
import io.github.azanx.shopping_list.repository.AppUserRepository;
import io.github.azanx.shopping_list.repository.ListItemRepository;
import io.github.azanx.shopping_list.repository.ShoppingListRepository;
import io.github.azanx.shopping_list.service.exception.DuplicateUserException;
import io.github.azanx.shopping_list.service.exception.ItemNotFoundException;
import io.github.azanx.shopping_list.service.exception.ListNotFoundException;
import io.github.azanx.shopping_list.service.exception.UserNotFoundException;

//

/**
 * Service used to separate repository usage and exception management (user not
 * found etc) for database access from controllers
 * 
 * @author Kamil Piwowarski
 */
@Service
public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	private final AppUserRepository appUserRepository;

	private final ListItemRepository listItemRepository;

	private final ShoppingListRepository shoppingListRepository;

	@Autowired
	public UserService(AppUserRepository appUserRepository, ListItemRepository listItemRepository,
			ShoppingListRepository shoppingListRepository) {
		super();
		this.appUserRepository = appUserRepository;
		this.listItemRepository = listItemRepository;
		this.shoppingListRepository = shoppingListRepository;
	}

	/**
	 * Checks if database has admin account configured, if not - creates it with
	 * default password 'admin' and generate WARNING in log
	 */
	@PostConstruct
	@Transactional(readOnly = false)
	public void init() {
		if (!appUserRepository.findByUserName("admin").isPresent()) {
			// check if admin exists, else create one and log
			LOGGER.warn("No 'admin' account found in DB, creating 'admin' account with password defaulting to 'admin'");
			AppUser newAdmin = new AppUser("admin", "admin", "admin@temp.pl");
			appUserRepository.save(newAdmin);
			ShoppingList shopList = newAdmin.addShoppingList("admin shopping");
			ListItem listItem1 = new ListItem("mleko", shopList);
			shopList.addListItem(listItem1);
			newAdmin.addShoppingList(shopList);
			newAdmin.addShoppingList("second list").addListItem("mleko z drugiej listy");

			appUserRepository.save(newAdmin);
		}
	}

	/**
	 * Returns AppUser instance if user with given userName exists
	 * 
	 * @param userName
	 *            name of the user to retrieve from the database
	 * @return AppUser
	 * @throws UserNotFoundException
	 *             if user with 'userName' doesn't exist
	 */
	// throws clause not necessarry as it's an unchecked exception
	@Transactional(readOnly = true)
	public AppUser getUserIfExistsElseThrow(String userName) {
		return appUserRepository//
				.findByUserName(userName) //
				.orElseThrow( //
						() -> new UserNotFoundException(userName)); //
	}

	/**
	 * Retrieve shopping lists of an user from the database
	 * 
	 * @param userName
	 *            name of the user whose lists to retrieve from the database
	 * @return collection containing all of the ShoppingLists belonging to this
	 *         user
	 * @throws ListNotFoundException
	 *             if user doesn't have any lists
	 */
	@Transactional(readOnly = true)
	public Set<ShoppingList> getShoppingListsForUser(String userName) {
		Set<ShoppingList> lists = shoppingListRepository.findByOwnerName(userName);
		if (lists.isEmpty()) {
			throw new ListNotFoundException(userName);
		}
		LOGGER.debug("Returning ShoppingLists for user: {}, Lists:\n {}",userName,//
				lists.stream()//
					.map(ShoppingList::toString)//
					.collect(Collectors.toList())//
				);
		return lists;
	}

	/**
	 * Retrieve ListItems of particular user's ShoppingList
	 * 
	 * @param userName
	 *            name of the user whose ListItems to retrieve from the database
	 * @param listNo
	 *            id (as in: number inside the list, NOT primary key in the DB) of the list to retrieve
	 * @return collection containing all of the ListItems belonging to this
	 *         ShoppingList
	 * @throws ListNotFoundException
	 *             if user doesn't have list with this ID
	 */
	@Transactional(readOnly = true)
	public Set<ListItem> getItemsForUsersListNo(String userName, Short listNo) {
		LOGGER.debug("getItemsForUsersListNo: {}", listNo);
		Set<ListItem> items = listItemRepository.findByParentList_OwnerNameAndParentList_ListNo(userName, listNo);
		if (items.isEmpty()) {
			throw new ItemNotFoundException(userName);
		}
		LOGGER.debug("Returning ListItem's with ID's: {}", //
				items.stream()//
						.map(ListItem::getId)//
						.collect(Collectors.toList())
				);
		return items;
	}
	
	/**
	 * Retrieve ListItems of particular user's ShoppingList
	 * 
	 * @param userName
	 *            name of the user whose ListItems to retrieve from the database
	 * @param listId
	 *            id (as in: primary key in DB) of the list to retrieve
	 * @return collection containing all of the ListItems belonging to this
	 *         ShoppingList
	 * @throws ListNotFoundException
	 *             if user doesn't have list with this ID
	 */
	@Transactional(readOnly = true)
	public Set<ListItem> getItemsForUsersListId(String userName, Long listId) {
		//Set<ListItem> items = listItemRepository.findByParentList_OwnerNameAndParentList_ListNo(userName, listId);
//		if (items.isEmpty()) {
//			throw new ItemNotFoundException(userName);
//		}
//		LOGGER.debug("Returning ListItem's with ID's: {}", //
//				items.stream()//
//						.map(ListItem::getId)//
//						.collect(Collectors.toList())
//				);
		//TODO
		return null;
	}

	/**
	 * Add new user if there is no user with same name
	 * 
	 * @param newUser
	 *            AppUser instance of the new user to create
	 * @throws DuplicateUserException
	 *             if user with same name already exists
	 */
	@Transactional(readOnly = false)
	public void addUser(AppUser newUser) {
		if (!appUserRepository.findByUserName(newUser.getUserName()).isPresent()) {
			appUserRepository.save(newUser);
			LOGGER.info("addUser: Created new user: {}", newUser);
		} else {
			throw new DuplicateUserException(newUser.getUserName());
		}
	}

	/**
	 * 
	 */
	/**
	 * Add new shopping list for a given user
	 * @param userName name of the user for which to create the new list
	 * @param newListName name of the new list
	 * @return newly created list
	 * @throws UserNotFoundException if user with given name doesn't exist
	 */
	@Transactional(readOnly = false)
	public ShoppingList addShoppingListToUserByName(String userName, String newListName) {
		AppUser user = appUserRepository.findByUserName(userName)//
				.orElseThrow(//
						() -> new UserNotFoundException(userName));
		//get count of user lists
		short count = shoppingListRepository.countByOwnerName(userName);
		if(count==Short.MAX_VALUE)
			throw new ListTooLongException(ListTooLongException.listType.SHOPPING_LIST, user.getId());
		count++;
		ShoppingList list = user.addShoppingList(newListName, count);
		list = shoppingListRepository.save(list);
		LOGGER.info("Created new list: {}", list);
		return list;
	}

	// @Transactional(readOnly = false)
	// public void removeShoppingList( listId) {
	//
	// }
}
