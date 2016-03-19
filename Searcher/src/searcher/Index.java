
package searcher;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Index {
    
    private final String lang;
    private final Directory directory;
    private final HashMap<String,HashMap<String,Float>> tfidfMap;
    private String langContents;
    private String tfidfContents;
    private Analyzer analyzer;
    
    public Index(String lang,File store) throws IOException{
        
        this.lang=lang;
        langValues();
        directory= FSDirectory.open(store.toPath());
        tfidfMap=new HashMap<>();
    }
    
    private void langValues(){
        switch (lang) {
            case "el":
                analyzer=new GreekAnalyzer();
                langContents="el_contents";
                tfidfContents="el_contents_hightfidf";
                break;
            case "en":
                analyzer=new EnglishAnalyzer();
                langContents="en_contents";
                tfidfContents="en_contents_hightfidf";
                break;
            case "de":
                analyzer=new GermanAnalyzer();
                langContents="de_contents";
                tfidfContents="de_contents_hightfidf";
                break;
            case "fr":
                analyzer=new FrenchAnalyzer();
                langContents="fr_contents";
                tfidfContents="fr_contents_hightfidf";
                break;
            case "it":
                analyzer=new ItalianAnalyzer();
                langContents="it_contents";
                tfidfContents="it_contents_hightfidf";
                break;
            case "es":
                analyzer=new SpanishAnalyzer();
                langContents="es_contents";
                tfidfContents="es_contents_hightfidf";
                break;               
            default:
                analyzer=new StandardAnalyzer();
                langContents="other_contents";
                tfidfContents="other_contents_hightfidf";
                break;
        }
    }
    
    public void searchQuery(String term) throws IOException, ParseException{
        Set<String> results=new HashSet<>();
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(ireader);
        QueryParser parser=new QueryParser("filename",new StandardAnalyzer());
        Query query=parser.parse(QueryParser.escape(term));
         TopDocs topdocs=searcher.search(query,null,10000);
         ScoreDoc[] hits=topdocs.scoreDocs;
         for (ScoreDoc hit : hits) {
                int docID = hit.doc;
                Document doc=searcher.doc(docID);
                results.add(doc.get("path"));
         }
         Set<String> tfidfresults=new HashSet<>();
         String[] splitterm=term.split("\\s+");
         for(String split:splitterm){
            QueryParser tfidfparser=new QueryParser(tfidfContents,analyzer);
            query=tfidfparser.parse(QueryParser.escape(split));
            topdocs=searcher.search(query,null,10000);
            hits=topdocs.scoreDocs;
            for (ScoreDoc hit : hits) {
                   int docID = hit.doc;
                   Document doc=searcher.doc(docID);
                   tfidfresults.add(doc.get("path"));
            } 
         }
         Set<String> cresults=new HashSet<>();
         QueryParser cparser=new QueryParser(langContents,analyzer);
         query=cparser.parse(QueryParser.escape(term));
         topdocs=searcher.search(query,null,10000);
         hits=topdocs.scoreDocs;
         for (ScoreDoc hit : hits) {
                int docID = hit.doc;
                Document doc=searcher.doc(docID);
                cresults.add(doc.get("path"));
         }
         for(String res:cresults){
             if(tfidfresults.contains(res)){
                 results.add(res);
             }
         }
         System.out.println("Found "+results.size()+" results:");
         for(String s:results){
             System.out.println(s);
         }
    }
}
