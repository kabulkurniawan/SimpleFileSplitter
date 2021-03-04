package sepses.SimpleFileSplit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import helper.GrokHelper;
import helper.Utility;
import io.thekraken.grok.api.exception.GrokException;






public class FileSplit {
	
	public static void main( String[] args ) throws Exception
    {
	
		//=====commandline argument===========
		  Options options = new Options();
	      options.addOption("i", true, "input folder");
	      options.addOption("o", true, "output folder");
	      options.addOption("m", true, "log meta folder");
	      options.addOption("l", true, "line number to process for each iteration");
	 
	      CommandLineParser parser = new DefaultParser();
	      CommandLine cmd = parser.parse(options, args); 
	      String input = cmd.getOptionValue("i");
	      String output = cmd.getOptionValue("o");
	      String logmeta = cmd.getOptionValue("m");
	      String line = cmd.getOptionValue("l");

	    //=====end commandline argument===========

	      input = "input/";
	      output = "output/";
	  	  line = "20000";
	  	  logmeta = "logmeta/";
	  	  String filetype = "apache";
	  	  String grokfile = "pattern/pattern.grok"; 
	  	  String grokpattern =  "%{HTTPDATESIMPLE}";
	      
	  	  System.out.println("Start running indexer...");
	      readJson(input,output,logmeta, line, grokfile, grokpattern, filetype);
	    		  
	    
	  	
    }
	
	
	public static void readJson(String input, String output,String logmeta, String l, String grokfile, String grokpattern, String filetype) throws Exception {
		  
		Integer lineNumber = 1; // 1 here means the minimum line to be extracted
		if(l!=null) {lineNumber=Integer.parseInt(l);}
		Integer startingLine = 0; // 1 means start from the beginning
		
		// create in one json object
		Integer countLine=0;
		Integer templ = 0;
		Integer group=0;
	

		//alert model store in jena model
		Model metaModel = ModelFactory.createDefaultModel();
		long time1 = System.currentTimeMillis();
	
		

		ArrayList<Integer> counter = new ArrayList<Integer>(); 
		counter.add(0);
		
		
		File folder = new File(input);
		
		ArrayList<String> listFiles = Utility.listFilesForFolder(folder);
		Collections.sort(listFiles);
		
		 if (listFiles.size()==0) { System.out.print("folder is empty!"); System.exit(0);}
	     for (String file : listFiles) {
	    	 	System.out.println("processing file: "+file);
	    	 	String filename = input+file;
	
			InputStream jf = new FileInputStream(filename);
			BufferedReader in = new BufferedReader(new InputStreamReader(jf));	
			 Collection<String> groupline = new ArrayList<String>();
			 
					while (in.ready()) {
						String line = in.readLine();
						if (countLine.equals(startingLine)) {
							System.out.println("reading from line : "+ startingLine);
								group=((int) Math.ceil((startingLine-1)/lineNumber));
						}
						if(countLine >= startingLine) {
							
							 groupline.add(line);    
								templ++;
							if(templ.equals(lineNumber)) {
								String fdate = getTimestamp(parseGrok(grokfile, grokpattern, getFirstElement(groupline)));
								String ldate = getTimestamp(parseGrok(grokfile, grokpattern, getLastElement(groupline)));
								
								System.out.println(fdate);
								System.out.println(ldate);
								
								
								String filenamegroup = file+"_"+group; 
								String fn = output+filenamegroup;
								createFile(fn,groupline);
								
								createMetaFile(metaModel, filenamegroup,fdate,ldate,filetype,group);
								
								System.out.println("writing "+fn+" finished in "+(System.currentTimeMillis() - time1));
								group++;

								templ=0;
							}
							
							
						  }
						countLine++;
				}
		// check the rest 
		in.close();
		if(templ!=0) {
			String fdate = getTimestamp(parseGrok(grokfile, grokpattern, getFirstElement(groupline)));
			String ldate = getTimestamp(parseGrok(grokfile, grokpattern, getLastElement(groupline)));
			
			System.out.println(fdate);
			System.out.println(ldate);
			String filenamegroup = file+"_"+group; 
			String fn = output+filenamegroup;
			createFile(fn,groupline);
			createMetaFile(metaModel, filenamegroup,fdate,ldate,filetype,group);
			//System.out.println("the rest is less than "+lineNumber+" which is "+templ);
			System.out.println("last writing "+fn+" finished in "+(System.currentTimeMillis() - time1));
			templ=0;
			
		}
			//end of a file	
		   System.out.println("finish processing file:"+filename);
		  //metaModel.write(System.out,"TURTLE");
		   Utility.saveToRDF(metaModel, logmeta, filetype+"_meta");
	   }
	
	   
	}
	
	private static void createMetaFile(Model metamodel, String filenamegroup, String fdate, String ldate, String fileType, Integer fileID) {
		Property startDate = metamodel.createProperty("http://w3id.org/sepses/asset#startDate");
		Property endDate = metamodel.createProperty("http://w3id.org/sepses/asset#endDate");
		Property fType = metamodel.createProperty("http://w3id.org/sepses/asset#fileType");
		Property fID = metamodel.createProperty("http://w3id.org/sepses/asset#fileID");
		Resource res = metamodel.createResource("http://w3id.org/sepses/meta/"+filenamegroup);
		metamodel.add(res, RDFS.label, filenamegroup);
		metamodel.add(res, startDate, fdate);
		metamodel.add(res, endDate, ldate);
		metamodel.add(res, fType, fileType);
		metamodel.addLiteral(res, fID, fileID);
		
		
	}


	public static void createFile(String filename, Collection<String> groupline) throws IOException {
		try {
		      File myObj = new File(filename);
		      if (myObj.createNewFile()) {
		        //System.out.println("File created: " + myObj.getName());
		      } 
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		File textfile = new File(filename);
		FileUtils.writeLines(textfile, groupline);
		groupline.clear();	
	}
	
	public static <T> T getFirstElement(final Iterable<T> elements) {
	    return elements.iterator().next();
	}

	public static <T> T getLastElement(final Iterable<T> elements) {
	    T lastElement = null;

	    for (T element : elements) {
	        lastElement = element;
	    }

	    return lastElement;
	}
	
	public static Any parseGrok(String grokfile, String grokpattern,String line) throws GrokException {
		GrokHelper gh = new GrokHelper(grokfile, grokpattern);
		String gl = gh.parseGrok(line);
		
		
		Any sl=JsonIterator.deserialize(gl);

	
		return  sl;
	}
	
	public static String getTimestamp(Any date) throws ParseException {
		String sdate = date.get("HTTPDATESIMPLE").toString();
		String year = date.get("YEAR").toString();
		String day = date.get("MONTHDAY").toString();
		String time = date.get("TIME").toString();
	
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss");
		Date d = sdf.parse(sdate);

		Integer month = d.getMonth();
		month = month+1;

		String timestamp = year+"-"+month+"-"+day+"T"+time;
		if(month<10) {
			 timestamp = year+"-0"+month+"-"+day+"T"+time;

		}
         return timestamp;			
	}
	

}
