package it.biblio.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.biblio.framework.data.DataLayerException;
import it.biblio.framework.result.TemplateManagerException;

/**
 * Carica la home page del sito.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
@WebServlet(name="Home", urlPatterns={"/index.html", "/Home"})
public class Home extends BibliotecaBaseController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8654932571825576771L;

	/**
	 * Invoca la action_result di default o la action_error in caso di errore.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			action_result(request,response);
		} catch (TemplateManagerException | DataLayerException ex) {
			request.setAttribute("exception", ex);
            action_error(request, response);
		}

	}

}
