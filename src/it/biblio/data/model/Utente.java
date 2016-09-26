package it.biblio.data.model;

/**
 * Interfaccia del Plain Old Java Object della Entità Utente.
 * Contiene metodi get e set per ogni attributo dell'entità.
 * Implementato dal Transfer Object del pattern DAO
 *  
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */

public interface Utente {

	long getID();
	
	String getUsername();
	void setUsername(String username);
	
	String getPassword();
	void setPassword(String password);
	
	String getNome();
	void setNome(String nome);
	
	String getCognome();
	void setCognome(String cognome);
	
	String getEmail();
	void setEmail(String email);
	
}
