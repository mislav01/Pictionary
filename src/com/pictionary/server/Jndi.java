package com.pictionary.server;

import com.pictionary.gui.GameScreenController;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Jndi {

    private Hashtable env;
    private BufferedReader reader;
    private String line;
    private int port;
    private ArrayList<String> wordList = new ArrayList<>();
    private int points;

    public void doJndi() {
        env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.fscontext.RefFSContextFactory");
        env.put(Context.PROVIDER_URL, "file:C:/");

        try {
            Context ctx = new InitialContext(env);

            Object obj = ctx.lookup("Pictionary.cfg");

            reader = new BufferedReader(new FileReader(obj.toString()));
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("=");
                if (tokens[0].equals("port")) {
                    port = Integer.parseInt(tokens[1]);
                } else if (tokens[0].equals("points")) {
                    points = Integer.parseInt(tokens[1]);
                }  
                else{
                    wordList.add(tokens[0].toLowerCase());
                }
            }

            reader.close();
            ctx.close();

            Server server = new Server(port, wordList, points);
            server.start();
        } catch (NamingException ex) {
            Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Jndi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Jndi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
