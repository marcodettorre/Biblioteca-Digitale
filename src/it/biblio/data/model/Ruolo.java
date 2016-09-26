package it.biblio.data.model;

/**
 * Interfaccia del Plain Old Java Object della Entità Ruolo.
 * Contiene metodi get e set per ogni attributo dell'entità.
 * Implementato dal Transfer Object del pattern DAO
 *  
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
public interface Ruolo {

	long getID();
	
	String getNome();
	void setNome(String Nome);
	
	String getDescrizione();
	void setDescrizione(String descrizione);
}
