package it.biblio.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.biblio.data.model.BibliotecaDataLayer;
import it.biblio.data.model.Ruolo;
import it.biblio.data.model.Utente;
import it.biblio.framework.data.DataLayerException;
import it.biblio.framework.result.TemplateManagerException;
import it.biblio.framework.utility.ControllerException;
import it.biblio.framework.utility.SecurityLayer;

/**
 * Servlet che gestisce il Login.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
@WebServlet(description = "Verifica credenziali e reindirizzamento alla homepage utente", urlPatterns = { "/Login" })
public class Login extends BibliotecaBaseController {

	private static final long serialVersionUID = -6467942832198091561L;

	/**
	 * Metodo che verifica le credenziali di accesso e crea una sessione utente reindirizzandolo
	 * alla sua home page in caso le credenziali siano valide, richiama la home page generale con un messaggio di errore
	 * in caso cotrario.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 * @throws ControllerException se occorre un errore nella redirezione alla homepage
	 */
	private void action_login(HttpServletRequest request, HttpServletResponse response) throws TemplateManagerException, ControllerException {
		try {
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");
			Utente U = datalayer.getUtenteByUsername(SecurityLayer.addSlashes(request.getParameter("username")));

			if (U != null) {
				if (U.getPassword()
						.equals(SecurityLayer.criptaPassword(SecurityLayer.addSlashes(request.getParameter("password")),
								SecurityLayer.addSlashes(request.getParameter("username"))))) {
					// credenziali ok
					List<Ruolo> ruoli_utente = datalayer.getListaRuoliUtente(U);
					List<String> nomi_ruoli_utente = new ArrayList<String>();
					for (Ruolo ruolo : ruoli_utente) {
						nomi_ruoli_utente.add(ruolo.getNome());
					}
					SecurityLayer.createSession(request, U.getUsername(), U.getID(), nomi_ruoli_utente);
					// è necessario ricaricare la pagina 
					response.sendRedirect("Home");
				} else {
					// display dell'errore nella form di login
					request.setAttribute("errore_login", true);
					action_result(request,response);
				}
			} else {
				// display dell'errore nella form di login
				request.setAttribute("errore_login", true);
				action_result(request,response);
			}
			
			
		} catch (DataLayerException ex) {
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error(request,response);
		} catch (IOException e) {
			throw new ControllerException("errore nella redirezione alla homepage"+ e.getMessage(), e.getCause());
		}
	}

	/**
	 * Effettua la login o, in caso di errori, genera una pagina di errore.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		try {
			action_login(request, response);
		} catch (TemplateManagerException | ControllerException ex) {
			request.setAttribute("exception", ex);
            action_error(request, response);
		} 

	}

}
