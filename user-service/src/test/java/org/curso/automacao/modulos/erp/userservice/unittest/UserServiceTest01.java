package org.curso.automacao.modulos.erp.userservice.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.curso.automacao.modulos.erp.userservice.exceptions.ServiceException;
import org.curso.automacao.modulos.erp.userservice.impl.User;
import org.curso.automacao.modulos.erp.userservice.impl.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.javafaker.Faker;


public class UserServiceTest01 extends BaseTest {
	
	
	// CLASSE DE LOG PARA OS TESTES
	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceTest01.class);

	@Autowired
	private UserService userService;
	
	@Test
	public void validateFindAllUsers() throws ServiceException {
		
		LOGGER.info("Starting test of finding all users.");
		
		assertTrue(userService.findAll().size() > 0, "Validate if the service has users");
		
		LOGGER.info("Total of users found [ " + userService.findAll().size() + " ].");
		
		LOGGER.info("End of test of finding all users.");
	}
	
	@Test
	public void validateUserCreation() throws ServiceException {
		LOGGER.info("Starting test of user creation.");
		
		User user = User.builder()
						.name(Faker.instance().name().fullName())// O FAKER AJUDA NA MASSA DE DADOS CRIANDO USUARIO
						.roles("ROLE_USER")
						.username(Faker.instance().internet().emailAddress())// O FAKER AJUDA NA MASSA DE DADOS CRIANDO EMAIL
						.userpass(Faker.instance().internet().password(10, 15))// O FAKER AJUDA CRIANDO SENHA COM TAMANHO MINIMO DE 10 CARACTER
						.build(),
						
						userCreated = userService.save(user);	
		
		assertNotNull(userCreated);
		assertNotEquals(userCreated.getId(), 0, "Validate if the new user has a valid id");
		
		LOGGER.info("New user id [" + userCreated.getId() + "].");
		LOGGER.info("New user name [" + userCreated.getName()+ "].");
		LOGGER.info("New user e-mail [" + userCreated.getUsername() + "].");
		LOGGER.info("New user passord [" + userCreated.getUserpass() + "].");
		
		LOGGER.info("End of test of users creation.");
	}
	
	@Test
	public void validateUserUpdate() throws ServiceException {
		
		LOGGER.info("Starting test of user update.");
		
		//OBRIGATÓRIAMENTE CRIA UM NOVO USUARIO
		validateUserCreation();
		
		// Busca o 3° usuario da lista para atualizar o e-mail
		User userToUpdate = userService.findAll().get(2);
		
		// Atualiza o e-mail do 3° usuario da lista
		userToUpdate.setUsername(Faker.instance().internet().emailAddress());
		
		//Salva o novo e-mail
		userService.save(userToUpdate);
		
		// BUCA NA TABELA PRA VERIFICAR SE O TESTE DEU CERTO COM E-MAIL ATUALIZADO DO USUARIO
		assertEquals(userService.findById(userToUpdate.getId()).get().getUsername(), userToUpdate.getUsername(), "Validate if the username was update.");
		
		LOGGER.info("End of test of users update.");
	}
	
	@Test
	public void validateUserDelete() throws ServiceException {
		
		LOGGER.info("Starting test of user delete.");
		
		validateUserCreation();
		
		User userToDelete = userService.findAll().get(2);
		
		userService.delete(userToDelete);
		
		// Valida se o delete foi executado com sucesso
		assertTrue(userService.findById(userToDelete.getId()).isEmpty(), "Validate if the user was deleted");
		
		LOGGER.info("End of test of users delete.");
		
	}
	
	
}
