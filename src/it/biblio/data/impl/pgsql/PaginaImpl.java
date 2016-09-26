package it.biblio.data.impl.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import it.biblio.data.model.Opera;
import it.biblio.data.model.Pagina;
import it.biblio.framework.data.DataLayerException;

/**
 * Implementazionde del Plain Old Java Object relativo all'interfaccia Pagina
 *  
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
public class PaginaImpl implements Pagina {

	private long ID, id_opera;
	private String numero,path_immagine, path_trascrizione;
	private Boolean immagine_validata, trascrizione_validata;
	private Timestamp upload_immagine, ultima_modifica_trascrizione;
	private Opera opera;
	
	/**
	 * Oggetto DAO per l'accesso al DB.
	 */
	private BibliotecaDataLayerPgsqlImpl datalayer;
	
	/**
	 * Costruttore base che genera un oggetto "vuoto".
	 * @param datalayer
	 */
	public PaginaImpl(BibliotecaDataLayerPgsqlImpl datalayer){
		ID = 0;
		numero = path_immagine = path_trascrizione = "";
		immagine_validata = trascrizione_validata = false;
		upload_immagine = ultima_modifica_trascrizione = null;
		id_opera = 0;
		this.datalayer = datalayer;
		
	}
	
	/**
	 * costruttore helper che costruisce l'oggetto dai risultati di una query
	 * 
	 * @param datalayer
	 * @param dati
	 * @throws SQLException
	 */
	public PaginaImpl(BibliotecaDataLayerPgsqlImpl datalayer, ResultSet dati) throws SQLException{
		ID = dati.getLong("ID");
		numero = dati.getString("numero");
		path_immagine = dati.getString("path_immagine");
		upload_immagine = dati.getTimestamp("upload_immagine");
		immagine_validata = dati.getBoolean("immagine_validata");
		path_trascrizione = dati.getString("path_trascrizione");
		ultima_modifica_trascrizione = dati.getTimestamp("ultima_modifica_trascrizione");
		trascrizione_validata = dati.getBoolean("trascrizione_validata");
		id_opera = dati.getLong("opera");
		this.datalayer = datalayer;
	}
	
	@Override
	public long getID() {
		return ID;
	}

	@Override
	public String getNumero() {
		return numero;
	}

	@Override
	public void setNumero(String numero) {
		this.numero = numero;

	}

	@Override
	public String getPathImmagine() {
		return this.path_immagine;
	}

	@Override
	public void setPathImmagine(String path) {
		this.path_immagine = path;

	}

	@Override
	public String getPathTrascrizione() {
		return this.path_trascrizione;
	}

	@Override
	public void setPathTrascrizione(String path) {
		this.path_trascrizione = path;

	}

	@Override
	public Timestamp getUploadImmagine() {
		return this.upload_immagine;
	}

	@Override
	public void setUploadImmagine(Timestamp ts) {
		this.upload_immagine = ts;
	}

	@Override
	public Timestamp getUltimaModificaTrascrizione() {
		return this.ultima_modifica_trascrizione;
	}

	@Override
	public void setUltimaModificaTrascrizione(Timestamp ts) {
		this.ultima_modifica_trascrizione = ts;
	}

	@Override
	public Boolean getImmagineValidata() {
		return this.immagine_validata;
	}

	@Override
	public void setImmagineValidata(Boolean b) {
		this.immagine_validata = b;

	}

	@Override
	public Boolean getTrascrizioneValidata() {
		return this.trascrizione_validata;
	}

	@Override
	public void setTrascrizioneValidata(Boolean b) {
		this.trascrizione_validata = b;
	}

	/**
	 * Caricamento Lazy dell'oggetto
	 * @throws DataLayerException 
	 */
	@Override
	public Opera getOpera() throws DataLayerException {
		if(this.opera == null){
			this.opera = datalayer.getOpera(id_opera);
		}
		return this.opera;
	}

	@Override
	public void setOpera(Opera O) {
		this.opera = O;

	}


}
