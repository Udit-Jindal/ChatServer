/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package chatEngine;

import java.util.Arrays;

/**
 *
 * @author Raghu
 */
public class StringParser {
    
//    private String inputLine;
    private int returnCode;
    private String[] inputCommand;
    private String returnString;
    private static final String delimitor = " ";
    
    public boolean isCommand = false;
    public String inputFor;
    public ParsedData parcedData;
    
    public StringParser(){
        parcedData = new ParsedData();
    }
    
    //<editor-fold defaultstate="collapsed" desc="Getters and setters">
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
    //</editor-fold>
    
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
        
        String[] tempArray = input.split(delimitor);
        
        if(tempArray[0].equalsIgnoreCase("@SERVER")){
            isCommand = true;
            inputCommand = tempArray;
            inputFor = "SERVER";
            generateReturnCode();
        }else if(tempArray[0].contains("@")){
            inputFor = tempArray[0].replace("@", "");
            tempArray[0] = "";
            returnString = Arrays.asList(tempArray).toString();
            returnString = returnString.substring(1, returnString.length()-1).replaceAll(",", "");
        }
        else{
            this.returnString = input;
            inputFor = "ALL";
        }
    }
    
    public void generateReturnCode(){
        String pointer = inputCommand[1];
        pointer = pointer.toUpperCase();
        switch (pointer) {
            
            case "ADDME":{
                returnCode = 000;
                parcedData.userName = inputCommand[2];
            }
            break;
            
            case "SHOWROOMLIST":{
                returnCode = 001;
            }
            break;
            
            case "SHOWUSERLIST":{
                returnCode = 002;
            }
            break;
            
            case "SHOWUSERINROOMLIST":{
                returnCode = 003;
                parcedData.roomName = inputCommand[2];
            }
            break;
            
            case "CONNECTTOROOM":{
                returnCode = 004;
                parcedData.roomName = inputCommand[2];
            }
            break;
            
            case "EXITROOM":{
                returnCode = 005;
            }
            break;
            
            case "EXIT":{
                returnCode = 111;
            }
            break;
            
            default:
            {
                returnCode = 400;//Malformed Request.
            }
        }
        
    }
    
    
    public class ParsedData{
        
        public String userName=null;
        public String roomName=null;
        
    }
    
}
