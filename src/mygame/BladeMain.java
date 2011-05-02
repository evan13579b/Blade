package mygame;

import javax.swing.JOptionPane;

public class BladeMain {
    public static int port=5010;

    static {
 
    }

    public static void main(String[] args) {
        if(args.length==0){
            String newArg=JOptionPane.showInputDialog("Start as server or client?", "client");
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
