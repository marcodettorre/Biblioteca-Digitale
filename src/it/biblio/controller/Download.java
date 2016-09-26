package it.biblio.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.biblio.data.model.BibliotecaDataLayer;
import it.biblio.data.model.Pagina;
import it.biblio.framework.data.DataLayerException;
import it.biblio.framework.result.FailureResult;
import it.biblio.framework.result.StreamResult;
import it.biblio.framework.result.TemplateManagerException;
import it.biblio.framework.utility.ControllerException;
import it.biblio.framework.utility.SecurityLayer;

/**
 * Servlet per lo scaricamento di flussi di dati.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
@WebServlet("/Download")
public class Download extends BibliotecaBaseController {

	private static final long serialVersionUID = -3999174379443972231L;

	/**
	 * Restituisce un'immagine di errore.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	protected void action_error(HttpServletRequest request, HttpServletResponse response){
		StreamResult sr = new StreamResult(getServletContext());
		try {
			sr.activate(new File(getServletContext().getRealPath(File.separator)+File.separator+"template/img/error_osd.jpeg"), request, response);
		} catch (IOException e) {
			request.setAttribute("message", "Errore grave nel reperire le immagini delle opere:"+e.getMessage());
			(new FailureResult(getServletContext())).activate((String) request.getAttribute("message"), request,
					response);
		}
	}
	
	/**
	 * Restituisce un'immagine della pagina specificata.
	 * 
	 * @param request
	 * @param response
	 * @param imgid
	 * @throws IOException
	 * @throws DataLayerException
	 * @throws ControllerException
	 */
	private void action_download_openseadragon(HttpServletRequest request, HttpServletResponse response, long imgid)
			throws IOException, DataLayerException, ControllerException {
		StreamResult sr = new StreamResult(getServletContext());

		BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");

		Pagina P = datalayer.getPagina(imgid);
		if (P != null) {
			sr.activate(new File(P.getPathImmagine()), request, response);
		} else {
			throw new ControllerException("Non è stata reperita alcuna pagina");
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
			long imgid = SecurityLayer.checkNumeric(request.getParameter("imgid"));
			action_download_openseadragon(request,response,imgid);
		}catch (IOException | ControllerException | DataLayerException | NumberFormatException ex) {
            action_error(request, response);
		}
		
	}

}
