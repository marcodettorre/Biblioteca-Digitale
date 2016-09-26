package it.biblio.framework.result;
/**
 * Eccezione che si verifica durante il processamento della
 * fase di presentazione.
 * 
 * @author francesco
 *
 */
@SuppressWarnings("serial")
public class TemplateManagerException extends Exception {

	public TemplateManagerException(String message) {
        super(message);
    }

    public TemplateManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateManagerException(Throwable cause) {
        super(cause);
    }
}
