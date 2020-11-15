package general;

public class Byzantine {

    public enum ByzanType {
        // types of byzantine behavior
        CORRECT,
        MEAN_QUEEN,         // act normal until the queen/king round and then
                            // try and throw everyone off
        OPPOSITES_REPEL,    // send the opposite of what the correct process sends
        RANDOM_RANDY,       // send random values
        SEMI_SILENT_SALLY,  // omit messages at random
        PARTICULARLY_PEEPISH_PAULA  // only send messages to certain 
    }

    ByzanType myByzanType; 

    public Byzantine(ByzanType byz)
    {
        myByzanType = byz;
    }

    public void set_byzantype(ByzanType byz)
    {

    }

    public static Integer Byzantine_Reply(int intended, int recipient_id, boolean amiqueen)
    {
        return intended;
    }
}
