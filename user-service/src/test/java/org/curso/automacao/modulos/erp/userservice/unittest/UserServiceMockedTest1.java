package org.curso.automacao.modulos.erp.userservice.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.curso.automacao.modulos.erp.userservice.exceptions.ServiceException;
import org.curso.automacao.modulos.erp.userservice.impl.User;
import org.curso.automacao.modulos.erp.userservice.impl.UserQueryBuilder;
import org.curso.automacao.modulos.erp.userservice.impl.UserRepository;
import org.curso.automacao.modulos.erp.userservice.impl.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.javafaker.Faker;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@ActiveProfiles({ "dev", "mock" })
public class UserServiceMockedTest1 extends BaseTest {

	// CLASSE DE LOG PARA OS TESTES
	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceMockedTest1.class);

	// Lista de usuarios
	private static List<User> users = new ArrayList<>();

	@MockBean
	private UserQueryBuilder userQueryBuilder;

	@MockBean
	private UserRepository userRepository;

	@InjectMocks
	@Autowired
	private UserService userService;

	// Cria um usuario com os dados
	public static User createUser() {
		return User.builder().name(Faker.instance().name().fullName())// O FAKER AJUDA NA MASSA DE DADOS CRIANDO USUARIO
				.roles("ROLE_USER").username(Faker.instance().internet().emailAddress())// O FAKER AJUDA NA MASSA DE
																						// DADOS CRIANDO EMAIL
				.userpass(Faker.instance().internet().password(10, 15))// O FAKER AJUDA CRIANDO SENHA COM TAMANHO MINIMO
																		// DE 10 CARACTER
				.build();
	}

	// Popula a lista de usuarios
	@BeforeAll
	public static void setUpClass() {
		for (int i = 0; i < 10; i++) {
			users.add(createUser());
			users.get(i).setId(i + 1);
		}
	}

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void validateFindAllUsers() throws ServiceException {

		LOGGER.info("Starting test of finding all users.");

		// MOCKITO DO METODO (userService.repository).findAll()
		doAnswer(invocation -> {
			return users;
		}).when(userService.repository).findAll();

		assertTrue(userService.findAll().size() > 0, "Validate if the service has users");

		// VERIFICA QUE O MOCK FOI CHAMADO PELO MENOS UMA VEZ
		verify(userService.repository, times(1)).findAll();

		LOGGER.info("Total of users found [ " + userService.findAll().size() + " ].");

		LOGGER.info("End of test of finding all users.");
	}

	@Test // MÉTODO DE CRIAÇÃO
	public void validateUserCreation() throws ServiceException {
		LOGGER.info("Starting test of user creation.");

		// MOCKITO DO METODO (userService.repository).save()
		doAnswer(invocation -> {
			// Usando uma lista para simular um comportamento de uma base de dados

			// Pega o 1° argumento da lista
			User userToAdd = invocation.getArgument(0);

			// Adiciona na minha lista "users"
			users.add(userToAdd);

			// Define o tamanho da lista
			userToAdd.setId(users.size() + 1);

			return userToAdd;

		}).when(userService.repository).save(any(User.class));
		
		
		// MOCKITO DO METODO (userService.userQuery).findUserBy
		doAnswer(invocation -> {

			String userName = invocation.getArgument(0);
			Optional<User> userFound = users.stream().filter(u -> u.getUsername() == userName).findFirst();

			if (userFound.isPresent())
				return userFound.get();
			return null;
		}).when(userService.userQuery).findUserBy(anyString());

		User user = createUser(), userCreated = userService.save(user);

		assertNotNull(userCreated);
		assertNotEquals(userCreated.getId(), 0, "Validate if the new user has a valid id");

		verify(userService.repository, times(1)).save(any(User.class));
		verify(userService.userQuery, atLeastOnce()).findUserBy(anyString());

		LOGGER.info("New user id [" + userCreated.getId() + "].");
		LOGGER.info("New user name [" + userCreated.getName() + "].");
		LOGGER.info("New user e-mail [" + userCreated.getUsername() + "].");
		LOGGER.info("New user passord [" + userCreated.getUserpass() + "].");

		LOGGER.info("End of test of users creation.");
	}

	@Test
	public void validateUserCreationWithDuplicatedUserName() throws ServiceException {
		LOGGER.info("Starting test of user creation.");

		// MOCKITO DO METODO (userService.repository).save()
		doAnswer(invocation -> {
			// Usando uma lista para simular um comportamento de uma base de dados

			// Pega o 1° argumento da lista
			User userToAdd = invocation.getArgument(0);

			// Adiciona na minha lista "users"
			users.add(userToAdd);

			// Define o tamanho da lista
			userToAdd.setId(users.size() + 1);

			return userToAdd;

		}).when(userService.repository).save(any(User.class));

		// MOCKITO DO METODO (userService.userQuery).findUserBy
		doAnswer(invocation -> {

			String userName = invocation.getArgument(0);
			Optional<User> userFound = users.stream().filter(u -> u.getUsername() == userName).findFirst();

			if (userFound.isPresent())
				return userFound.get();
			return null;
		}).when(userService.userQuery).findUserBy(anyString());
		
		User user = users.get(1);
		user.setId(0);
		
		assertThrows(ServiceException.class, () -> userService.save(user), " Validate if an error is thrown with duplicated name");

		verify(userService.repository, times(0)).save(any(User.class));
		verify(userService.userQuery, times(1)).findUserBy(anyString());
		
		LOGGER.info("End of test of users creation.");
	}
	
	
	@Test
	public void validateUserCreationWithDuplicatedShortUserPass() throws ServiceException {
		LOGGER.info("Starting test of user creation.");

		// MOCKITO DO METODO (userService.repository).save()
		doAnswer(invocation -> {
			// Usando uma lista para simular um comportamento de uma base de dados

			// Pega o 1° argumento da lista
			User userToAdd = invocation.getArgument(0);

			// Adiciona na minha lista "users"
			users.add(userToAdd);

			// Define o tamanho da lista
			userToAdd.setId(users.size() + 1);

			return userToAdd;

		}).when(userService.repository).save(any(User.class));

		// MOCKITO DO METODO (userService.userQuery).findUserBy
		doAnswer(invocation -> {

			String userName = invocation.getArgument(0);
			Optional<User> userFound = users.stream().filter(u -> u.getUsername() == userName).findFirst();

			if (userFound.isPresent())
				return userFound.get();
			return null;
		}).when(userService.userQuery).findUserBy(anyString());
		
		User user = createUser();
		user.setUserpass(Faker.instance().internet().password(5, 9));
		
		assertThrows(ServiceException.class, () -> userService.save(user), " Validate if an error is thrown with short password");

		verify(userService.repository, times(0)).save(any(User.class));
		verify(userService.userQuery, times(1)).findUserBy(anyString());
		
		LOGGER.info("End of test of users creation.");
	}

	@Test
	public void validateUserUpdate() throws ServiceException {

		LOGGER.info("Starting test of user update.");

		// SEM MOCK ||||||||||||
//	               \/\/\/\/\/\/		
//		//OBRIGATÓRIAMENTE CRIA UM NOVO USUARIO
//		validateUserCreation();

		// MOCKITO DO METODO (userService.repository).findAll()
		doAnswer(invocation -> {
			return users;
		}).when(userService.repository).findAll();

		doAnswer(invocation -> {
			// Usando uma lista para simular um comportamento de uma base de dados

			// Pega o 1° argumento da lista
			User userToAddOrUpdate = invocation.getArgument(0);

			if (userToAddOrUpdate.getId() == 0) {

				// Adiciona na minha lista "users"
				users.add(userToAddOrUpdate);
				// Define o tamanho da lista
				userToAddOrUpdate.setId(users.size() + 1);
			} else {
				Optional<User> userFound = users.stream().filter(u -> u.getId() == userToAddOrUpdate.getId())
						.findFirst();

				if (userFound.isPresent()) {
					users.set(users.indexOf(userFound.get()), userToAddOrUpdate);
				}
			}

			return userToAddOrUpdate;

		}).when(userService.repository).save(any(User.class));

		// MOCKITO DO METODO (userService.repository).findById()
		doAnswer(invocation -> {

			Long id = invocation.getArgument(0);
			Optional<User> userFound = users.stream().filter(u -> u.getId() == id).findFirst();

			return userFound;
		}).when(userService.repository).findById(anyLong());
		
		// MOCKITO DO METODO (userService.userQuery).findUserBy
		doAnswer(invocation -> {

			String userName = invocation.getArgument(0);
			Optional<User> userFound = users.stream().filter(u -> u.getUsername() == userName).findFirst();

			if (userFound.isPresent())
				return userFound.get();
			return null;
		}).when(userService.userQuery).findUserBy(anyString());


		// Busca o 3° usuario da lista para atualizar o e-mail
		User userToUpdate = userService.findAll().get(2);

		// Atualiza o e-mail do 3° usuario da lista
		userToUpdate.setUsername(Faker.instance().internet().emailAddress());

		// Salva o novo e-mail
		userService.save(userToUpdate);

		// BUCA NA TABELA PRA VERIFICAR SE O TESTE DEU CERTO COM E-MAIL ATUALIZADO DO
		// USUARIO
		assertEquals(userService.findById(userToUpdate.getId()).get().getUsername(), userToUpdate.getUsername(),
				"Validate if the username was update.");

		verify(userService.repository, times(1)).findAll();
		verify(userService.repository, times(1)).save(any(User.class));
		verify(userService.repository, times(1)).findById(anyLong());
		verify(userService.userQuery, times(0)).findUserBy(anyString());
		

		LOGGER.info("End of test of users update.");
	}

	@Test
	public void validateUserDelete() throws ServiceException {

		LOGGER.info("Starting test of user delete.");

		// MOCKITO DO METODO (userService.repository).findAll()
		doAnswer(invocation -> {
			return users;
		}).when(userService.repository).findAll();

		doAnswer(invocation -> {
			User userToDelete = invocation.getArgument(0);
			users.remove(userToDelete);

			return null;
		}).when(userService.repository).delete(any(User.class));

		// MOCKITO DO METODO (userService.repository).delete
		doAnswer(invocation -> {

			Long id = invocation.getArgument(0);
			Optional<User> userFound = users.stream().filter(u -> u.getId() == id).findFirst();

			return userFound;
		}).when(userService.repository).findById(anyLong());

		User userToDelete = userService.findAll().get(2);

		userService.delete(userToDelete);

		// Valida se o delete foi executado com sucesso
		assertTrue(userService.findById(userToDelete.getId()).isEmpty(), "Validate if the user was deleted");

		verify(userService.repository, times(1)).findAll();
		verify(userService.repository, times(1)).delete(any(User.class));
		verify(userService.repository, times(1)).findById(anyLong());

		LOGGER.info("End of test of users delete.");

	}

}