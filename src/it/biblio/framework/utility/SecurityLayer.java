package it.biblio.framework.utility;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public final class SecurityLayer {

    /**
     * questa funzione esegue una serie di controlli di sicurezza
     * sulla sessione corrente. Se la sessione non è valida, la cancella
     * ritorna null, altrimenti la aggiorna e la restituisce
     * 
     * @param r
     * @return
     */
    public static HttpSession checkSession(HttpServletRequest r) {
        boolean check = true;

        HttpSession s = r.getSession(false);
        //per prima cosa vediamo se la sessione è attiva
        if (s == null) {
            return null;
        }

        //check sulla validità  della sessione
        if (s.getAttribute("userid") == null) {
            check = false;
            //check sull'ip del client
        } else if ((s.getAttribute("ip") == null) || !((String) s.getAttribute("ip")).equals(r.getRemoteHost())) {
            check = false;
            //check sulle date
        } else {
            //inizio sessione
            //session start timestamp
            Calendar begin = (Calendar) s.getAttribute("inizio-sessione");
            //ultima azione
            //last action timestamp
            Calendar last = (Calendar) s.getAttribute("ultima-azione");
            //data/ora correnti
            //current timestamp
            Calendar now = Calendar.getInstance();
            if (begin == null) {
                check = false;
            } else {
                //secondi trascorsi dall'inizio della sessione
                //seconds from the session start
                long secondsfrombegin = (now.getTimeInMillis() - begin.getTimeInMillis()) / 1000;
                //dopo tre ore la sessione scade
                //after three hours the session is invalidated
                if (secondsfrombegin > 3 * 60 * 60) {
                    check = false;
                } else if (last != null) {
                    //secondi trascorsi dall'ultima azione
                    //seconds from the last valid action
                    long secondsfromlast = (now.getTimeInMillis() - last.getTimeInMillis()) / 1000;
                    //dopo trenta minuti dall'ultima operazione la sessione è invalidata
                    //after 30 minutes since the last action the session is invalidated                    
                    if (secondsfromlast > 30 * 60) {
                        check = false;
                    }
                }
            }
        }
        if (!check) {
            s.invalidate();
            return null;
        } else {
            //reimpostiamo la data/ora dell'ultima azione
            //if che checks are ok, update the last action timestamp
            s.setAttribute("ultima-azione", Calendar.getInstance());
            return s;
        }
    }

    public static HttpSession createSession(HttpServletRequest request, String username, long userid, List<String> ruoli) {
        HttpSession s = request.getSession(true);
        s.setAttribute("username", username);
        s.setAttribute("ip", request.getRemoteHost());
        s.setAttribute("inizio-sessione", Calendar.getInstance());
        s.setAttribute("userid", userid);
        s.setAttribute("ruoli", ruoli);
        return s;
    }

    public static void disposeSession(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s != null) {
            s.invalidate();
        }
    }

    //--------- DATA SECURITY ------------
    //questa funzione aggiunge un backslash davanti a
    //tutti i caratteri "pericolosi", usati per eseguire
    //SQL injection attraverso i parametri delle form
    
    //this function adds backslashes in front of
    //all the "malicious" charcaters, usually exploited
    //to perform SQL injection through form parameters
    public static String addSlashes(String s) {
        return s.replaceAll("(['\"\\\\])", "\\\\$1");
    }

    //questa funzione rimuove gli slash aggiunti da addSlashes
    //this function removes the slashes added by addSlashes
    public static String stripSlashes(String s) {
        return s.replaceAll("\\\\(['\"\\\\])", "$1");
    }

    /*questa funzione (utilizzata per lo più per analizzare la correttezza dei campi stringa in una form)
    restituisce il nome dei campi che sono nulli o il cui valore contiene caratteri non ammessi*/
    public static String checkString(List<String> nomi, List<String> valori, String caratteri_non_ammessi){
        String valori_errati = "";
        for(int i = 0; i < nomi.size(); i++){
            if(valori.get(i) == null || valori.get(i).matches(".*"+ caratteri_non_ammessi +".*")){
                valori_errati += nomi.get(i);
            }
        }
        return valori_errati;
    }
    
    public static long checkNumeric(String s) throws NumberFormatException {
        //convertiamo la stringa in numero, ma assicuriamoci prima che sia valida
        //convert the string to a number, ensuring its validity
        if (s != null) {
            //se la conversione fallisce, viene generata un'eccezione
            //if the conversion fails, an exception is raised
            return Long.parseLong(s);
        } else {
            throw new NumberFormatException("String argument is null");
        }
    }

    //--------- CONNECTION SECURITY ------------
    //questa funzione verifica se il protocollo HTTPS è attivo
    //checks if the HTTPS protocol is in use
    public static boolean checkHttps(HttpServletRequest r) {
        return r.isSecure();
        //metodo "fatto a mano" che funziona solo se il server trasmette gli header corretti
        //the following is an "handmade" alternative, which works only if the server sends correct headers
        //String httpsheader = r.getHeader("HTTPS");
        //return (httpsheader != null && httpsheader.toLowerCase().equals("on"));
    }

    //questa funzione ridirige il browser sullo stesso indirizzo
    //attuale, ma con protocollo https
    //this function redirects the browser on the current address, but
    //with https protocol
    @SuppressWarnings("static-access")
	public static void redirectToHttps(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        //estraiamo le parti della request url
        String server = request.getServerName();
        //int port = request.getServerPort();
        String context = request.getContextPath();
        String path = request.getServletPath();
        String info = request.getPathInfo();
        String query = request.getQueryString();

        //ricostruiamo la url cambiando il protocollo e la porta COME SPECIFICATO NELLA CONFIGURAZIONE DI TOMCAT
        //rebuild the url changing port and protocol AS SPECIFIED IN THE SERVER CONFIGURATION
        String newUrl = "https://" + server + ":8443" +  context + path + (info != null ? info : "") + (query != null ? "?" + query : "");
        try {
            //ridirigiamo il client
            //redirect
            response.sendRedirect(newUrl);
        } catch (IOException ex) {
            try {
                //in caso di problemi tentiamo prima di inviare un errore HTTP standard
                //in case of problems, first try to send a standard HTTP error message
                response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Cannot redirect to HTTPS, blocking request");
            } catch (IOException ex1) {
                //altrimenti generiamo un'eccezione
                //otherwise, raise an exception
                throw new ServletException("Cannot redirect to https!");
            }
        }
    }
    
    //questa funzione crea una password con lo username e applicando
    //l'algoritmo SHA-1 di Java
    public static String criptaPassword(String pwd, String username){
        String sha1 = "";
        String argsha1 = pwd + username;
        try{
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(argsha1.getBytes("UTF-8"));
            byte[] digest = crypt.digest();
            for(byte b : digest){
                sha1 += String.valueOf(b);
            }
        } catch (NoSuchAlgorithmException e){
        
        } catch (UnsupportedEncodingException e){
        
        }
        return sha1;
    }
    
    
    //questa funzione genera una nuova password random, utile per gli utenti
    //che le dimenticano e per il primo accesso
    public static String randPassword(String s, int lunghezza){
        String alfabeto = "012389ABCDEFGHIJ4567KLMNOXYZabc"+s+"defghilmnoPQRSTUVWpqrstuvz";
        Random rand = new Random(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder(lunghezza);
        for(int i = 0; i < lunghezza; i++){
            sb.append(alfabeto.charAt(rand.nextInt(alfabeto.length())));
        }
        return sb.toString();
    }
    
    public static String removeNewLine(String s){
    	return s.replaceAll(System.lineSeparator(), "");
    }
}

