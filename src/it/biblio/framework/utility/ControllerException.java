package it.biblio.framework.utility;

/**
 * Eccezione che indica errore di tipo diverso da
 * livello dati e logica del template manager che vengono sollevati solo
 * ed esclusivamente nelle servlet.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
@SuppressWarnings("serial")
public class ControllerException extends Exception {
	
	public ControllerException(String err, Throwable cause){
		super(err,cause);
	}
	public ControllerException(String err){
		super(err);
	}
	public ControllerException(){
		super("errore sconosciuto");
	}
}
