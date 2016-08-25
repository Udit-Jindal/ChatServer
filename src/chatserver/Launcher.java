/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package chatserver;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Raghu
 */
public class Launcher {
    
    public static void main(String[] args) {
        try {
            ChatServer chatServer = new ChatServer();
            System.out.println("Server started on: "+ (new Date()) );
        } catch (Exception ex) {
            System.out.println("Server cannot be intitated.");
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
