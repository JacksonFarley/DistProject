package general; 

import java.io.Serializable;

public class Message implements Serializable
{
    static final long serialVersionUID=1L;
    
    int peerId; 
    Integer value;
    Integer confidence;


    public Message(int peer, Integer val){
        peerId = peer; 
        value = val; 
        confidence = null;
    }

    public Message(int peer, Integer val, int conf){
        peerId = peer; 
        value = val; 
        confidence = conf;
    }

    public int get_peer_id(){
        return this.peerId;
    }

    public void set_peer_id(int peer){
        this.peerId = peer;
    }

    public Integer get_value(){
        return this.value;
    }
    
    public void set_value(Integer val){
        this.value = val; 
    }

    public Integer get_confidence(){
        return this.confidence;
    }

    public void set_confidence(int conf){
        this.confidence = conf;
    }
}