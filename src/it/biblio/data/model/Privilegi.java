package it.biblio.data.model;

import it.biblio.framework.data.DataLayerException;

/**
 * Interfaccia del Plain Old Java Object della Entità Privilegi.
 * Contiene metodi get e set per ogni attributo dell'entità.
 * Implementato dal Transfer Object del pattern DAO.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
public interface Privilegi {
	
	long getProgressivo() throws DataLayerException;
	
	Utente getUtente()throws DataLayerException;
	void setUtente(Utente U)throws DataLayerException;
	
	Ruolo getRuolo()throws DataLayerException;
	void setRuolo(Ruolo R)throws DataLayerException;

}
