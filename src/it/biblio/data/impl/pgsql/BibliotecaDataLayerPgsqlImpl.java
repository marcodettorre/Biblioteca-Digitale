package it.biblio.data.impl.pgsql;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.sun.xml.internal.ws.org.objectweb.asm.Type;

import it.biblio.data.model.BibliotecaDataLayer;
import it.biblio.data.model.Commenta;
import it.biblio.data.model.Opera;
import it.biblio.data.model.Pagina;
import it.biblio.data.model.Privilegi;
import it.biblio.data.model.Ruolo;
import it.biblio.data.model.Utente;
import it.biblio.framework.data.DataLayerException;
import it.biblio.framework.data.DataLayerPgsqlImpl;

/**
 * Classe che funge da oggetto DAO di riferimento per le servlet.
 * 
 * @see it.biblio.data.model.BibliotecaDataLayer
 * @see it.biblio.framework.data.DataLayerPgsqlImpl
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
public class BibliotecaDataLayerPgsqlImpl extends DataLayerPgsqlImpl implements BibliotecaDataLayer {

	private PreparedStatement aUtente, gUtente,gUtenteUsername, gUtenti, rUtente;
	private PreparedStatement aRuolo, gRuolo, gRuoloNome,gRuoliUtente,gRuoli;
	private PreparedStatement gPrivilegi, aPrivilegi, rPrivilegiUtente;
	private PreparedStatement gOpera, aOpera, aggiornaOpera,gOpere, rOpera;
	private PreparedStatement gPagina, aPagina, gPagineOpera, rPagina, aggiornaPagina;
	private PreparedStatement gCommenta, aCommenta;
	
	private Statement gOpereByQuery, gOpereConImmaginiMancanti, gOpereDaTrascrivere, gOpereInPubblicazioneAcquisizioni, gOpereInPubblicazioneTrascrizioni, gOpereConImmaginiNonValidate;
	
	
	public BibliotecaDataLayerPgsqlImpl(DataSource ds) {
		super(ds);
	}
	
	/**
	 * Inizializza la connessione al DB e prepara tutte le query predefinite.
	 * 
	 * @see it.biblio.framework.data.DataLayerPgsqlImpl#init()
	 */
	@Override
	public void init() throws DataLayerException {
		try{
			super.init();
			
			//precompilazione delle query utilizzate nella classe
			aUtente = c.prepareStatement("INSERT INTO Utente(username,password,email,nome,cognome) VALUES (?,?,?,?,?) RETURNING ID");
			gUtente = c.prepareStatement("SELECT * FROM Utente WHERE id = ?");
			gUtenteUsername = c.prepareStatement("SELECT * FROM Utente WHERE username = ?");
			gUtenti = c.prepareStatement("SELECT * FROM Utente ORDER BY(username)");
			rUtente = c.prepareStatement("DELETE FROM Utente WHERE id = ?");
			aRuolo = c.prepareStatement("INSERT INTO Ruolo(nome, descrizione) VALUES (?,?) RETURNING progressivo");
			gRuolo = c.prepareStatement("SELECT * FROM Ruolo WHERE id = ?");
			gRuoloNome = c.prepareStatement("SELECT * FROM Ruolo WHERE LOWER(nome) = LOWER(?)");
			gRuoliUtente = c.prepareStatement("SELECT Ruolo.* FROM Ruolo,Utente,Privilegi WHERE username = ? AND Utente.id = Privilegi.utente AND Privilegi.ruolo = Ruolo.id");
			gRuoli = c.prepareStatement("SELECT * FROM Ruolo");
			gPrivilegi = c.prepareStatement("SELECT * FROM Privilegi WHERE progressivo = ?");
			aPrivilegi = c.prepareStatement("INSERT INTO Privilegi(utente,ruolo) VALUES(?,?) RETURNING progressivo");
			rPrivilegiUtente = c.prepareStatement("DELETE FROM privilegi WHERE utente = ?");
			gOpera = c.prepareStatement("SELECT * FROM Opera WHERE id = ?");
			aOpera = c.prepareStatement("INSERT INTO Opera(titolo,lingua,anno,editore,descrizione,immagini_pubblicate, trascrizioni_pubblicate,acquisitore, trascrittore,numero_pagine, autore) VALUES(?,?,?,?,?,?,?,?,?,?,?) RETURNING ID");
			aggiornaOpera = c.prepareStatement("UPDATE Opera SET titolo = ?, lingua = ?, anno = ?, editore = ?, descrizione = ?, immagini_pubblicate = ?, trascrizioni_pubblicate = ?,acquisitore = ?, trascrittore = ?, numero_pagine = ? WHERE id = ?");
			gOpere = c.prepareStatement("SELECT * FROM Opera ORDER BY(titolo)");
			gPagina = c.prepareStatement("SELECT * FROM Pagina WHERE id = ?");
			aPagina = c.prepareStatement("INSERT INTO Pagina(numero,path_immagine,upload_immagine,immagine_validata,"
					+ "path_trascrizione,ultima_modifica_trascrizione,trascrizione_validata,opera) VALUES(?,?,?,?,?,?,?,?) RETURNING ID");
			rPagina = c.prepareStatement("DELETE FROM Pagina WHERE id = ?");
			aggiornaPagina = c.prepareStatement("UPDATE Pagina SET numero = ?, path_immagine = ?, opera=?, trascrizione_validata = ?, upload_immagine = ?, ultima_modifica_trascrizione = ?, path_trascrizione = ?, immagine_validata=? WHERE id = ?");
			gCommenta = c.prepareStatement("SELECT * FROM Commenta WHERE progressivo = ?");
			aCommenta = c.prepareStatement("");
			gOpereByQuery = c.createStatement();
			gOpereConImmaginiMancanti = c.createStatement();
			gOpereDaTrascrivere = c.createStatement();
			gPagineOpera = c.prepareStatement("SELECT * FROM Pagina WHERE opera = ? ORDER BY(numero)");
			rOpera = c.prepareStatement("DELETE FROM Opera WHERE id = ?");
			gOpereInPubblicazioneAcquisizioni = c.createStatement();
			gOpereInPubblicazioneTrascrizioni = c.createStatement();
			gOpereConImmaginiNonValidate = c.createStatement();
		} catch(SQLException ex){
			throw new DataLayerException("Errore nell'inizializzazione del datalayer della biblioteca", ex);
		}
		
	}
	
	

	@Override
	public Utente creaUtente() {
		return new UtenteImpl(this);
	}

	@Override
	public Utente aggiungiUtente(Utente U) throws DataLayerException {
		UtenteImpl U1 = (UtenteImpl) U;
		try {
			aUtente.setString(1, U1.getUsername());
			aUtente.setString(2, U1.getPassword());
			aUtente.setString(3, U1.getEmail());
			aUtente.setString(4, U1.getNome());
			aUtente.setString(5, U1.getCognome());
			try(ResultSet chiave = aUtente.executeQuery()){
				if (chiave.next()) {
					return getUtente(chiave.getLong("id"));
				}
			}
		} catch (SQLException ex) {
			throw new DataLayerException("Incapace di aggiungere l'utente", ex);
		} 
		return null;
	}

	@Override
	public Utente getUtente(long id) throws DataLayerException {
		Utente ris = null;
		try {
			gUtente.setLong(1, id);
			try(ResultSet rs = gUtente.executeQuery()){
				if (rs.next()) {
					ris = new UtenteImpl(this, rs);
				}
			}
		} catch (SQLException ex) {
			throw new DataLayerException("Incapace di caricare l'utente", ex);
		}
		return ris;
	}

	@Override
	public Ruolo creaRuolo() {
		return new RuoloImpl(this);
	}

	@Override
	public Ruolo getRuolo(long progressivo) throws DataLayerException {
		Ruolo ris = null;
		try {
			gRuolo.setLong(1, progressivo);
			try(ResultSet rs = gRuolo.executeQuery()){
				if(rs.next()){
					ris = new RuoloImpl(this, rs);
				}
			}
		} catch (SQLException ex) {
			throw new DataLayerException("Incapace di caricare il ruolo", ex);
		}
		return ris;
	}

	@Override
	public Ruolo aggiungiRuolo(Ruolo R) throws DataLayerException {
		RuoloImpl RI = (RuoloImpl) R;
		try{
			aRuolo.setString(1, RI.getNome());
			aRuolo.setString(2, RI.getDescrizione());
			try(ResultSet chiave = aRuolo.executeQuery()){
				if(chiave.next()){
					return getRuolo(chiave.getLong("progressivo"));
				}
			}
			
		} catch(SQLException ex){
			throw new DataLayerException("Incapace di aggiungere il ruolo", ex);
		}
		return null;
	}

	@Override
	public Utente getUtenteByUsername(String username) throws DataLayerException {
		Utente ris = null;
		try {
			gUtenteUsername.setString(1, username);
			try(ResultSet rs = gUtenteUsername.executeQuery()){
				if (rs.next()) {
					ris = new UtenteImpl(this, rs);
				}
			}
		} catch (SQLException ex) {
			throw new DataLayerException("Incapace di caricare l'utente", ex);
		}
		return ris;
	}

	@Override
	public Privilegi creaPrivilegio() {
		return new PrivilegiImpl(this);
	}

	@Override
	public Privilegi getPrivilegi(long progressivo) throws DataLayerException {
		Privilegi ris = null;
		try{
			gPrivilegi.setLong(1, progressivo);
			try(ResultSet rs = gPrivilegi.executeQuery()){
				if(rs.next()){
					ris = new PrivilegiImpl(this,rs);
				}
			}
		} catch(SQLException ex){
			throw new DataLayerException("Incapace di caricare il privilegio", ex);
		}
		return ris;
	}

	@Override
	public Privilegi aggiungiPrivilegi(Privilegi P) throws DataLayerException {
		Privilegi PI = (PrivilegiImpl) P;
		try{
			aPrivilegi.setLong(1, PI.getUtente().getID());
			aPrivilegi.setLong(2, PI.getRuolo().getID());
			try(ResultSet chiave = aPrivilegi.executeQuery()){
				if(chiave.next()){
					return getPrivilegi(chiave.getLong("progressivo"));
				}
			}
		} catch(SQLException ex){
			throw new DataLayerException("Incapace di aggiungere il privilegio", ex);
		}
		return null;
	}

	@Override
	public Privilegi rimuoviPrivilegiUtente(long id_utente) throws DataLayerException {
		try{
			this.rPrivilegiUtente.setLong(1, id_utente);
			this.rPrivilegiUtente.executeUpdate();
		}catch(SQLException ex){
			throw new DataLayerException("Incapace di rimuovere il privilegio dell'utente", ex);
		}
		return null;
	}

	@Override
	public Opera creaOpera() {
		return new OperaImpl(this);
		
	}

	@Override
	public Opera getOpera(long id) throws DataLayerException {
		Opera ris = null;
		try{
			gOpera.setLong(1,id);
			try(ResultSet rs = gOpera.executeQuery()){
				if(rs.next()){
					ris = new OperaImpl(this, rs);
				}
			}
		} catch (SQLException ex){
			throw new DataLayerException("Incapace di caricare l'Opera", ex);
		}
		return ris;
	}

	@Override
	public Opera aggiungiOpera(Opera O) throws DataLayerException {
		OperaImpl OI = (OperaImpl) O;
		try{
			aOpera.setString(1, OI.getTitolo());
			aOpera.setString(2, OI.getLingua());
			aOpera.setString(3, OI.getAnno());
			aOpera.setString(4, OI.getEditore());
			aOpera.setString(5, OI.getDescrizione());
			aOpera.setBoolean(6, OI.getImmaginiPubblicate());
			aOpera.setBoolean(7, OI.getTrascrizioniPubblicate());
			if(OI.getAcquisitore() != null){
				aOpera.setLong(8,OI.getAcquisitore().getID());
			}else{
				aOpera.setNull(8,Type.LONG);
			}
			if(OI.getTrascrittore() != null){
				aOpera.setLong(9, OI.getTrascrittore().getID());
			}else{
				aOpera.setNull(9, Type.LONG);
			}
			aOpera.setInt(10, OI.getNumeroPagine());
			aOpera.setString(11, OI.getAutore());
			try(ResultSet chiave = aOpera.executeQuery()){
				if(chiave.next()){
					return getOpera(chiave.getLong("ID"));
				}
			}
		} catch(SQLException ex){
			throw new DataLayerException("Incapace di aggiungere l'Opera", ex);
		}
		return null;
	}

	
	@Override
	public List<Opera> getOpereByQuery(Opera O) throws DataLayerException {
		Opera OI = (OperaImpl) O; 
		List<Opera> ris = new ArrayList<Opera>();
		try{
			String query = "SELECT * FROM Opera WHERE immagini_pubblicate = "+OI.getImmaginiPubblicate().toString()+" AND trascrizioni_pubblicate = "+OI.getTrascrizioniPubblicate().toString();
			
			if(O.getID() != 0){
				query = query + " AND id = "+O.getID();
			}
			if(!O.getAnno().equals("")){
				query = query + " AND LOWER(anno) = LOWER('"+O.getAnno()+"')";
			}
			if(!O.getLingua().equals("")){
				query = query + " AND LOWER(lingua) = LOWER('"+O.getLingua()+"')";
			}
			if(!O.getEditore().equals("")){
				query = query + " AND LOWER(editore) LIKE LOWER('%"+O.getEditore()+"%')";
			}
			if(!O.getTitolo().equals("")){
				query = query + " AND LOWER(titolo) LIKE LOWER('%"+O.getTitolo()+"%')";
			}
			if(O.getAcquisitore() != null){
				query = query + " AND acquisitore = "+O.getAcquisitore().getID();
			}
			if(O.getTrascrittore() != null){
				query = query + " AND trascrittore = "+O.getTrascrittore().getID();
			}
			if(O.getNumeroPagine() != 0){
				query = query + " AND numero_pagine = " + O.getNumeroPagine();
			}
			if(!O.getAutore().equals("")){
				query = query + " AND LOWER(autore) LIKE LOWER('%" + O.getAutore() + "%')";
			}
			
			try(ResultSet rs = gOpereByQuery.executeQuery(query+" ORDER BY(titolo)")){
				while(rs.next()){
					ris.add(new OperaImpl(this, rs));
				}
			}
		} catch (SQLException ex){
			throw new DataLayerException("Incapace di caricare le opere", ex);

		}
		return ris;
	}

	@Override
	public Pagina creaPagina() {
		return new PaginaImpl(this);
	}

	@Override
	public Pagina getPagina(long id) throws DataLayerException {
		Pagina ris = null;
		try{
			gPagina.setLong(1,id);
			try(ResultSet rs = gPagina.executeQuery()){
				if(rs.next()){
					ris = new PaginaImpl(this, rs);
				}
			}
		} catch (SQLException ex){
			throw new DataLayerException("Incapace di caricare la pagina", ex);

		}
		return ris;
	}

	@Override
	public Pagina aggiungiPagina(Pagina P) throws DataLayerException {
		PaginaImpl PI = (PaginaImpl) P;
		try{
			aPagina.setString(1, PI.getNumero());
			aPagina.setString(2,PI.getPathImmagine());
			aPagina.setTimestamp(3, PI.getUploadImmagine());
			aPagina.setBoolean(4, PI.getImmagineValidata());
			aPagina.setString(5, PI.getPathTrascrizione());
			aPagina.setTimestamp(6, PI.getUltimaModificaTrascrizione());
			aPagina.setBoolean(7, PI.getTrascrizioneValidata());
			aPagina.setLong(8, PI.getOpera().getID());
			try(ResultSet chiave = aPagina.executeQuery()){
				if(chiave.next()){
					return getPagina(chiave.getLong("ID"));
				}
			}
		} catch(SQLException ex){
			throw new DataLayerException("Incapace di aggiungere la pagina", ex);

		}
		return null;
	}

	@Override
	public Commenta creaCommenta() {
		return new CommentaImpl(this);
	}

	@Override
	public Commenta getCommenta(long progressivo) throws DataLayerException {
		Commenta ris = null;
		try{
			gCommenta.setLong(1,progressivo);
			try(ResultSet rs = gCommenta.executeQuery()){
				if(rs.next()){
					ris = new CommentaImpl(this, rs);
				}
			}
		} catch (SQLException ex){
			throw new DataLayerException("Incapace di caricare il commento", ex);
		}
		return ris;
	}

	@Override
	public Commenta aggiungiCommenta(Commenta C) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Opera aggiornaOpera(Opera O) throws DataLayerException {
		try{
			aggiornaOpera.setString(1, O.getTitolo());
			aggiornaOpera.setString(2, O.getLingua());
			aggiornaOpera.setString(3, O.getAnno());
			aggiornaOpera.setString(4, O.getEditore());
			aggiornaOpera.setString(5, O.getDescrizione());
			aggiornaOpera.setBoolean(6, O.getImmaginiPubblicate());
			aggiornaOpera.setBoolean(7, O.getTrascrizioniPubblicate());
			if(O.getAcquisitore() != null){
				aggiornaOpera.setLong(8,O.getAcquisitore().getID());
			}else{
				aggiornaOpera.setNull(8,Types.INTEGER);
			}
			if(O.getTrascrittore() != null){
				aggiornaOpera.setLong(9, O.getTrascrittore().getID());
			}else{
				aggiornaOpera.setNull(9, Types.INTEGER);
			}
			aggiornaOpera.setInt(10, O.getNumeroPagine());
			aggiornaOpera.setLong(11, O.getID());
			if(aggiornaOpera.executeUpdate() == 1){
				return getOpera(O.getID());
			}
		} catch(SQLException ex){
			throw new DataLayerException("Incapace di aggiornare l'opera", ex);
		}
		return null;
	}

	@Override
	public Ruolo getRuoloByNome(String nome) throws DataLayerException {
		Ruolo ris = null;
		try{
			gRuoloNome.setString(1, nome);
			try(ResultSet rs = gRuoloNome.executeQuery()){
				if(rs.next()){
					ris = new RuoloImpl(this, rs);
				}
			}
		} catch(SQLException ex){
			throw new DataLayerException("Incapace di caricare il ruolo", ex);
		}
		return ris;
	}

	@Override
	public List<Ruolo> getListaRuoliUtente(Utente U) throws DataLayerException {
		List<Ruolo> ris = new ArrayList<Ruolo>();
		try{
			this.gRuoliUtente.setString(1, U.getUsername());
			try(ResultSet rs = gRuoliUtente.executeQuery()){
				while(rs.next()){
					ris.add(new RuoloImpl(this,rs));
				}
			}
		}catch(SQLException ex){
			throw new DataLayerException("Incapace di caricare i ruoli", ex);
		}
		return ris;
	}

	@Override
	public List<Opera> getOpereDaTrascrivere() throws DataLayerException {
		List<Opera> ris = new ArrayList<Opera>();
		try{
			try(ResultSet rs = this.gOpereDaTrascrivere.executeQuery("SELECT * FROM Opera WHERE trascrittore IS NULL AND immagini_pubblicate=true")){
				while(rs.next()){
					ris.add(new OperaImpl(this,rs));
				}
			}
		}catch(SQLException ex){
			throw new DataLayerException("Incapace di caricare le opere", ex);
		}
		return ris;
	}

	@Override
	public List<Pagina> getPagineOpera(long id_opera) throws DataLayerException {
		List<Pagina> ris = new ArrayList<Pagina>();
		try{
			this.gPagineOpera.setLong(1, id_opera);
			try(ResultSet rs = this.gPagineOpera.executeQuery()){
				while(rs.next()){
					ris.add(new PaginaImpl(this,rs));
				}
			}
		}catch(SQLException ex){
			throw new DataLayerException("Incapace di caricare le pagine", ex);
		}
		return ris;
	}
	
	@Override
	public List<Utente> getTuttiGliUtenti() throws DataLayerException {
		List<Utente> ris = new ArrayList<Utente>();
		try(ResultSet rs = this.gUtenti.executeQuery()){
			while(rs.next()){
				ris.add(new UtenteImpl(this,rs));
			}
		} catch (SQLException ex) {
			throw new DataLayerException("Incapace di caricare tutti gli utenti", ex);
		}
		return ris;
	}

	@Override
	public List<Ruolo> getTuttiIRuoli() throws DataLayerException {
		List<Ruolo> ris = new ArrayList<Ruolo>();
		try(ResultSet rs = this.gRuoli.executeQuery()){
			while(rs.next()){
				ris.add(new RuoloImpl(this,rs));
			}
		} catch (SQLException ex) {
			throw new DataLayerException("Incapace di caricare tutti i ruoli", ex);
		}
		return ris;
	}

	@Override
	public List<Opera> getTutteLeOpere() throws DataLayerException {
		List<Opera> ris = new ArrayList<Opera>();
		try(ResultSet rs = this.gOpere.executeQuery()){
			while(rs.next()){
				ris.add(new OperaImpl(this,rs));
			}
		} catch (SQLException ex) {
			throw new DataLayerException("Incapace di caricare tutte le opere", ex);
		}
		return ris;
	}
	
	private void rimuoviFilesPagina(Pagina P){
		if(P!= null){
			if(P.getPathImmagine() != null){
				File f = new File(P.getPathImmagine());
				if(f.exists()){
					f.delete();
				}
			}
			if(P.getPathTrascrizione() != null){
				File f = new File(P.getPathImmagine());
				if(f.exists()){
					f.delete();
				}
			}
		}
	}
	
	@Override
	public Pagina rimuoviPagina(Pagina P) throws DataLayerException {
		try{
			this.rPagina.setLong(1, P.getID());
			if(this.rPagina.executeUpdate()==1){
				rimuoviFilesPagina(P);
				return P;
			}
		}catch (SQLException ex) {
			throw new DataLayerException("Incapace di eliminare la pagina richiesta", ex);
		}
		return null;
	}
	

	@Override
	public Opera rimuoviOpera(Opera O) throws DataLayerException {
		try{
			this.rOpera.setLong(1, O.getID());
			if(this.rOpera.executeUpdate() == 1){
				return O;
			}
		}catch (SQLException ex) {
			throw new DataLayerException("Incapace di eliminare l'opera richiesta", ex);
		}
		return null;
	}

	@Override
	public List<Opera> getOpereInPubblicazioneAcquisizioni() throws DataLayerException {
		List<Opera> ris = new ArrayList<Opera>();
		String query = "SELECT Opera.* FROM Opera JOIN Pagina ON (Pagina.opera=Opera.id) "
				+ "WHERE pagina.immagine_validata = true AND opera.immagini_pubblicate = false "
				+ "GROUP BY(Opera.id) HAVING COUNT(*) = Opera.numero_pagine";
		try(ResultSet rs = this.gOpereInPubblicazioneAcquisizioni.executeQuery(query)){
			while(rs.next()){
				ris.add(new OperaImpl(this,rs));
			}
		} catch (SQLException e) {
			throw new DataLayerException("Incapace di ottenere le opere in pubblicazione acquisizioni", e);
		}
		return ris;
	}


	@Override
	public List<Opera> getOpereInPubblicazioneTrascrizioni() throws DataLayerException {
		List<Opera> ris = new ArrayList<Opera>();
		String query = "SELECT Opera.* FROM Opera JOIN Pagina ON (Pagina.opera=Opera.id) "
				+ "WHERE immagini_pubblicate = true AND trascrizioni_pubblicate = false "
				+ "AND Pagina.trascrizione_validata = true "
				+ "GROUP BY(Opera.id) HAVING COUNT(*) = Opera.numero_pagine";
		try(ResultSet rs = this.gOpereInPubblicazioneAcquisizioni.executeQuery(query)){
			while(rs.next()){
				ris.add(new OperaImpl(this,rs));
			}
		} catch (SQLException e) {
			throw new DataLayerException("Incapace di ottenere le opere in pubblicazione acquisizioni", e);
		}
		return ris;
	}

	@Override
	public Pagina aggiornaPagina(Pagina P) throws DataLayerException {
		try {
			this.aggiornaPagina.setString(1, P.getNumero());
			this.aggiornaPagina.setString(2, P.getPathImmagine());
			this.aggiornaPagina.setLong(3, P.getOpera().getID());
			this.aggiornaPagina.setBoolean(4, P.getTrascrizioneValidata());
			this.aggiornaPagina.setTimestamp(5, P.getUploadImmagine());
			this.aggiornaPagina.setTimestamp(6, P.getUltimaModificaTrascrizione());
			this.aggiornaPagina.setString(7, P.getPathTrascrizione());
			this.aggiornaPagina.setBoolean(8, P.getImmagineValidata());
			this.aggiornaPagina.setLong(9, P.getID());
			if(this.aggiornaPagina.executeUpdate() == 1){
				return this.getPagina(P.getID());
			}
		} catch (SQLException ex) {
			throw new DataLayerException("Incapace di aggiornare l'opera", ex);
		}
		return null;
	}


	@Override
	public Utente rimuoviUtente(Utente U) throws DataLayerException {
		try{
			this.rUtente.setLong(1, U.getID());
			if(this.rUtente.executeUpdate() == 1){
				return U;
			}
		}catch (SQLException ex) {
			throw new DataLayerException("Incapace di eliminare l'utente richiesto", ex);
		}
		return null;
	}
	

	@Override
	public List<Opera> getOpereConImmaginiNonValidate() throws DataLayerException {
		List<Opera> result = new ArrayList<>();
		String query = "SELECT Opera.* FROM pagina JOIN opera ON (pagina.opera=opera.id) "
		+ "WHERE pagina.immagine_validata = false GROUP BY(opera.id) ORDER BY(opera.titolo)";
		try(ResultSet rs = this.gOpereConImmaginiNonValidate.executeQuery(query)){
			while(rs.next()){
				result.add(this.getOpera(rs.getLong("id")));
			}
		} catch (SQLException ex) {
			throw new DataLayerException("Incapace di ottenere le opere con immagini non validate", ex);
		}
		return result;
	}
	
	@Override
	public List<Opera> getOpereConImmaginiMancanti() throws DataLayerException {
		List<Opera> result = new ArrayList<>();
		String query = "SELECT Opera.* FROM pagina RIGHT JOIN opera ON (pagina.opera=opera.id) "
				+"GROUP BY (opera.id) HAVING COUNT(*) < opera.numero_pagine";
		try(ResultSet rs = this.gOpereConImmaginiMancanti.executeQuery(query)){
			while(rs.next()){
				result.add(this.getOpera(rs.getLong("id")));
			}
		} catch(SQLException ex){
			throw new DataLayerException("Incapace di ottenere le opere con immagini mancanti", ex);
		}
		return result;
	}
	
/*
	@Override
	public List<Opera> getOpereConTrascrizioniNonValidate() throws DataLayerException {
		List<Opera> result = new ArrayList<>();
		String query = "SELECT Opera.* FROM pagina JOIN opera ON (pagina.opera=opera.id) "
		+ "WHERE opera.immagini_pubblicate = true AND opera= false GROUP BY(opera.id) ORDER BY(opera.titolo)";
		try(ResultSet rs = this.gOpereConImmaginiNonValidate.executeQuery(query)){
			while(rs.next()){
				result.add(this.getOpera(rs.getLong("id")));
			}
		} catch (SQLException ex) {
			throw new DataLayerException("Incapace di ottenere le opere con immagini non validate", ex);
		}
		return result;
	}
*/
	/**
	 * chiude tutti gli statement e chiude la connessione al DB.
	 * 
	 * @see it.biblio.framework.data.DataLayerPgsqlImpl#destroy()
	 */
	@Override
	public void destroy() throws DataLayerException {
		try {
			this.aUtente.close();
			this.aCommenta.close();
			this.aOpera.close();
			this.aPagina.close();
			this.aPrivilegi.close();
			this.aRuolo.close();
			this.aggiornaOpera.close();
			this.aggiornaPagina.close();
			this.gCommenta.close();
			this.gOpera.close();
			this.gOpereByQuery.close();
			this.gOpere.close();
			this.gOpereInPubblicazioneAcquisizioni.close();
			this.gOpereInPubblicazioneTrascrizioni.close();
			this.gOpereConImmaginiNonValidate.close();
			this.gOpereConImmaginiMancanti.close();
			this.gPagina.close();
			this.gPagineOpera.close();
			this.gPrivilegi.close();
			this.gRuoliUtente.close();
			this.gRuoli.close();
			this.gRuolo.close();
			this.gRuoloNome.close();
			this.gUtente.close();
			this.gUtenteUsername.close();
			this.gUtenti.close();
			this.gOpereDaTrascrivere.close();
			this.rPrivilegiUtente.close();
			this.rPagina.close();
			this.rOpera.close();
			this.rUtente.close();
		} catch (SQLException e) {
			
		}
		super.destroy();
	}


}
