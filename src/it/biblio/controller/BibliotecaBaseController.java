package it.biblio.controller;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import it.biblio.data.impl.pgsql.BibliotecaDataLayerPgsqlImpl;
import it.biblio.data.model.BibliotecaDataLayer;
import it.biblio.data.model.Opera;
import it.biblio.data.model.Utente;
import it.biblio.framework.data.DataLayerException;
import it.biblio.framework.result.*;

/**
 * Servlet astratta che permette di inizializzare la connessione al DB.
 * Implementata da tutte le servlet che vogliono accedere al DB.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
@SuppressWarnings("serial")
public abstract class BibliotecaBaseController extends HttpServlet {
	/**
	 * risorsa definita in web.xml
	 */
	@Resource(name = "jdbc/bibliodb")
	private DataSource ds;
	
	
	/**
	 * Punto di partenza della servlet per gestire le sue richieste.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException se occorre un errore non gestito
	 */
	protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException;
	
	
	/**
	 * Restituisce una stringa di errore da gestire nella funzione success della chiamata AJAX lato client.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 */
	protected void action_error_ajax(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException {
		String messaggio = (String) request.getAttribute("message");
		
		request.setAttribute("risultato", messaggio);
		request.setAttribute("outline_tpl", "");
		
		TemplateResult tr = new TemplateResult(getServletContext());
		tr.activate("controlloAjax.ftl.json", request, response);
	}
	
	/**
	 * Richiama la pagina di errore specificando li problema riscontrato.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected void action_error(HttpServletRequest request, HttpServletResponse response) {
		if (request.getAttribute("exception") != null) {
			(new FailureResult(getServletContext())).activate((Exception) request.getAttribute("exception"), request,
					response);
		} else {
			(new FailureResult(getServletContext())).activate((String) request.getAttribute("message"), request,
					response);
		}
	}
	
	
	/**
	 * Istanzia il modello dei dati da passare a Freemarker e chiama i metodi del package framework.result
	 * per attivare la response e inviare le pagine come output.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 * @throws DataLayerException se occorre un errore nella logica dei dati
	 */
	protected void action_result(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException, DataLayerException{
		BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");
		if((Boolean)request.getAttribute("acquisitore") == true){
			List<Opera> opere = datalayer.getOpereConImmaginiMancanti();
			request.setAttribute("opere_non_pubblicate", opere);
		}
		else if((Boolean)request.getAttribute("trascrittore") == true){
			Utente U = datalayer.getUtenteByUsername((String) request.getAttribute("nomeutente"));
			Opera O = datalayer.creaOpera();
			O.setImmaginiPubblicate(true);
			O.setTrascrizioniPubblicate(false);
			O.setTrascrittore(U);
			List<Opera> opere = datalayer.getOpereByQuery(O);
			request.setAttribute("opere_in_trascrizione",opere);
			opere = datalayer.getOpereDaTrascrivere();
			request.setAttribute("opere_da_trascrivere", opere);
		}
		else if((Boolean) request.getAttribute("revisore_acquisizioni") == true){
			Opera query = datalayer.creaOpera();
			query.setImmaginiPubblicate(false);
			List<Opera> opere = datalayer.getOpereByQuery(query);
		}
		else if((Boolean) request.getAttribute("revisore_trascrizioni") == true){
			Opera query = datalayer.creaOpera();
			query.setTrascrizioniPubblicate(false);
			List<Opera> opere = datalayer.getOpereByQuery(query);
		}
		
		TemplateResult res = new TemplateResult(getServletContext());
		request.setAttribute("strip_slashes", new SplitSlashesFmkExt() );
		res.activate("", request, response);
	}
	
	/**
	 * Inizializza la connessione al DB e la rende disponibile come attributo della 
	 * servlet <b>request</b>.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 */
	private void processBaseRequest(HttpServletRequest request, HttpServletResponse response){
		try(BibliotecaDataLayer datalayer = new BibliotecaDataLayerPgsqlImpl(ds)){
			datalayer.init();
			request.setAttribute("datalayer", datalayer);
			processRequest(request, response);
		}catch(Exception ex){
			ex.printStackTrace();
			(new FailureResult(getServletContext())).activate(
					(ex.getMessage() != null || ex.getCause() != null) ? ex.getMessage() : ex.getCause().getMessage() , request, response);
		}
	}
	
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processBaseRequest(request, response);
    }
	
	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processBaseRequest(request, response);
    }

}
