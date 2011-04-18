/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author blah
 */
public class IOLib {    
    public static Map<String,String> getIpAddressMap(){
        BufferedReader reader = null;
        Map<String,String> ipAddressMap=new HashMap();
        try {
            String line;
            String[] parts;
            reader = new BufferedReader(new FileReader("IpAddressMap.txt"));
            while((line=reader.readLine())!=null){
                line=line.trim();
                parts=line.split("\\s+");
                if(parts.length==2){
                    ipAddressMap.put(parts[0], parts[1]);
                    System.out.println("name: "+parts[0]+", ip: "+parts[1]);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(IOLib.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(IOLib.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ipAddressMap;
    }
}
