/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Raghu
 */
public class Launcher {
    
    public static Client _client;
    
    public static void main(String args[]){
        
        try {
            BufferedReader userInputMain=new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Please enter your name:- ");
            String name = userInputMain.readLine();
            
            _client = new Client(name);
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(Exception ex){
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
