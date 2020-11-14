package general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/** General Package
 *  General functions that will be necessary for all of the subsequent algorithms in one place
 */


public class General {

    public static Integer[] calculate_anchor(Float[] weights, float rho){
        // Go through array and look for max weight
        // make temporary copy so don't modify actual weights

        float sum = 0.0f; 
        int i; 
        ArrayList<Integer> anchorList = new ArrayList<Integer>(); 
        while(sum < rho){
            for(i = 0; i < weights.length; i++){
                if(weights[i] == Collections.max(Arrays.asList(weights))){
                    sum = sum + weights[i]; 
                    anchorList.add(i);
                    // flip negative so that weights it 
                    // doesn't get added as max again.
                    weights[i] = -weights[i]; 
                    // break the for loop
                    break;
                }
            }
        }
        // I've modified the copy of weights, re-flip negative values
        for(i = 0; i < weights.length; i++){
            weights[i] = Math.abs(weights[i]); 
        }

        Integer[] output = new Integer[anchorList.size()];
        anchorList.toArray(output);
        // now our anchor list contains weights with a sum greater than rho
        return output;
    }

    public static void wait_millis(int millis){
        try{
            Thread.sleep(millis);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
