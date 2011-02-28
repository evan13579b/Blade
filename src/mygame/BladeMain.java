package mygame;

import java.util.HashMap;
import javax.swing.JOptionPane;

public class BladeMain {
    static int port=5010;
    static final HashMap<String,String> serverMap;

    static {
        serverMap=new HashMap();
        serverMap.put("larry", "24.20.242.41");
        serverMap.put("evan", "97.115.2.114");
        serverMap.put("localhost","localhost");
    }

    public static void main(String[] args) {

        if(args.length==0){
            String newArg=JOptionPane.showInputDialog("Start as server or client?", "server");
            String newArgs[]={newArg};
            BladeMain.main(newArgs);
        }
        else if(args[0].toLowerCase().equals("server")){
            BladeServer.main(args);
            System.out.println("Starting Server");
        }
        else if(args[0].toLowerCase().equals("client")){
            BladeClient.main(args);
            System.out.println("Starting Client");
        }
        else{
            System.out.println("'"+args[0]+"' is not a proper command.");
        }
    }
}
