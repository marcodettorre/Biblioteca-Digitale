package it.biblio.controller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.biblio.data.model.BibliotecaDataLayer;
import it.biblio.data.model.Opera;
import it.biblio.data.model.Pagina;
import it.biblio.data.model.Ruolo;
import it.biblio.data.model.Utente;
import it.biblio.framework.data.DataLayerException;
import it.biblio.framework.result.AddSlashesFmkExt;
import it.biblio.framework.result.SplitSlashesFmkExt;
import it.biblio.framework.result.TemplateManagerException;
import it.biblio.framework.result.TemplateResult;
import it.biblio.framework.utility.ControllerException;
import it.biblio.framework.utility.ParserTEI;
import it.biblio.framework.utility.SecurityLayer;

/**
 * Servlet che gestisce le richieste di selezione delle entit&agrave presenti
 * nella base di dati.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
@WebServlet(name = "Ricerca", urlPatterns = { "/Ricerca" })
public class Ricerca extends BibliotecaBaseController {

	/**
	 * campo per la serializzazione della classe.
	 * 
	 */
	private static final long serialVersionUID = 8446825403497077597L;

	/**
	 * Funzione di ricerca delle opere che restituisce un oggetto JSON.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws TemplateManagerException
	 *             se occorre un errore nella logica del template manager
	 */
	private void action_ricerca_ajax(HttpServletRequest request, HttpServletResponse response)
			throws TemplateManagerException {
		try {
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");

			String anno = request.getParameter("anno");
			String editore = request.getParameter("editore");
			String autore = request.getParameter("autore");
			String titolo = request.getParameter("titolo");
			String immagini_pubblicate = request.getParameter("pubblicata");
			String lingua = request.getParameter("lingua");

			List<Opera> opere = new ArrayList<Opera>();
			Opera O = datalayer.creaOpera();
			O.setAutore(SecurityLayer.addSlashes(autore));
			O.setAnno(SecurityLayer.addSlashes(anno));
			O.setEditore(SecurityLayer.addSlashes(editore));
			O.setTitolo(SecurityLayer.addSlashes(titolo));
			O.setImmaginiPubblicate(
					(immagini_pubblicate != null && immagini_pubblicate.equals("false")) ? false : true);
			O.setTrascrizioniPubblicate(false);
			O.setLingua(SecurityLayer.addSlashes(lingua));
			opere = datalayer.getOpereByQuery(O);

			O.setTrascrizioniPubblicate(true);
			opere.addAll(datalayer.getOpereByQuery(O));
			request.setAttribute("opere", opere);
			request.setAttribute("outline_tpl", "");
			request.setAttribute("strip_slashes", new SplitSlashesFmkExt());
			request.setAttribute("contentType", "text/json");
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("queryOpere.ftl.json", request, response);
		} catch (DataLayerException ex) {
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error_ajax(request, response);
		}

	}

	/**
	 * Funzione di ricerca degli utenti che restituisce un oggetto JSON.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws TemplateManagerException
	 *             se occorre un errore nella logica del template manager
	 */
	private void action_ricerca_utenti_ajax(HttpServletRequest request, HttpServletResponse response)
			throws TemplateManagerException {
		try {
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");

			List<Ruolo> ruoli = new ArrayList<>();
			List<Utente> utenti = datalayer.getTuttiGliUtenti();
			for (Utente utente : utenti) {
				List<Ruolo> ruoli_utente = datalayer.getListaRuoliUtente(utente);
				if (!ruoli_utente.isEmpty())
					ruoli.add(ruoli_utente.get(0));
				else {
					Ruolo ruolo = datalayer.creaRuolo();
					ruolo.setNome("utente base");
					ruolo.setDescrizione("utente che può solo vedere le opere!");
					ruoli.add(ruolo);
				}
			}

			List<Ruolo> ruoli_db = datalayer.getTuttiIRuoli();

			request.setAttribute("utenti", utenti);
			request.setAttribute("ruoli", ruoli);
			request.setAttribute("ruoli_db", ruoli_db);
			request.setAttribute("strip_slashes", new SplitSlashesFmkExt());
			request.setAttribute("outline_tpl", "");
			request.setAttribute("contentType", "text/json");
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("queryUtenti.ftl.json", request, response);
		} catch (DataLayerException ex) {
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error_ajax(request, response);
		}
	}

	/**
	 * Funzione che invia in risposta un JSON con 
	 * tutte le opere presenti nel sistema. Possibile solo per utenti admin.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 */
	private void action_tutteleopere_ajax(HttpServletRequest request, HttpServletResponse response)
			throws TemplateManagerException {
		try {
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");

			List<Opera> opere = datalayer.getTutteLeOpere();
			request.setAttribute("opere", opere);
			request.setAttribute("outline_tpl", "");
			request.setAttribute("contentType", "text/json");
			request.setAttribute("strip_slashes", new SplitSlashesFmkExt());
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("queryOpere.ftl.json", request, response);
		} catch (DataLayerException ex) {
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error_ajax(request, response);
		}
	}

	/**
	 * Funzione che invia in risposta un JSON con le opere in stato "acquisito".
	 * Ovvero con tutte le immagini validate.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 */
	private void action_opere_in_pubblicazione_acquisizioni_ajax(HttpServletRequest request,
			HttpServletResponse response) throws TemplateManagerException {
		try {
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");

			List<Opera> opere = datalayer.getOpereInPubblicazioneAcquisizioni();
			request.setAttribute("opere", opere);
			request.setAttribute("outline_tpl", "");
			request.setAttribute("contentType", "text/json");
			request.setAttribute("strip_slashes", new SplitSlashesFmkExt());
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("queryOpere.ftl.json", request, response);
		} catch (DataLayerException ex) {
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error_ajax(request, response);
		}
	}

	/**
	 * Funzione che invia in risposta un JSON con le opere che non risultano "trascritte" ed il cui 
	 * trascrittore assegnato è l'utente che ha fatto tale richiesta.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 */
	private void action_opere_in_trascrizione_ajax(HttpServletRequest request, HttpServletResponse response)
			throws TemplateManagerException {
		try {
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");

			Utente U = datalayer.getUtenteByUsername((String) request.getAttribute("nomeutente"));
			Opera O = datalayer.creaOpera();
			O.setImmaginiPubblicate(true);
			O.setTrascrizioniPubblicate(false);
			O.setTrascrittore(U);
			List<Opera> opere = datalayer.getOpereByQuery(O);
			request.setAttribute("opere", opere);
			request.setAttribute("outline_tpl", "");
			request.setAttribute("contentType", "text/json");
			request.setAttribute("strip_slashes", new SplitSlashesFmkExt());
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("queryOpere.ftl.json", request, response);
		} catch (DataLayerException ex) {
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error_ajax(request, response);
		}
	}

	/**
	 * Funzione che invia in risposta un JSON con tutte le opere non che non hanno nemmeno una pagina in trascrizione.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 */
	private void action_opere_da_trascrivere_ajax(HttpServletRequest request, HttpServletResponse response)
			throws TemplateManagerException {
		try {
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");

			List<Opera> opere = datalayer.getOpereDaTrascrivere();
			request.setAttribute("opere", opere);
			request.setAttribute("outline_tpl", "");
			request.setAttribute("contentType", "text/json");
			request.setAttribute("strip_slashes", new SplitSlashesFmkExt());
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("queryOpere.ftl.json", request, response);
		} catch (DataLayerException ex) {
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error_ajax(request, response);
		}
	}

	/**
	 * Funzione che invia in risposta un JSON con le opere che risultano "trascritte".
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException  se occorre un errore nella logica del template manager
	 */
	private void action_opere_in_pubblicazione_trascrizioni_ajax(HttpServletRequest request,
			HttpServletResponse response) throws TemplateManagerException {
		try {
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");

			List<Opera> opere = datalayer.getOpereInPubblicazioneTrascrizioni();
			request.setAttribute("opere", opere);
			request.setAttribute("outline_tpl", "");
			request.setAttribute("strip_slashes", new SplitSlashesFmkExt());
			request.setAttribute("contentType", "text/json");
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("queryOpere.ftl.json", request, response);
		} catch (DataLayerException ex) {
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error_ajax(request, response);
		}
	}

	/**
	 * Funzione che invia in risposta un JSON contenente tutte le pagine dell'opera. A seconda dell'utente che ne
	 * fa richiesta restituisce una versione diversa della trascrizione presente.(con o senza tag TEI).
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 */
	private void action_pagine_opera_ajax(HttpServletRequest request, HttpServletResponse response)
			throws TemplateManagerException {
		try {
			//L'utente può accedere alle pagine dell'opera solo se loggato
			if ((Boolean) request.getAttribute("loggato")) {
				BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");

				long id_opera = Long.parseLong(request.getParameter("id_opera"));
				List<Pagina> pagine = datalayer.getPagineOpera(id_opera);
				// leggi il file trascrizione in caso di utente trascrittore o
				// revisore.
				for (Pagina p : pagine) {
					if (!p.getPathTrascrizione().equals("")) {

						//lettura file
						BufferedReader in = new BufferedReader(new InputStreamReader(
								new FileInputStream(p.getPathTrascrizione())
								,StandardCharsets.UTF_8)
								);
						String testo, riga;
						testo = riga = "";
						while ((riga = in.readLine()) != null) {
							testo += riga + System.lineSeparator();
						}
						in.close();
						//il revisore vedrà tutto il documento in formato TEI
						if ((Boolean) request.getAttribute("revisore_trascrizioni") == true) {
							p.setPathTrascrizione(SecurityLayer.removeNewLine(testo));
						}
						//il trasrittore vedrà solo il body in formato TEI
						else if((Boolean)request.getAttribute("trascrittore") == true){
							  p.setPathTrascrizione(ParserTEI.getBody(testo));
						}
						//gli altri utenti vedranno solo la conversione in testo semplice
						else {
							testo = ParserTEI.tei_to_txt(testo);
							p.setPathTrascrizione(SecurityLayer.removeNewLine(testo));
						}

					}
				}
				// per la semplice consultazione delle pagine pubblicate.
				request.setAttribute("trascrizioni_pubblicate",
						datalayer.getOpera(id_opera).getTrascrizioniPubblicate());
				request.setAttribute("pagine", pagine);
			} 
			//utente non loggato e quindi non gli vengono restituite le pagine
			else {
				request.setAttribute("trascrizioni_pubblicate", "");
				request.setAttribute("pagine", new ArrayList<Pagina>());
			}
			request.setAttribute("outline_tpl", "");
			request.setAttribute("strip_slashes", new SplitSlashesFmkExt());
			request.setAttribute("add_slashes", new AddSlashesFmkExt());
			request.setAttribute("contentType", "text/json");
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("queryPagine.ftl.json", request, response);
		} catch (DataLayerException ex) {
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error_ajax(request, response);
		} catch (IOException e) {
			request.setAttribute("message",
					"Il path è sbagliato, probabilmente devi aggiustare i path nel DB con quelli del tuo sistema.");
			action_error_ajax(request, response);
		}
	}

	@SuppressWarnings("unused")
	private void action_ricerca(HttpServletRequest request, HttpServletResponse response)
			throws TemplateManagerException {
		try {
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");

			String anno = request.getParameter("anno");
			String editore = request.getParameter("editore");
			String autore = request.getParameter("autore");
			String titolo = request.getParameter("titolo");
			String immagini_pubblicate = request.getParameter("pubblicata");
			String lingua = request.getParameter("lingua");

			List<Opera> opere = new ArrayList<Opera>();
			Opera O = datalayer.creaOpera();
			O.setAnno(SecurityLayer.addSlashes(anno));
			O.setEditore(SecurityLayer.addSlashes(editore));
			O.setTitolo(SecurityLayer.addSlashes(titolo));
			O.setImmaginiPubblicate((immagini_pubblicate.equals("true")) ? true : false);
			O.setLingua(SecurityLayer.addSlashes(lingua));
			opere = datalayer.getOpereByQuery(O);

			request.setAttribute("ricerca", true);
			request.setAttribute("opere", opere);
			request.setAttribute("strip_slashes", new SplitSlashesFmkExt());

			action_result(request, response);
		} catch (DataLayerException ex) {
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error_ajax(request, response);
		}

	}

	/**
	 * Funzione che invia in risposta un JSON contenente le opere in "revisione acquisizione".
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 */
	private void action_opere_con_acquisizioni_da_convalidare_ajax(HttpServletRequest request,
			HttpServletResponse response) throws TemplateManagerException {
		try {
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");
			List<Opera> opere = datalayer.getOpereConImmaginiNonValidate();

			request.setAttribute("opere", opere);
			request.setAttribute("outline_tpl", "");
			request.setAttribute("strip_slashes", new SplitSlashesFmkExt());
			request.setAttribute("contentType", "text/json");
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("queryOpere.ftl.json", request, response);
		} catch (DataLayerException ex) {
			request.setAttribute("message", "Data access exception: " + ex.getMessage());
			action_error_ajax(request, response);
		}
	}

	/**
	 * Funzione che invia in risposta un JSON contenente le opere in "revisione trascrizione".
	 * 
	 * @param request servlet response
	 * @param response servlet response
	 * @throws TemplateManagerException se occorre un errore nella logica del template manager
	 */
	private void action_opere_con_trascrizioni_da_convalidare_ajax(HttpServletRequest request,
			HttpServletResponse response) throws TemplateManagerException {
		try {
			BibliotecaDataLayer datalayer = (BibliotecaDataLayer) request.getAttribute("datalayer");
			Opera O = datalayer.creaOpera();
			O.setImmaginiPubblicate(true);
			O.setTrascrizioniPubblicate(false);
			List<Opera> opere = datalayer.getOpereByQuery(O);

			for (Opera o : opere) {
				List<Pagina> pagine_opera = datalayer.getPagineOpera(o.getID());
				int cont = 0;
				for (Pagina pagina : pagine_opera) {
					if (pagina.getTrascrizioneValidata()) {
						cont++;
					}
				}
				if (cont == o.getNumeroPagine()) {
					opere.remove(o);
				}
			}

			request.setAttribute("opere", opere);
			request.setAttribute("outline_tpl", "");
			request.setAttribute("strip_slashes", new SplitSlashesFmkExt());
			request.setAttribute("contentType", "text/json");
			TemplateResult tr = new TemplateResult(getServletContext());
			tr.activate("queryOpere.ftl.json", request, response);
		} catch (DataLayerException ex) {
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

		try {
			if (request.getParameter("tipoRicerca") == null) {
				action_ricerca_ajax(request, response);
			} else {
				Boolean privilegi = true;
				switch (request.getParameter("tipoRicerca")) {
				case "utenti":
					if((Boolean)request.getAttribute("admin")==true){action_ricerca_utenti_ajax(request, response);}
					else{privilegi = false;}
					break;
				case "tutteleopere":
					if((Boolean)request.getAttribute("admin")==true){action_tutteleopere_ajax(request, response);}
					else{privilegi = false;}
					break;
				case "opereInPubblicazioneAcquisizioni":
					if((Boolean)request.getAttribute("admin")==true){action_opere_in_pubblicazione_acquisizioni_ajax(request, response);}
					else{privilegi = false;}
					break;
				case "opereInPubblicazioneTrascrizioni":
					if((Boolean)request.getAttribute("admin")==true){action_opere_in_pubblicazione_trascrizioni_ajax(request, response);}
					else{privilegi = false;}
					break;
				case "opere_in_trascrizione":
					if((Boolean)request.getAttribute("trascrittore")==true){action_opere_in_trascrizione_ajax(request, response);}
					else{privilegi = false;}
					break;
				case "opere_da_trascrivere":
					if((Boolean)request.getAttribute("trascrittore")==true){action_opere_da_trascrivere_ajax(request, response);}
					else{privilegi = false;}
					break;
				case "opere_con_acquisizioni_da_convalidare":
					if((Boolean)request.getAttribute("revisore_acquisizioni")==true){action_opere_con_acquisizioni_da_convalidare_ajax(request, response);}
					else{privilegi = false;}
					break;
				case "opere_con_trascrizioni_da_convalidare":
					if((Boolean)request.getAttribute("revisore_trascrizioni")==true){action_opere_con_trascrizioni_da_convalidare_ajax(request, response);}
					else{privilegi = false;}
					break;
				case "pagine_opera":
					if((Boolean)request.getAttribute("loggato")==true){action_pagine_opera_ajax(request, response);}
					else{privilegi = false;}
					break;
				default:
					action_ricerca_ajax(request, response);
					break;
				}
				if(!privilegi){
					throw new ControllerException("Accesso alla funzione negato!");
				}
			}
		} catch (TemplateManagerException | ControllerException ex) {
			request.setAttribute("exception", ex);
			action_error(request, response);
		}

	}

}
