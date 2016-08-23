/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;


/**
 *
 * @author Raghu
 */
public class ReadWriteStream extends Thread{
    static final Charset _utf8 = Charset.forName("UTF-8");
    
    BufferedReader _inputBufferedReader;
    PrintStream _outputPrintStream;
    boolean _isFromKeyboardToServer;
    static final String delimitor = " ";
    
    String _name;
    
    public ReadWriteStream(InputStream inputStream, OutputStream outputStream, String name,boolean isFromKeyboardToServer) throws IOException {
        super("Input from thread");
        
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        _inputBufferedReader = new BufferedReader(inputStreamReader);
        _outputPrintStream=new PrintStream(outputStream);
        _name = name;
        _isFromKeyboardToServer = isFromKeyboardToServer;
    }
    
    @Override
    public void run(){
        if(_isFromKeyboardToServer){
            this._outputPrintStream.println("@SERVER ADDME "+_name);
        }
        
        try {
            while(true){
                String line = _inputBufferedReader.readLine();
                if(_isFromKeyboardToServer){
                    if(line.contains("#")){
                        String[] tempArray = line.split(delimitor);
                        tempArray[0] = "";
                        line = Arrays.toString(tempArray);
                    }
                }
                this._outputPrintStream.println(line);
            }
        } catch (Exception ex) {
            System.out.println("IOException"+ex);
        }
    }
    
}
