/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package chatEngine;

/**
 *
 * @author Raghu
 */
public class StringParser {
    
    private String inputLine;
    private int returnCode;
    private String[] inputCommand;
    private String returnString;
    
    public boolean isCommand;
    
    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }
    
    public void setIsCommand(boolean isCommand) {
        this.isCommand = isCommand;
    }
    
    public void setInputCommand(String[] inputCommand) {
        this.inputCommand = inputCommand;
    }
    
    public String getReturnString() {
        return returnString;
    }
    
    public int getReturnCode() {
        return returnCode;
    }
    
    public void setInputLine(String inputLine) {
        this.inputLine = inputLine;
    }
    
    public void processInput(String inputLine){
        parseInput(inputLine);
        if(isCommand)
        {
            generateReturnCode();
        }
        else{
            setReturnCode(100);//Continue
        }
    }
    
    public void parseInput(String input){
        
    }
    
    public void generateReturnCode(){
        
    }
    
}
