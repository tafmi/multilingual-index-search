
package indexer;

import com.cybozu.labs.langdetect.LangDetectException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Indexer {

    public static void main(String[] args) throws SAXException, TikaException, LangDetectException {
 
         try{
            if(args.length!=2){
                throw new InvalidArgumentException();
            }
            else{
                 String inputDirectory=args[0];
                 String inputStore=args[1];
                 File directory= new File(inputDirectory);
                 File store=new File(inputStore);
                 if(!directory.exists()){
                    throw new FileNotFoundException("Input directory does not exist."
                            + " Please specify a valid input directory.");
                 }
                 if(!directory.isDirectory()){
                    throw new IOException("The input path that was specified is not a directory."
                            + " Please specify a valid directory.");
                 }
                 if(!store.exists()){
                    throw new FileNotFoundException("Input index directory does not exist."
                            + " Please specify a valid input directory.");
                 }
                 if(!store.isDirectory()){
                    throw new IOException("The input index path that was specified is not a directory."
                            + " Please specify a valid directory.");
                 }
                 
                 start(directory,store);  
            }         
        }
        catch(InvalidArgumentException iaex){
            System.out.println("Application usage:");
            System.out.println("<executble_name> <directory> <index_directory>");
        } catch (FileNotFoundException fnfex) {
            System.out.println("Error: "+fnfex.getMessage());
        } catch (IOException ioex) {
            System.out.println("Error"+ioex.getMessage());
        }
    }
    
    private static void start(File directory, File store) throws IOException, FileNotFoundException, SAXException, TikaException, LangDetectException{
        Logger.getRootLogger().setLevel(Level.OFF);
		Index index=new Index(directory,store);
    }
}
