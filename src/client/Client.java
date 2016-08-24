/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Raghu
 */
public class Client {
    
    
    public Socket _socket;
    public String _name;
    public Thread _keyboardReadStream;
    public BufferedReader _inputFromStream;
    public Thread _userReadStream;
    
    public Client(String name) {
        try {
            _socket = new Socket("localhost", 58000);
            
            this._name = name;
            
            _keyboardReadStream = new ReadWriteStream(System.in,_socket.getOutputStream(),name,true);
            _userReadStream = new ReadWriteStream(_socket.getInputStream(),System.out,name,false);
            
            _keyboardReadStream.start();
            _userReadStream.start();
        }catch (SocketException ex){
            _keyboardReadStream.interrupt();
            _userReadStream.interrupt();
            System.out.println("Socket closed");
        }
        catch (IOException ex) {
            _keyboardReadStream.interrupt();
            _userReadStream.interrupt();
            System.out.println("Stream error. Closing socket.");
        }
        
        
    }
}