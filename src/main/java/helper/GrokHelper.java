package helper;



import io.thekraken.grok.api.Grok;
import io.thekraken.grok.api.Match;
import io.thekraken.grok.api.exception.GrokException;



public class GrokHelper {
	private String grokfile;
	private String grokpattern;


 
 public GrokHelper(String grokfile,String grokpattern) {
	 this.grokfile=grokfile;
	 this.grokpattern=grokpattern;
 }
 
 public String parseGrok(String logline) throws GrokException {

	 Grok g = new Grok();
		g.addPatternFromFile(this.grokfile);
		g.compile(this.grokpattern);
		//System.out.println(logline);
		Match gm = g.match(logline);
		gm.captures();
		//System.out.println(gm.toJson());
		//See the result
	 return gm.toJson();
  }
 
  
	
	
	
  
}
