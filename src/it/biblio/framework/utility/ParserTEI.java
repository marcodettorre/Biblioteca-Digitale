package it.biblio.framework.utility;

import java.io.IOException;

/**
 * Classe che supporta operazioni di analisi di codice TEI.
 * 
 * @author Marco D'Ettorre
 * @author Francesco Proietti
 */
public final class ParserTEI {

	/**
	 * Funzione di validazione. Al momento richiama un eseguibile
	 * scaricabile ed installabile da https://github.com/TEIC/Stylesheets tramite 
	 * un comando Bash.
	 * 
	 * @param path_doc path al documento in formato TEI
	 * @return true se valido, false altrimenti
	 * @throws IOException se ci sono problemi nel reperire il file dal path passato come parametro
	 */
	public static boolean valida(String path_doc) throws IOException{
		String comando = new String("bash -c \"teitotxt "+path_doc+"\"");
		Process proc = Runtime.getRuntime().exec(comando);
		int valore_di_ritorno;
		try {
			valore_di_ritorno = proc.waitFor();
		} catch (InterruptedException e) {
			return false;
		}
		if(valore_di_ritorno != 0){
			return false;
		}
		return true;
	}
	
	/**
	 * Funzione che elimina, dal testo passato come argomento, ogni sorta di tag TEI e XML supportato.
	 * Il tag <br /> non viene rimosso per permettere la corretta renderizzazione su pagine HTML.
	 * 
	 * @param doc testo in formato TEI
	 * @return testo privo di ogni tag XML e TEI
	 */
	public static String tei_to_txt(String doc){
		final String[] tags={"<?xml","<TEI","</TEI","<body","</body","<p","</p","<div","</div","<figure","</figure","<hi","</hi","<q","</q","<text","</text"};
		Boolean almeno_un_tag_presente = true;
		
		while(almeno_un_tag_presente == true){
			almeno_un_tag_presente = false;
			
			for(String tag : tags){
				if(doc.contains(tag)){
					almeno_un_tag_presente=true;
					int beginIndex = doc.indexOf(tag);
					int endIndex = doc.indexOf('>', beginIndex);
					String tag_intero = doc.substring(beginIndex, endIndex+1);
					doc = doc.replace(tag_intero, "");
				}
			}
			
		}
		//rimozione intestazione
		String tag_fine_header = "</teiHeader>";
		int inizioIntestazione = doc.indexOf("<teiHeader");
		int fineIntetazione = doc.indexOf(tag_fine_header, inizioIntestazione);
		String intestazione = doc.substring(inizioIntestazione, fineIntetazione+tag_fine_header.length());
		doc = doc.replace(intestazione, "");
		//int occorrenza = 0;
		//while(doc.indexOf(System.lineSeparator()) != -1){
			//occorrenza = doc.indexOf(System.lineSeparator());
			//doc = doc.substring(doc.indexOf(System.lineSeparator())+System.lineSeparator().length());
		//}
		return doc;
	}
	
	public static String getBody(String doc){
		int beginIndex = doc.indexOf("<body>")+6; 
		int endIndex = doc.indexOf("</body>");
		return doc.substring(beginIndex,endIndex);
	}
}
