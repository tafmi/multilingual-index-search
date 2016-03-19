
package indexer;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import java.io.File;

public class LanguageDetector {
    
    private  Detector detector;
    
    public LanguageDetector() throws LangDetectException{
        String profiles=System.getProperty("user.dir")+File.separator+"profiles";
        DetectorFactory.loadProfile(profiles);
    }
    
    public   String detectLang(String s) throws LangDetectException{
       String language="";
       try{ 
       
                 detector = DetectorFactory.create();
                 detector.append(s);
                 
                  language=detector.detect();
                 
                 }
                 catch(LangDetectException e){
                     e.getMessage();
                 }
                 return language;
    }
}
