package helper;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

public class Utility {

    private static final Logger log = LoggerFactory.getLogger(Utility.class);

	
    public static void deleteFile(String file) throws Exception {
		File f = new File(file);
		f.delete();
	}
    
    public static void deleteFileInDirectory(String directory) throws Exception {
		File f = new File(directory);
		FileUtils.cleanDirectory(f); 
	}
    
	 
    public static String saveToFile(Model model, String outputDir, String fileName) {
    	String outputFileName = outputDir + fileName;
        File outputFile = new File(outputFileName);
        outputFile.getParentFile().mkdirs();

        try {
            FileWriter out = new FileWriter(outputFile);
            model.write(out, "TURTLE");
            out.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return outputFileName;
    }

	public static Map<String, Object> readYamlFile(String file) throws FileNotFoundException{
		InputStream input = readFile(file);
	    
	    Yaml yaml = new Yaml();
	    
	    Map<String, Object> yamlContent = yaml.load(input);
	    
	    
		return yamlContent;
		
	}
	
	protected static InputStream readFile(String file) throws FileNotFoundException {
		final File initialFile = new File(file);
		//System.out.print(initialFile);
	    final InputStream input = new FileInputStream(initialFile);
	    return input;

	}
	
	public static String getOriginalFileName(String filename){
	   	String fileName = filename.substring(filename.lastIndexOf("/") + 1);
        if (fileName.indexOf("\\") >= 0) {
            fileName = filename.substring(filename.lastIndexOf("\\") + 1);
        }
        
        return fileName;
        
	}

	public static void copyFileUsingStream(String sourceFile, String destFile) throws IOException {
		File source = new File(sourceFile);
		File dest = new File(destFile);
		
		
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
	
    public static ArrayList<String> listFilesForFolder(final File folder) {
    	ArrayList<String> rulefiles = new ArrayList<String>();
    	
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
            	rulefiles.add(fileEntry.getName());
                // System.out.println(fileEntry.getName());
            }
        }
        
        return rulefiles;
    }
    
			public static String saveToRDF(Model model, String outputdir, String namegraph) throws Exception {
			System.out.print("Save model to rdf file...");
			String filename = Utility.getOriginalFileName(namegraph)+".ttl";
			String rdfFile = Utility.saveToFile(model,outputdir,filename);
			System.out.println("Done!");
			return rdfFile;
		}

	
	
}
