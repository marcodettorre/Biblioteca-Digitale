package it.biblio.data.impl.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;

import it.biblio.data.model.Opera;
import it.biblio.data.model.Utente;
import it.biblio.framework.data.DataLayerException;

/**
 * Implementazionde del Plain Old Java Object relativo all'interfaccia Opera.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
public class OperaImpl implements Opera{

	private long ID, id_trascrittore, id_acquisitore;
	private Integer numeropagine;
	private Boolean immagini_pubblicate, trascrizioni_pubblicate;
	private String titolo,descrizione, lingua, anno, editore, autore;
	private Utente trascrittore, acquisitore;
	
	/**
	 * Oggetto DAO per l'accesso al DB.
	 */
	private BibliotecaDataLayerPgsqlImpl datalayer;
	
	/**
	 * Costruttore base che genera un oggetto "vuoto".
	 * @param datalayer DAO
	 */
	public OperaImpl(BibliotecaDataLayerPgsqlImpl datalayer){
		titolo = descrizione = editore = lingua = anno = autore ="";
		immagini_pubblicate = trascrizioni_pubblicate = false;
		ID = id_acquisitore= id_trascrittore = numeropagine = 0;
		this.datalayer = datalayer;
	}
	
	/**
	 * Costruttore helper che costruisce l'oggetto dai risultati di una query
	 * 
	 * @param datalayer DAO
	 * @param dati record del DB contenente gli attributi dell'oggetto.
	 * @throws SQLException
	 */
	public OperaImpl(BibliotecaDataLayerPgsqlImpl datalayer, ResultSet dati) throws SQLException{
		immagini_pubblicate = dati.getBoolean("immagini_pubblicate");
		trascrizioni_pubblicate = dati.getBoolean("trascrizioni_pubblicate");
		ID = dati.getLong("ID");
		titolo = dati.getString("titolo");
		descrizione = dati.getString("descrizione");
		editore = dati.getString("editore");
		lingua = dati.getString("lingua");
		anno = dati.getString("anno");
		id_trascrittore = dati.getLong("trascrittore");
		id_acquisitore = dati.getLong("acquisitore");
		numeropagine = dati.getInt("numero_pagine");
		autore = dati.getString("autore");
		this.datalayer = datalayer;
	}
	
	@Override
	public long getID() {
		return ID;
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
	public String getLingua() {
		return lingua;
	}

	@Override
	public void setLingua(String lingua) {
		this.lingua = lingua;
		
	}

	@Override
	public String getAnno() {
		return anno;
	}

	@Override
	public void setAnno(String aaaa) {
		this.anno = aaaa;
	}

	@Override
	public String getEditore() {
		return editore;
	}

	@Override
	public void setEditore(String editore) {
		this.editore = editore;
	}

	@Override
	public String getDescrizione() {
		return descrizione;
	}

	@Override
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
		
	}
	
	@Override
	public Utente getTrascrittore() throws DataLayerException {
		if(this.trascrittore == null){
			this.trascrittore = datalayer.getUtente(this.id_trascrittore);
		}
		return this.trascrittore;
	}

	@Override
	public void setTrascrittore(Utente T) {
		this.trascrittore = T;
		
	}

	@Override
	public Utente getAcquisitore() throws DataLayerException {
		if(this.acquisitore == null){
			this.acquisitore = datalayer.getUtente(this.id_acquisitore);
		}
		return this.acquisitore;
	}

	@Override
	public void setAcquisitore(Utente A) {
		this.acquisitore = A;
		
	}

	@Override
	public Integer getNumeroPagine() {
		return this.numeropagine;
	}

	@Override
	public void setNumeroPagine(Integer numeropagine) {
		this.numeropagine = numeropagine;
		
	}

	@Override
	public Boolean getImmaginiPubblicate() {
		return this.immagini_pubblicate;
	}

	@Override
	public void setImmaginiPubblicate(Boolean b) {
		this.immagini_pubblicate = b;
		
	}

	@Override
	public Boolean getTrascrizioniPubblicate() {
		return this.trascrizioni_pubblicate;
	}

	@Override
	public void setTrascrizioniPubblicate(Boolean b) {
		this.trascrizioni_pubblicate = b;
		
	}
	
	@Override
	public void setAutore(String autore){
		this.autore = autore;
	}
	
	@Override
	public String getAutore(){
		return this.autore;
	}

}
