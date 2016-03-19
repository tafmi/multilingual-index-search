
package indexer;

import com.cybozu.labs.langdetect.LangDetectException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

public class Index {
    
    private final Directory directory;
    private final LanguageDetector detector;
    private static HashMap<String,Analyzer> langMap;
    

    public Index(File file,File store) throws IOException, FileNotFoundException, SAXException, TikaException, LangDetectException{
        
        detector=new LanguageDetector();
        if(store.exists()){
           deleteDirectory(store);
        }
        directory= FSDirectory.open(store.toPath());
        indexDirectory(file);
        initLangMap();
        for(Map.Entry pair:langMap.entrySet()){
            tfidf((String)pair.getKey(),(Analyzer)pair.getValue());
        }
    }
    
    private void initLangMap(){
        langMap=new HashMap<>();
        langMap.put("el_contents", new GreekAnalyzer());
        langMap.put("en_contents", new EnglishAnalyzer());
        langMap.put("es_contents", new SpanishAnalyzer());
        langMap.put("fr_contents", new FrenchAnalyzer());
        langMap.put("it_contents", new ItalianAnalyzer());
        langMap.put("de_contents", new GermanAnalyzer());
        langMap.put("other_contents", new StandardAnalyzer());
    }
    
    private void deleteDirectory(File directory) {
     if(directory.exists()){
        File[] files = directory.listFiles();
        if(null!=files){
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
     } 
    }

    private void indexDirectory(File file) throws IOException, FileNotFoundException, SAXException, TikaException, LangDetectException{

        File[] files=file.listFiles();
        for(File f:files){
            if(f.isDirectory()){
               indexDirectory(f);
            }
            else{
                indexFile(f);
            }
        }       
    }

     private void indexFile(File file) throws IOException, FileNotFoundException, SAXException, TikaException, LangDetectException{

         if(file.isHidden() || !file.canRead()){
           return;
         }
         FileMetadata meta=new FileMetadata(file);
         Document document=new Document();
         String path=file.getCanonicalPath();
         document.add(new Field("path",path,TextField.TYPE_STORED));
         document.add(new Field("filename",meta.getFilename(),TextField.TYPE_STORED));      
         IndexWriterConfig config=new IndexWriterConfig(new StandardAnalyzer());
         try (IndexWriter writer = new IndexWriter(directory,config)) {
            writer.addDocument(document);
         }
         if(isDocument(file)){  
            String contents=meta.getContents();
            if(contents!=null){                
               indexContent(path,contents);     
            }
         }
    }
    
     private void indexContent(String path,String contents) throws LangDetectException, IOException{
         
        Document document=new Document();
        document.add(new Field("path",path,TextField.TYPE_STORED));
        FieldType fieldType = new FieldType();
        fieldType.setStoreTermVectors(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        fieldType.setStored(true);        
        String language=detector.detectLang(contents);
        Analyzer analyzer;
        String langContents;
        switch (language) {
            case "el":
                analyzer=new GreekAnalyzer();
                langContents="el_contents";
                break;
            case "en":
                analyzer=new EnglishAnalyzer();
                langContents="en_contents";
                break;
            case "de":
                analyzer=new GermanAnalyzer();
                langContents="de_contents";
                break;
            case "fr":
                analyzer=new FrenchAnalyzer();
                langContents="fr_contents";
                break;
            case "it":
                analyzer=new ItalianAnalyzer();
                langContents="it_contents";
                break;
            case "es":
                analyzer=new SpanishAnalyzer();
                langContents="es_contents";
                break;               
            default:
                analyzer=new StandardAnalyzer();
                langContents="other_contents";
                break;
        }
        document.add(new Field(langContents,contents,fieldType));
        IndexWriterConfig config=new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(directory,config)) {
            writer.addDocument(document);
        }
     }
     
     private void tfidf(String contentsField,Analyzer analyzer) throws IOException{
        HashMap<String,String> finalMap=new HashMap<>();
        try (DirectoryReader ireader = DirectoryReader.open(directory)) {
            IndexSearcher isearcher=new IndexSearcher(ireader);
            for(int i=0;i<ireader.maxDoc();i++){
                if(isearcher.doc(i).getField(contentsField)!=null){
                    int j=0;
                    float totalTfIdf=0;
                    float maxTfIdf=0;
                    HashMap<String,Float> contents=new HashMap<>();
                    Terms terms = ireader.getTermVector(i,contentsField);
                    if (terms != null && terms.size() > 0){
                        TFIDFSimilarity tfidfSIM = new DefaultSimilarity();
                        TermsEnum termsEnum = terms.iterator(null); // access the terms for this field
                        BytesRef term = null;
                        while ((term = termsEnum.next()) != null) {
                            DocsEnum docsEnum = termsEnum.docs(null, null); // enumerate through documents, in this case only one
                            int docIdEnum;
                            while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                                org.apache.lucene.index.Term termInstance = new org.apache.lucene.index.Term(contentsField, term);
                                long indexDf = ireader.docFreq(termInstance);
                                float tf = tfidfSIM.tf(docsEnum.freq());
                                float idf = tfidfSIM.idf(indexDf, ireader.getDocCount(contentsField));
                                float tfidf=tf*idf;
                                if(tfidf>maxTfIdf){
                                    maxTfIdf=tfidf;
                                }
                                totalTfIdf=totalTfIdf+tfidf;
                                j++;
                                contents.put(term.utf8ToString(), tfidf);
                            }
                        }
                    }
                    String hightfidfwords="";
                    if(j>0){
                        float mTfIdf=totalTfIdf/j;
                        float limit=(mTfIdf+maxTfIdf)/2;
                        for (Map.Entry pair : contents.entrySet()) {
                            if((Float)pair.getValue()>limit){
                                hightfidfwords+=" "+(String)pair.getKey();
                            }
                        }
                    }
                    finalMap.put(isearcher.doc(i).get("path"),hightfidfwords);
                }
            }
        }
        String highField=contentsField+"_hightfidf";
        for(Map.Entry pair:finalMap.entrySet()){
            Document doc=new Document();
            doc.add(new Field("path",(String)pair.getKey(),TextField.TYPE_STORED));
            doc.add(new Field(highField,(String)pair.getValue(),TextField.TYPE_STORED));
            IndexWriterConfig config=new IndexWriterConfig(analyzer);
            try (IndexWriter writer = new IndexWriter(directory,config)) {
                writer.addDocument(doc);
            }
            
        }
     }
     
     private boolean isDocument(File file){
         String ext=file.getName().substring(file.getName().lastIndexOf(".") + 1);
        return ext.equals("txt") || ext.equals("pdf") || ext.equals("doc") || ext.equals("docx")
                || ext.equals("xls") || ext.equals("xlsx") || ext.equals("ppt") || ext.equals("pptx");
     }
}
