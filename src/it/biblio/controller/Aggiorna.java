package it.biblio.controller;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.biblio.data.model.BibliotecaDataLayer;
import it.biblio.data.model.Opera;
import it.biblio.data.model.Pagina;
import it.biblio.data.model.Privilegi;
import it.biblio.framework.data.DataLayerException;
import it.biblio.framework.result.TemplateManagerException;
import it.biblio.framework.result.TemplateResult;
import it.biblio.framework.utility.ControllerException;
import it.biblio.framework.utility.SecurityLayer;

/**
 * Servlet che gestisce le richieste di aggiornamento dei record delle entità
 * presenti nella base di dati.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
@WebServlet(name="Aggiorna", urlPatterns={"/Aggiorna"})
public class Aggiorna extends BibliotecaBaseController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4549029224361299450L;

	/**
	 * Funzione che aggiorna l'opera pubblicandola con tutte le sue acquisizioni
	 * o trascrizioni, in base alla richiesta
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException errore nella logica del template manager 
	 */
	private void action_pubblica(HttpServletRequest request, HttpServletResponse response, String tipo) throws ControllerException, TemplateManagerException{
		try{
			long id_opera = SecurityLayer.checkNumeric(request.getParameter("id_opera"));
			Boolean successo = true;
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");
			Opera O = datalayer.getOpera(id_opera);
			if(tipo.equals("trascrizioni")){
				O.setTrascrizioniPubblicate(true);
			}
			else{
				O.setImmaginiPubblicate(true);
			}
			if(datalayer.aggiornaOpera(O) == null){
				successo = false;
			}
			request.setAttribute("risultato", successo.toString());
			request.setAttribute("outline_tpl", "");
			
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("controlloAjax.ftl.json", request, response);
		} catch(DataLayerException ex){
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error(request, response);
		} catch(NumberFormatException ex){
			throw new ControllerException(ex.getMessage());
		}
	}

	/**
	 * Gestisce la rimozione di un'opera dal sistema
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
	 * Funzione che aggiorna i privilegi utente rimuovendo quelli già presenti 
	 * per inserire quelli ricevuti dalla richiesta.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException errore nella logica del template manager 
	 */
	private void action_aggiorna_privilegi_utente(HttpServletRequest request, HttpServletResponse response) throws ControllerException, TemplateManagerException{
		
		try{
			long id_utente = SecurityLayer.checkNumeric(request.getParameter("id_utente"));
			if(request.getParameter("nome_ruolo")== null){
				throw new ControllerException("campo nome ruolo non corretto!");
			}
			Boolean successo = true;
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");
			
			datalayer.rimuoviPrivilegiUtente(id_utente);
			
			Privilegi P = datalayer.creaPrivilegio();
			P.setRuolo(datalayer.getRuoloByNome(request.getParameter("nome_ruolo")));
			P.setUtente(datalayer.getUtente(id_utente));
			
			P = datalayer.aggiungiPrivilegi(P);
			if(P == null){
				successo = false;
			}
			
			request.setAttribute("risultato", successo.toString());
			request.setAttribute("outline_tpl", "");
			
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("controlloAjax.ftl.json", request, response);
		}catch(DataLayerException ex){
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error(request, response);
		}catch(NumberFormatException ex){
			throw new ControllerException(ex.getMessage());
		}
	}
	
	/**
	 * Imposta l'immagine o la trascrizione come validata nella base di dati.
	 * 
	 * @param request
	 * @param response
	 * @param acquisizione_o_trascrizione
	 * @throws TemplateManagerException 
	 */
	private void action_valida_pagina(HttpServletRequest request, HttpServletResponse response, String acquisizione_o_trascrizione) throws TemplateManagerException{
		try{
			long id_pagina = SecurityLayer.checkNumeric(request.getParameter("id_pagina"));
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");
			
			Pagina p = datalayer.getPagina(id_pagina);
			Boolean successo = true;
			if(acquisizione_o_trascrizione.equals("acquisizione")){
				p.setImmagineValidata(true);
			}else{
				p.setTrascrizioneValidata(true);
			}
			if(datalayer.aggiornaPagina(p) == null){
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
	 * Analizza e smista le richieste ai dovuti metodi della classe.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 */
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		if(request.getParameter("tipoAggiornamento") != null){
			try{
				Boolean privilegi = true;
				switch(request.getParameter("tipoAggiornamento")){
				case "aggiorna_privilegi_utente": if((Boolean)request.getAttribute("admin")==true){action_aggiorna_privilegi_utente(request,response);}
													else{privilegi = false;}
					break;
				case "pubblicazione_acquisizioni":	if((Boolean)request.getAttribute("admin")==true){action_pubblica(request,response, "acquisizioni");}
													else{privilegi = false;}
					break;
				case "pubblicazione_trascrizioni":	if((Boolean)request.getAttribute("admin")==true){action_pubblica(request,response, "trascrizioni");}
													else{privilegi = false;}
					break;
				case "valida_acquisizione": if((Boolean)request.getAttribute("revisore_acquisizioni")==true){action_valida_pagina(request,response,"acquisizione");}
											else{privilegi = false;}
					break;
				case "valida_trascrizione": if((Boolean)request.getAttribute("revisore_trascrizioni")==true){action_valida_pagina(request,response,"trascrizione");}
											else{privilegi = false;}
					break;
				default: if((Boolean)request.getAttribute("admin")==true){action_aggiorna_privilegi_utente(request,response);}
						else{privilegi = false;}
				}
				if(!privilegi){
					throw new ControllerException("Accesso alla funzione negato!");
				}
			}catch(ControllerException | TemplateManagerException ex){
				request.setAttribute("message", ex.getMessage());
				action_error(request, response);
			}
		}

	}

}
