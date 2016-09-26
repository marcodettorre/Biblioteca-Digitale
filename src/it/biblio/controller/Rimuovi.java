package it.biblio.controller;

import java.io.File;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.biblio.data.model.BibliotecaDataLayer;
import it.biblio.data.model.Opera;
import it.biblio.data.model.Pagina;
import it.biblio.data.model.Utente;
import it.biblio.framework.data.DataLayerException;
import it.biblio.framework.result.TemplateManagerException;
import it.biblio.framework.result.TemplateResult;
import it.biblio.framework.utility.ControllerException;

/**
 * Servlet che gestisce le richieste di rimozione dei record delle entità
 * presenti nella base di dati.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
@WebServlet(name="Rimuovi", urlPatterns={"/Rimuovi"})
public class Rimuovi extends BibliotecaBaseController {

	private static final long serialVersionUID = 3263374695997268057L;

	/**
	 * Gestisce la rimozione di un utente dal sistema ed invia l'esito.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 */
	private void action_rimuovi_utente(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException{
		try{
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");
			Boolean successo = true;
			Utente U = datalayer.getUtente(Long.parseLong(request.getParameter("id_utente")));
			datalayer.rimuoviPrivilegiUtente(U.getID());
			if(datalayer.rimuoviUtente(U) == null){
				successo = false;
			}
			request.setAttribute("risultato", successo.toString());
			request.setAttribute("outline_tpl", "");
			
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("controlloAjax.ftl.json", request, response);
		}catch(DataLayerException ex){
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error(request, response);
		}
	}
	/**
	 * Gestisce la rimozione di un'opera dal sistema ed invia l'esito.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 */
	private void action_rimuovi_opera(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException{
		try{
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");
			Boolean successo = true;
			Opera O = datalayer.getOpera(Long.parseLong(request.getParameter("id_opera")));
			List<Pagina> pagine = datalayer.getPagineOpera(O.getID());
			for(Pagina p : pagine){
				if(datalayer.rimuoviPagina(p) == null){
					successo = false;
				}
			}
			if(datalayer.rimuoviOpera(O) == null){
				successo = false;
			}
			
			request.setAttribute("risultato", successo.toString());
			request.setAttribute("outline_tpl", "");
			
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("controlloAjax.ftl.json", request, response);
		} catch(DataLayerException ex){
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error(request, response);
		}
	}
	
	/**
	 * Gestisce la rimozione di una pagina nel sistema ed invia l'esito.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 * @throws ControllerException se occorre un errore nella rimozione della pagina
	 */
	private void action_rimuovi_pagina(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException, ControllerException{
		try{
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");
			Boolean successo = true;
			Pagina P = datalayer.getPagina(Long.parseLong(request.getParameter("id_pagina")));
			if(P == null){
				throw new ControllerException("Attenzione! La pagina che si vuol rimuovere non è presente nella base di dati!!!");
			}
			
			Opera O = datalayer.getOpera(P.getOpera().getID());
			O.setImmaginiPubblicate(false);
			O.setTrascrizioniPubblicate(false);
			O = datalayer.aggiornaOpera(O);
			if(datalayer.rimuoviPagina(P) == null){
				successo = false;
			}
			
			request.setAttribute("risultato", successo.toString());
			request.setAttribute("outline_tpl", "");
			
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("controlloAjax.ftl.json", request, response);
		}catch(Exception ex){
			request.setAttribute("message", ex.getMessage());
			action_error_ajax(request, response);
		}
	}
	
	/**
	 * Gestisce la rimozione di una trascrizione nel sistema ed invia l'esito.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 * @throws ControllerException se occorre un errore nella rimozione della pagina
	 */
	private void action_rimuovi_trascrizione(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException, ControllerException{
		try{
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");
			Pagina P = datalayer.getPagina(Long.parseLong(request.getParameter("id_trascrizione")));
			P.setTrascrizioneValidata(false);
			File f = new File(P.getPathTrascrizione());
			Boolean successo = f.delete();
			if(successo){
				P.setPathTrascrizione("");
				P = datalayer.aggiornaPagina(P);
				if(P == null){
					successo = false;
				}
			}
			
			request.setAttribute("risultato", successo.toString());
			request.setAttribute("outline_tpl", "");
			
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("controlloAjax.ftl.json", request, response);
			
		}catch(Exception ex){
			request.setAttribute("message", ex.getMessage());
			action_error_ajax(request, response);
		}
	}
		
			
	/**
	 * Analizza e smista le richieste ai dovuti metodi della classe.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException se occore un errore non gestito dall'applicazione.
	 */
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try{
			Boolean privilegi = true;
			if(request.getParameter("id_opera") != null){
				if((Boolean)request.getAttribute("admin")){action_rimuovi_opera(request,response);}
				else{privilegi = true;}
			}else if(request.getParameter("id_utente") != null){
				if((Boolean)request.getAttribute("admin")){action_rimuovi_utente(request,response);}
				else{privilegi = true;}
			}else if(request.getParameter("id_pagina") != null){
				if((Boolean)request.getAttribute("admin") || (Boolean)request.getAttribute("revisore_acquisizioni")){action_rimuovi_pagina(request,response);}
				else{privilegi = true;}
			}else if(request.getParameter("id_trascrizione") != null){
				if((Boolean)request.getAttribute("revisore_trascrizioni")){action_rimuovi_trascrizione(request,response);}
				else{privilegi = true;}
			}
			if(!privilegi){
				throw new ControllerException("Accesso alla funzione non consentito!");
			}
		}catch(TemplateManagerException | ControllerException ex){
			request.setAttribute("exception", ex);
			action_error(request, response);
		}

	}

}
