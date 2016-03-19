
package searcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;

public class Searcher {

    public static void main(String[] args) throws ParseException {
   
        try{
            if(args.length!=3){
                throw new InvalidArgumentException();
            }
            else{
                 String query=args[0];
                 String lang=args[1];
                 String inputStore=args[2];
                 File store=new File(inputStore);
                 if(!"el".equals(lang) && !"en".equals(lang) && !"de".equals(lang) 
                         && !"fr".equals(lang) && !"it".equals(lang) && !"es".equals(lang)){
                     throw new InvalidLangException("Input language is not valid"
                             + "Please specify one of the following:\n"
                             + "de for German\n"
                             + "el for Greek\n"
                             + "en for English\n"
                             + "es for Spanish\n"
                             + "fr for French\n"
                             + "it for Italian");
                 }
                 if(!store.exists()){
                    throw new FileNotFoundException("Input index directory does not exist."
                            + " Please specify a valid input directory.");
                 }
                 if(!store.isDirectory()){
                    throw new IOException("The input index path that was specified is not a directory."
                            + " Please specify a valid directory.");
                 }
                 
                 start(query,lang,store);  
            }         
        }
        catch(InvalidArgumentException iaex){
            System.out.println("Application usage:");
            System.out.println("<executble_name> <directory> <index_directory>");
        } catch(InvalidLangException ilex){
            System.out.println("Error: "+ilex.getMessage());
        } catch (FileNotFoundException fnfex) {
            System.out.println("Error: "+fnfex.getMessage());
        } catch (IOException ioex) {
            System.out.println("Error: "+ioex.getMessage());
        }
    }
    
    private static void start(String query,String lang,File store) throws IOException, ParseException{
        Index index= new Index(lang,store);
        index.searchQuery(query);
    }
}
