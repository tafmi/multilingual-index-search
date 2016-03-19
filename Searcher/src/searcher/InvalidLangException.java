/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searcher;

/**
 *
 * @author Teo
 */
public class InvalidLangException extends Exception{
    
    public InvalidLangException(){
        super();
    }
    
    public InvalidLangException(String message) {
        super(message);
    }
}
