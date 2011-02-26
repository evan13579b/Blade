/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mygame;

import com.jme3.network.events.MessageAdapter;
import com.jme3.network.message.Message;

/**
 *
 * @author blah
 */
public class InputMessageListener extends MessageAdapter{
    @Override
    public void messageReceived(Message message){
        System.out.println("Message Received");
    }
}
