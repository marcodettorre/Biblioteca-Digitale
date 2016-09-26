package it.biblio.data.impl.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;

import it.biblio.data.model.Commenta;
import it.biblio.data.model.Pagina;
import it.biblio.data.model.Utente;
import it.biblio.framework.data.DataLayerException;

/**
 * 
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
public class CommentaImpl implements Commenta {

	private long progressivo, id_utente, id_trascrizione;
	private Utente utente;
	private Pagina trascrizione;
	private String titolo,commento;
	
	private BibliotecaDataLayerPgsqlImpl datalayer;
	
	
	public CommentaImpl(BibliotecaDataLayerPgsqlImpl datalayer){
		progressivo = id_utente = id_trascrizione = 0;
		titolo = commento = "";
		this.datalayer = datalayer;
	}
	
	public CommentaImpl(BibliotecaDataLayerPgsqlImpl datalayer, ResultSet dati) throws SQLException{
		progressivo = dati.getLong("progressivo");
		id_utente = dati.getLong("utente");
		id_trascrizione = dati.getLong("trascrizione");
		titolo = dati.getString("titolo");
		commento = dati.getString("commento");
		this.datalayer = datalayer;
	}
	@Override
	public long getProgressivo() {
		return progressivo;
	}

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
	public Pagina getTrascrizione() throws DataLayerException {
		if(trascrizione == null){
			trascrizione = datalayer.getPagina(id_trascrizione);
		}
		return trascrizione;
	}

	@Override
	public void setTrascrizione(Pagina T) {
		this.trascrizione = T;

	}

	@Override
	public String getTitolo() {
		return titolo;
	}

	@Override
	public void setTitolo(String titolo) {
		this.titolo = titolo;
		
	}

	@Override
	public String getCommento() {
		return commento;
	}

	@Override
	public void setCommento(String commento) {
		this.commento = commento;
		
	}

}
