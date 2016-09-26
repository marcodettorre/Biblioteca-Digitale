package it.biblio.data.model;

import it.biblio.framework.data.DataLayerException;

/**
* Interfaccia del Plain Old Java Object della Entità Commenta.
* Contiene metodi get e set per ogni attributo dell'entità.
* Transfer Object del pattern DAO
*  
* @author Marco D'Ettorre
* @author Francesco Proietti*
*/
public interface Commenta {

	long getProgressivo();
	
	Utente getUtente() throws DataLayerException;
	void setUtente(Utente U);
	
	Pagina getTrascrizione() throws DataLayerException;
	void setTrascrizione(Pagina T);
	
	String getTitolo();
	void setTitolo(String titolo);
	
	String getCommento();
	void setCommento(String commento);
}
