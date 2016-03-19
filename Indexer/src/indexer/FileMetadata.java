/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author Teo
 */
public class FileMetadata {
    
    private final ContentHandler handler;
    private final String filename;
    
    public FileMetadata(File file) throws FileNotFoundException, IOException, SAXException, TikaException{
        filename=file.getName().replaceFirst("[.][^.]+$", "");
        Metadata metadata = new Metadata();
        InputStream input = new FileInputStream(file);
        handler = new BodyContentHandler(10*1024*1024);     
        Parser parser = new AutoDetectParser();        
        ParseContext context = new ParseContext();
        parser.parse(input, handler, metadata,context);       
    }
    
    public String getFilename(){
       return filename;
    }
    
    public String getContents(){
       return handler.toString();
    }
}
