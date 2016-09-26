package it.biblio.framework.data;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Classe che fornisce metodi per la gestione della
 * connessione alla base di dati PostgreSQL.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
public class DataLayerPgsqlImpl implements DataLayer {

	protected DataSource datasource;
    protected Connection c;
    
    
    public DataLayerPgsqlImpl(DataSource datasource){
    	super();
    	this.datasource = datasource;
    	this.c = null;
    	
    }
    
	@Override
	public void init() throws DataLayerException {
		try{
			//connessione al db locale
			c=datasource.getConnection();
		} catch(SQLException ex){
			throw new DataLayerException("Errore di inizializzazione del datalayer",ex);
		}

	}

	@Override
	public void destroy() throws DataLayerException {
		try{
		if(c != null){
			c.close();
			c = null;
		}
		} catch(SQLException ex){
			
		}

	}

	@Override
	public void close() throws Exception {
		destroy();

	}

}
