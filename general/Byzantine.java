package general;

import java.util.Random;

public class Byzantine {

    public enum ByzanType {
        // types of byzantine behavior
        CORRECT,
        MEAN_QUEEN,         // act normal until the queen/king round and then
                            // try and throw everyone off
        OPPOSITES_REPEL,    // send the opposite of what the correct process sends
        RANDOM_RANDY,       // send random values
        SEMI_SILENT_SALLY,  // omit messages at random
        //SPAMMY_SAMMY,       // sends many messages per phase
        PARTICULARLY_PEEPISH_PAULA  // only send messages to certain 
    }

    ByzanType myByzanType; 
    Random rand; 
    int semiSilentRate; 
    int upperbound = 2; // random number of 0 or 1 

    // NOTE: -1 will correspond to silence, not sending.

    public Byzantine(ByzanType byz)
    {
        myByzanType = byz;
        // seeded with different value
        // rand = new Random(System.currentTimeMillis());
        // seeded repetetively
        rand = new Random(2222); 
        //semiSilentRate = rand.nextInt(7)+2; // 20 - 80 % silent. Allows 
                                            // for greater randomness in how silent
        semiSilentRate = 5; 
    }

    public void set_byzantype(ByzanType byz)
    {
        myByzanType = byz; 
    }

    public Integer Byzantine_Filter(int intended, int recipient_id, boolean amiqueen)
    {
        Integer outValue; 
        switch(this.myByzanType)
        {
            case CORRECT:
                outValue = intended;
                break;
            case MEAN_QUEEN:
                if(amiqueen){
                    // more feindish to be random than just to swap
                    outValue = rand.nextInt(2); 
                } else {
                    outValue = intended; 
                }
                break;
            case OPPOSITES_REPEL:
                outValue = swap(intended);
                break;
            case RANDOM_RANDY:
                outValue = rand.nextInt(upperbound); // should give 0 or 1
                break;
            case SEMI_SILENT_SALLY: 
                if(rand.nextInt(10)>=semiSilentRate){
                    outValue = intended; 
                } else {
                    outValue = -1; 
                }
                break;
            case PARTICULARLY_PEEPISH_PAULA: 
                // currently only sending to even process ID's
                if(recipient_id%2 == 0)
                {
                    outValue = intended;
                } else {
                    outValue = -1;
                }
                break;
            default:
                System.out.println("Byzantine method "+this.myByzanType+" not supported"); 
                outValue = intended;
        }
        return outValue;
    }

    public Integer Byzantine_Confidence_Filter(int intended, int recipient_id)
    {
        Integer outValue; 
        switch(this.myByzanType)
        {
            case CORRECT:
                outValue = intended;
                break;
            case MEAN_QUEEN:
                outValue = swap_confidence(intended);
                break;
            case OPPOSITES_REPEL:
                outValue = swap_confidence(intended);
                break;
            case RANDOM_RANDY:
                outValue = rand.nextInt(3); // should give 0 to 2
                break;
            case SEMI_SILENT_SALLY: 
                // the standard filter takes care of silence
                outValue = intended; 
                break;
            case PARTICULARLY_PEEPISH_PAULA: 
                // the standard filter takes care of silence
                outValue = intended; 
                break;
            default:
                System.out.println("Byzantine method "+this.myByzanType+" not supported"); 
                outValue = intended;
        }
        return outValue;
    }

    public static ByzanType get_random_byzantine_type(int rand_int)
    {
        int x = (rand_int % 5) + 1;  
        ByzanType selected; 
        switch(x)
        {
            case 1: 
                selected = ByzanType.MEAN_QUEEN; 
                break;
            case 2:
                selected = ByzanType.OPPOSITES_REPEL; 
                break;
            case 3: 
                selected = ByzanType.RANDOM_RANDY;
                break;
            case 4:
                selected = ByzanType.SEMI_SILENT_SALLY; 
                break;
            case 5: 
                selected = ByzanType.PARTICULARLY_PEEPISH_PAULA; 
                break;
            default:
                System.out.println("correct byzantype selected");
                selected = ByzanType.CORRECT; 
        }
        return selected; 
    }

    private Integer swap(int x){
        if(x == 1){
            return 0;
        } else {
            return 1; 
        }
    }

    private Integer swap_confidence(int x){
        if(x <= 1){
            return 2;
        } else {
            return 0; 
        }
    }
}
