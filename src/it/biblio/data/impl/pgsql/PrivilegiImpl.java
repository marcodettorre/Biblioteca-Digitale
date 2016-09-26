package it.biblio.data.impl.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;

import it.biblio.data.model.Privilegi;
import it.biblio.data.model.Ruolo;
import it.biblio.data.model.Utente;
import it.biblio.framework.data.DataLayerException;

/**
* Implementazionde del Plain Old Java Object relativo all'interfaccia Privilegi
*  
* @author Marco D'Ettorre
* @author Francesco Proietti
*/
public class PrivilegiImpl implements Privilegi {

	private long progressivo, id_utente, id_ruolo;
	private Utente utente;
	private Ruolo ruolo;
	/**
	 * Oggetto DAO per l'accesso al DB.
	 */
	private BibliotecaDataLayerPgsqlImpl datalayer;
	
	/**
	 * Costruttore base che genera un oggetto "vuoto".
	 * @param datalayer
	 */
	public PrivilegiImpl(BibliotecaDataLayerPgsqlImpl datalayer){
		progressivo = id_utente = id_ruolo = 0;
		this.datalayer = datalayer;
	}
	
	/**
	 * costruttore helper che costruisce l'oggetto dai risultati di una query
	 * 
	 * @param datalayer
	 * @param dati
	 * @throws SQLException
	 */
	public PrivilegiImpl(BibliotecaDataLayerPgsqlImpl datalayer, ResultSet dati) throws SQLException{
		progressivo = dati.getLong("progressivo");
		id_utente = dati.getLong("utente");
		id_ruolo = dati.getLong("ruolo");
		this.datalayer = datalayer;
		
	}
	
	@Override
	public long getProgressivo() {
		return progressivo;
	}

	/**
	 * Caricamento Lazy dell'oggetto
	 * @throws DataLayerException 
	 */
	@Override
	public Utente getUtente() throws DataLayerException {
		if(utente == null){
			utente = datalayer.getUtente(id_utente);
		}
		return utente;
	}

	@Override
	public void setUtente(Utente U) {
		utente = U;
	}

	@Override
	public Ruolo getRuolo() throws DataLayerException {
		if(ruolo == null){
			ruolo = datalayer.getRuolo(id_ruolo);
		}
		return ruolo;
	}

	@Override
	public void setRuolo(Ruolo R) {
		ruolo = R;

	}

}
