package mygame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;

public class BladeMain {
    public static int port=5010;
    public static HashMap<String,String> serverMap;
    public static List<String> addressList;
    public static String serverIP="localhost";

    static {
        serverMap=new HashMap();
        serverMap.put("larry", "24.20.242.41");
        serverMap.put("evan", "67.160.181.221");
        serverMap.put("localhost","localhost");
        addressList=new ArrayList();
        addressList.add("larry");
        addressList.add("evan");
        addressList.add("localhost");
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
      //      serverIP=JOptionPane.showInputDialog("ServerIP?", "localhost");
            BladeClient.main(args);
            System.out.println("Starting Client");
        }
         else{
            System.out.println("'"+args[0]+"' is not a proper command.");
        }
    }
}
