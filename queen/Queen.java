package queen;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
// for phase timing
import java.util.Timer;
import java.util.TimerTask;
import java.lang.System;
// for array manipulation
import java.util.Arrays;

import general.Byzantine;
import general.General;
import general.Message;
import general.MessageRMI;
import general.Byzantine.ByzanType;


public class Queen implements MessageRMI, Runnable {

    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    Float[] weights; // weights indexed the same as the ports. 
    Boolean[] receivedValue;

    Registry registry;
    MessageRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    // list of the anchors based on the weights
    Integer[] Anchor; 

    // the phase that is currently happening? 
    int phaseNo;
    // iteration number
    int iterNo; 
    boolean running;

    //float myAdvanceWeight; 
    Timer phaseTimer;
    long phaseInterval; // in milliseconds 

    // specific to queen binary agreement
    boolean finished;

    float s0, s1, myWeight; 
    int V;
    Integer myValue, queenValue; // can be null

    Byzantine byz; 

    private class LocalState extends TimerTask{
        int iteration; // 0 = first anchor, 1 = second anchor, ...
        int phase; // 0 = inactive, 1 = phase 1, 2 = queen phase (repeats)
        Boolean stateChange; // true if a state has changed. Additional Action
                             // will be required.
        //Boolean finished;    // true if the Queen has completed.
        ReentrantLock stateMutex;

        public LocalState(Integer iter, Integer pha){
            iteration = iter;
            phase = pha;
            stateChange = false; 
            //finished = false; 
            stateMutex = new ReentrantLock();
        }

        // this is the periodic timer task that I need to have. 
        /**
         * Everything that happens at the conclusion of the phase will 
         * happen in this task.
         */
        public void run()
        {
            //System.out.println("Node "+me+": Periodic Timer Task called at: "+
            //                System.currentTimeMillis()+"ms");
            // determine current phase:
            switch(this.get_current_phase()){
                case 1:
                    // do the phase 1 transition
                    if(s1 > 0.5f){
                        myValue = 1; 
                        myWeight = s1; 
                    } else {
                        myValue = 0; 
                        myWeight = s0; 
                    }
                    break;
                case 2: 
                    // arbitary in case of a faulty queen
                    if(queenValue == null){
                        queenValue = 0; 
                    }
                    // do the phase 2 transition
                    if(myWeight > 3.0f/4.0f){
                        V = myValue; 
                    } else {
                        V = queenValue; 
                    }
                    break;
                default: 
                    System.out.println("ERROR: Unsupported phase " +
                                    this.get_current_phase() + " chosen");
            }
            increment_local_state();
            // clear variables
            s0 = 0; 
            s1 = 0; 
            queenValue = null; 
            // allow another value to be recorded in the next phase
            Arrays.fill(receivedValue,false); 
        }

        public void increment_local_state(){
            // check to make sure the run() method has at least had 
            // 1 go at the new state
            //while(stateChange == true){
                //wait
            //}
            
            // specific to the queen algorithm
            
            this.phase = this.phase + 1;
            if (this.phase == 3){
                // reset to the first phase
                this.phase = 1; 
                this.iteration = this.iteration + 1;
            } 
            stateMutex.lock();
            this.stateChange = true; 
            stateMutex.unlock();
        }

        public int get_current_phase(){
            return this.phase;
        }

        public int get_current_iteration(){
            return this.iteration;
        }

        public boolean is_equal(LocalState state2){
            return(this.iteration == state2.iteration &&
                   this.phase == state2.phase);
        }
    }
    
    LocalState localState;


    public Queen(int me, String[] peers, int[] ports, Float[] weights){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.weights = weights;
        this.Anchor = General.calculate_anchor(weights, 1.0f/4.0f);
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
        this.iterNo = 0; 
        this.phaseNo = 1;

        //this.myAdvanceWeight = 0;
        this.localState = new LocalState(0, 0); 

        this.phaseTimer = new Timer();
        this.phaseInterval = 5 * 1000; // 5 seconds

        this.s0 = 0; 
        this.s1 = 0;

        this.myValue = null;
        this.queenValue = null;
        this.myWeight = 0; 

        this.byz = new Byzantine(ByzanType.CORRECT); 

        this.receivedValue = new Boolean[peers.length];
        Arrays.fill(receivedValue,false); 

        this.finished = false; 

        // should be supplied by test
        this.V = -1; 

        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (MessageRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Peer", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void Start(Integer value){
        // Your code here
        this.V = value; 
        (new Thread(this)).start();
    }

    public retStatus Status(){
        retStatus status = new retStatus(this.finished,this.V);
        return status; 
    }

    public class retStatus{
        public Boolean state;
        public Object v;

        public retStatus(Boolean state, Object v){
            this.state = state;
            this.v = v;
        }
    }

    public void run(){
        // System.out.println("Node "+this.me+": Current Time Started: "+System.currentTimeMillis()+"ms"); 
        // schedule the local state to run at the appropriate phase interval, 
        phaseTimer.schedule(this.localState,phaseInterval,phaseInterval);
        localState.increment_local_state();

        while(localState.get_current_iteration() < Anchor.length)
        {
            // phase advance has taken place
            if(this.localState.stateChange == true)
            {
                // beginning of a new phase
                //System.out.println("Advancing to phase: " + this.localState.phase +
                //                " on iteration: " + this.localState.iteration);
                // Debugging Simply.
                if(this.me == 0){
                    System.out.println("Iteration: "+localState.get_current_iteration()+
                                       "    Phase " + localState.get_current_phase()); 
                }
                // set to false;
                this.localState.stateMutex.lock(); 
                this.localState.stateChange = false; 
                this.localState.stateMutex.unlock();

                // determine current phase
                switch(this.localState.get_current_phase())
                {
                    case 1: 
                        //System.out.println("Node "+this.me+": performing exchange phase on interation "+localState.get_current_iteration());
                        // wait 1 second before beginning (should help keep all messages in phase)
                        General.wait_millis(2000);
                        exchange_value(V);
                        break; 
                    case 2: 
                        //System.out.println("Node "+this.me+": performing queen phase on iteration"+localState.get_current_iteration()); 
                        if(Anchor[localState.get_current_iteration()] == this.me){
                            // i'm the queen
                            System.out.println("Node "+this.me+": I'm the queen!");
                            General.wait_millis(2000);
                            exchange_value(myValue);
                        }
                        break; 
                
                    default: 
                        System.out.println("ERROR: Phase " + this.localState.get_current_phase() + 
                                        "not supported in main run"); 

                }
                
            } else {
                // wait 500 milliseconds to check again. 
                General.wait_millis(500); 
            }
            
        }

        // stop repeating
        phaseTimer.cancel();
        phaseTimer.purge();
       
        this.finished = true; 

        System.out.println("Node " + this.me + " finished with agreed value of " + this.V); 
    
    }

    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     */
    public Boolean Call(String rmi, Message msg, int id){

        MessageRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(MessageRMI) registry.lookup("Peer");
            if(rmi.equals("Send"))
                stub.Receive(msg);
            //else if(rmi.equals("Advance"))
            //    stub.Advance(msg); 
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return false;
        }
        return true;
    }

    /**
     * Receive RMI Message Handler
     */
    // FIXME: Add Idempotence, ways to sync
    public void Receive(Message msg){
        // am I running? 
        
        // check to see if the message is received and new: 
        if(receivedValue[msg.get_peer_id()] == false)
        {
            receivedValue[msg.get_peer_id()] = true; 
            switch(this.localState.get_current_phase())
            {
                case 1: 
                    if(msg.get_value() == 1){
                        s1 = s1 + this.weights[msg.get_peer_id()];
                    } else {
                        s0 = s0 + this.weights[msg.get_peer_id()];
                    }
                    break;
                case 2:
                    // make sure that the message sender is the pre-determined queen
                    if(msg.get_peer_id()==this.Anchor[localState.get_current_iteration()]){
                        queenValue = msg.get_value(); 
                    }
                    break;
                default: 
                    System.out.println("ERROR: Phase " + this.localState.get_current_phase() + 
                    "not supported in recevie"); 
            }
        }
        else {
            System.out.println("Message from "+msg.get_peer_id()+" already received in this phase");
        }
        
    }

    /*
    // value of the advance message can be the local state
    public void Advance(Message msg){
        // check to see if we are in the same state, else ignore
        if(this.localState.isEqual((LocalState)msg.get_value())){
            // if I have received this advance, then i Increment the advance weight
            myAdvanceWeight = myAdvanceWeight + weights[msg.get_peer_id()];
            // this is our condition for continuing
            if(myAdvanceWeight > 3.0f/4.0f){
                this.localState.increment_local_state();    
                myAdvanceWeight = 0; 
            } 
        }
        

    }
    */ 

    private void exchange_value(int val){
        // craft message
        Message msg; 
        Integer newVal; 

        boolean amiqueen = (this.me == Anchor[this.localState.get_current_iteration()] &&
                            this.localState.get_current_phase() == 2); 
        // send to everybody
        for(int i = 0; i < ports.length; i++){
            // this will only change value if there is a byzantine setting
            newVal = byz.Byzantine_Filter(val, i, amiqueen);
            if(newVal == null || val != newVal) {
                System.out.println("Node "+this.me+" sends altered message val "+
                                   newVal+" from original "+val+"."); 
            }
            if(newVal != null){
                msg = new Message(this.me, newVal); 
                Call("Send",msg,i); 
            }
           
        }

    }

    /*
    private void ready_to_advance()
    {
        Message msg = new Message(this.me, this.localState);
        // send "I'm ready to advance" to everybody.
        for(int port : ports){
            Call("Advance",msg,port);
        }
    }
    */

    /** 
     * Testing functions, allow for node to act in pequiliar ways
     * 
     */
    public void Kill(){
        //System.out.println("Node "+this.me+" has been killed");
        this.dead.getAndSet(true);
        // cancel periodic task too
        this.phaseTimer.cancel();
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

    public void set_byzantine(ByzanType bt){
        this.byz.set_byzantype(bt);
    }

    public boolean isDead(){
        return this.dead.get();
    }

    public void setUnreliable(){
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable(){
        return this.unreliable.get();
    }

}
