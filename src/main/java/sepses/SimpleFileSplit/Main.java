package sepses.SimpleFileSplit;

import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import helper.Utility;



public class Main {
	public static void main( String[] args ) throws Exception
    {
		//======yaml config=================
		  Map<String, Object> s = Utility.readYamlFile("config.yaml");
		  ObjectMapper mapper = new ObjectMapper();
		 JsonNode conf = mapper.valueToTree(s);

	       
	       
		//=====commandline argument===========
		  Options options = new Options();
		  
	      options.addOption("t", true, "log type");
	      options.addOption("l", true, "line number to process for each iteration");
	 
	      CommandLineParser parser = new DefaultParser();
	      CommandLine cmd = parser.parse(options, args); 
	      String type = cmd.getOptionValue("t");
	      String line = cmd.getOptionValue("l");

	    //=====end commandline argument===========
	      
//	      line = "20000";
//	  	  type = "apache";
	  	    
	  
	  	  System.out.println("Start running splitter for: "+type);
	      FileSplit.readJson(line, type, conf);

		 
	    		  	
    }
	
	
	
}
