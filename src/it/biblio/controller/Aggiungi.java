package it.biblio.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.biblio.data.model.BibliotecaDataLayer;
import it.biblio.data.model.Opera;
import it.biblio.framework.data.DataLayerException;
import it.biblio.framework.result.TemplateManagerException;
import it.biblio.framework.utility.ControllerException;
import it.biblio.framework.utility.SecurityLayer;

/**
 * Servlet che gestisce le richieste di inserimento dei record delle entità
 * presenti nella base di dati.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
@WebServlet(name="Aggiungi", urlPatterns={"/AggiungiOpera","/Aggiungi"})
public class Aggiungi extends BibliotecaBaseController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -352354517627529114L;


	/**
	 * Aggiunta di un'opera nella base di dati.
	 * 
	 * @param request
	 * @param response
	 * @throws TemplateManagerException 
	 */
	private void action_aggiungi_opera(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException{
		try{
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");
			
			Opera O = datalayer.creaOpera();
			O.setAnno(SecurityLayer.addSlashes(request.getParameter("anno")));
			O.setDescrizione(SecurityLayer.addSlashes(request.getParameter("descrizione")));
			O.setLingua(SecurityLayer.addSlashes(request.getParameter("lingua")));
			O.setTitolo(SecurityLayer.addSlashes(request.getParameter("titolo")));
			O.setEditore(SecurityLayer.addSlashes(request.getParameter("editore")));
			O.setAutore(SecurityLayer.addSlashes(request.getParameter("autore")));
			O.setNumeroPagine(Integer.parseInt(request.getParameter("numeroPagine")));
			O.setImmaginiPubblicate(false);
			O.setTrascrizioniPubblicate(false);
			
			O = datalayer.aggiungiOpera(O);
			if(O == null){
				throw new DataLayerException("Errore nell'aggiungere l'opera nel database");
			}
			else{
				request.setAttribute("messaggio", "È stata inserita l'opera "+O.getTitolo()+" di "+O.getAutore()+" con "+O.getNumeroPagine()+" pagine." );
				action_result(request, response);
			}
		}
		catch(DataLayerException ex){
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error_ajax(request, response);
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
		try{
			if(request.getParameter("anno") != null && request.getParameter("titolo") != null 
				&& request.getParameter("lingua") != null && request.getParameter("autore") != null
				&& request.getParameter("editore") != null && request.getParameter("descrizione") != null){
				
				SecurityLayer.checkNumeric(request.getParameter("numeroPagine"));
				if((Boolean) request.getAttribute("admin") == true){
					action_aggiungi_opera(request, response);
				}
				else{
					throw new ControllerException("Accesso non consentito!");
				}
			}else{
				throw new ControllerException("parametri non validi!");
			}
			
		}catch(ControllerException | NumberFormatException | TemplateManagerException ex){
			request.setAttribute("exception", ex);
			action_error(request, response);
		}

	}

}
