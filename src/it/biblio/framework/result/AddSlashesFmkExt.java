package it.biblio.framework.result;

import java.util.List;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import it.biblio.framework.utility.SecurityLayer;

public class AddSlashesFmkExt implements TemplateMethodModelEx {

	@SuppressWarnings("rawtypes")
	@Override
	public Object exec(List arg0) throws TemplateModelException {
		//la lista contiene i parametri passati alla funzione nel template
		if(arg0 != null && !arg0.isEmpty()){
			System.out.println(arg0);
			if(arg0.get(0) == null){//fix
				return "";
			}
			return SecurityLayer.addSlashes(arg0.get(0).toString());
		}else{
			return "";
		}
		
	}

}
