package it.biblio.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import it.biblio.framework.utility.SecurityLayer;

/**
 * Servlet Filter implementation class FiltroAttributi.
 * Inizializza gli attributi della request prendendoli dalla sessione.
 * 
 */
@WebFilter("/*")
public class FiltroAttributi implements Filter {

    /**
     * Default constructor. 
     */
    public FiltroAttributi() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@SuppressWarnings("unchecked")
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpSession s = SecurityLayer.checkSession((HttpServletRequest)request);
		if(s != null){
			request.setAttribute("loggato", true);
			request.setAttribute("userid", (Long) s.getAttribute("userid"));
			request.setAttribute("nomeutente", (String) s.getAttribute("username"));
			List<String> ruoli = (List<String>) s.getAttribute("ruoli");
			
			request.setAttribute("ruoli", ruoli);
			if(ruoli != null){
			request.setAttribute("acquisitore", ruoli.contains("acquisitore"));
			request.setAttribute("trascrittore", ruoli.contains("trascrittore"));
			request.setAttribute("admin", ruoli.contains("admin"));
			request.setAttribute("revisore_acquisizioni", ruoli.contains("revisore acquisizioni"));
			request.setAttribute("revisore_trascrizioni", ruoli.contains("revisore trascrizioni"));
			}
			else{
				request.setAttribute("acquisitore", false);
				request.setAttribute("trascrittore", false);
				request.setAttribute("admin", false);
				request.setAttribute("revisore_acquisizioni", false);
				request.setAttribute("revisore_trascrizioni", false);
			}
		}else{
			request.setAttribute("loggato", false);
			request.setAttribute("acquisitore", false);
			request.setAttribute("trascrittore", false);
			request.setAttribute("admin", false);
			request.setAttribute("revisore_acquisizioni", false);
			request.setAttribute("revisore_trascrizioni", false);
		}
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
