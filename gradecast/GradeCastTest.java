package gradecast;

import org.junit.Test;

import general.General;
import general.Byzantine;
import java.util.Random;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/**
 * This is a subset of entire test cases
 * For your reference only.
 */
public class GradeCastTest {

    private int ndecided(GradeCast[] gc){
        int counter = 0;
        Object v = null;
        GradeCast.retStatus ret;
        for(int i = 0; i < gc.length; i++){
            if(gc[i] != null){
                ret = gc[i].Status();
                if(ret.state == true) {
                    assertFalse("decided values do not match: i=" + i + " v=" + v + " v1=" + ret.v, counter > 0 && !v.equals(ret.v));
                    counter++;
                    v = ret.v;
                }

            }
        }
        return counter;
    }

    private void waitn(GradeCast[] gc, int wanted){
        int to = 10;
        for(int i = 0; i < 30; i++){
            if(ndecided(gc) >= wanted){
                break;
            }
            try {
                Thread.sleep(to);
            } catch (Exception e){
                e.printStackTrace();
            }
            if(to < 1000){
                to = to * 2;
            }
        }

        int nd = ndecided(gc);
        assertFalse("too few decided; ndecided=" + nd + " wanted=" + wanted, nd < wanted);

    }

    private void waitn_iter(GradeCast[] gc, int wanted, int num_iterations)
    {
        int to = 1000;
        for(int i = 0; i < (10*num_iterations+5); i++){
            if(ndecided(gc) >= wanted){
                break;
            }
            try {
                Thread.sleep(to);
            } catch (Exception e){
                e.printStackTrace();
            }

        }

        int nd = ndecided(gc);
        assertFalse("too few decided; ndecided=" + nd + " wanted=" + wanted, nd < wanted);

    }

    /*
    private void waitmajority(GradeCast[] gc){
        waitn(gc, (gc.length/2) + 1);
    }
    */

    private void cleanup(GradeCast[] gc){
        for(int i = 0; i < gc.length; i++){
            if(gc[i] != null){
                gc[i].Kill();
            }
        }
    }

    private GradeCast[] initGradeCast(int nqueen, Float[] weights){
        String host = "127.0.0.1";
        String[] peers = new String[nqueen];
        int[] ports = new int[nqueen];
        GradeCast[] gc = new GradeCast[nqueen];
        for(int i = 0 ; i < nqueen; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        for(int i = 0; i < nqueen; i++){
            gc[i] = new GradeCast(i, peers, ports, weights);
        }
        return gc;
    }

    @Test 
    public void TestSingleGradeCast()
    {
        int nqueen = 1; 
        Float[] weights = {1.0f}; 
        GradeCast[] gc = initGradeCast(nqueen, weights);

        System.out.println("Test: Single GradeCast ...");

        gc[0].Start(1);
        
        waitn(gc, 1); 
        assertFalse("Validity not satisfied, gc has value" + gc[0].V, gc[0].V != 1);
        cleanup(gc); 

        gc = initGradeCast(nqueen, weights);

        gc[0].Start(0);

        waitn(gc, 1); 
        assertFalse("Validity not satisfied, gc has value" + gc[0].V, gc[0].V != 0);

        cleanup(gc);

        System.out.println("Variable weights"); 

        nqueen = 5; 
        Float[] new_weights = {0.1f,0.6f,0.3f,0.0f,0.0f}; 

        gc = initGradeCast(nqueen, new_weights);

        gc[0].Start(0);
        gc[1].Start(1);
        gc[2].Start(0);
        gc[3].Start(0);
        gc[4].Start(0);

        // only wait for one iteraion
        waitn_iter(gc, 5, 1); 
        assertFalse("Expecting 1" + gc[0].V, gc[0].V != 1);

        cleanup(gc);

        System.out.println("... Passed");


    }

    
    @Test
    public void TestBasic(){

        final int nqueen = 5;
        final Float[] weights = {0.2f,0.2f,0.2f,0.2f,0.2f}; 

        GradeCast[] gc = initGradeCast(nqueen, weights);

        System.out.println("Basic Test with 5 actors");
        // should be 0 (inconclusive weight followed by gc[0] queen)
        gc[0].Start(0);
        gc[1].Start(1);
        gc[2].Start(0);
        gc[3].Start(1);
        gc[4].Start(0);
        waitn(gc, nqueen);

        assertFalse("Expecting 0", gc[0].V != 0);
        cleanup(gc); 

        gc = initGradeCast(nqueen, weights);
        // should be 0 (consortium in iter 1 phase 1)
        gc[0].Start(1);
        gc[1].Start(0);
        gc[2].Start(0);
        gc[3].Start(0);
        gc[4].Start(0);

        waitn(gc, nqueen);
        assertFalse("Expecting 0", gc[0].V != 0);

        cleanup(gc); 

        gc = initGradeCast(nqueen, weights);
        // should be 1 (consortium in iter 1 phase 1)
        gc[0].Start(0);
        gc[1].Start(1);
        gc[2].Start(1);
        gc[3].Start(1);
        gc[4].Start(1);

        waitn(gc, nqueen);
        assertFalse("Expecting 1", gc[0].V != 1);

        cleanup(gc); 

        gc = initGradeCast(nqueen, weights);
        // should be 1 (inconclusive followed by queen value of 1)
        gc[0].Start(1);
        gc[1].Start(1);
        gc[2].Start(0);
        gc[3].Start(0);
        gc[4].Start(1);

        waitn(gc, nqueen);
        assertFalse("Expecting 1", gc[0].V != 1);

        System.out.println("... Passed");

        cleanup(gc);

    }

    @Test 
    public void TestKillBasic()
    {
        final int nqueen = 5;
        final Float[] weights = {0.2f,0.2f,0.2f,0.2f,0.2f}; 

        GradeCast[] gc = initGradeCast(nqueen, weights);

        System.out.println("Why Don't we kill Node 3");
        // should be 0 (inconclusive weight followed by gc[0] queen)
        gc[0].Start(0);
        gc[1].Start(1);
        gc[2].Start(0);
        gc[3].Start(1);
        gc[4].Start(0);
        
        General.wait_millis(4000);
        // Kill a process
        gc[3].Kill();

        waitn(gc, nqueen -1);

        //assertFalse("Expecting 0", gc[0].V != 0);
        cleanup(gc); 

        gc = initGradeCast(nqueen, weights);

        System.out.println("Off with her Head! Kill the queen");
        // should be 0 (inconclusive weight followed by gc[0] queen)
        gc[0].Start(0);
        gc[1].Start(1);
        gc[2].Start(0);
        gc[3].Start(1);
        gc[4].Start(0);
        
        General.wait_millis(4000);
        // Kill the queen
        gc[0].Kill();
        

        waitn(gc, nqueen -1);

        //assertFalse("Expecting 0", gc[0].V != 0);
        cleanup(gc); 

        System.out.println("... Passed");
    }

    @Test
    public void TestByzantineBasic()
    {
        final int nqueen = 5;
        final Float[] weights = {0.2f,0.2f,0.2f,0.2f,0.2f}; 

        Byzantine.ByzanType assignedType;
        int node;

        Random rand = new Random(2222); 

        System.out.println("Byzantine Test");

        for(int i = 0; i < 15; i++){
            GradeCast[] gc = initGradeCast(nqueen, weights);

            gc[0].Start(0);
            gc[1].Start(1);
            gc[2].Start(0);
            gc[3].Start(1);
            gc[4].Start(0);

            // wait for somewhere between 1 and 15 seconds
            General.wait_millis((rand.nextInt(15)+1)*1000);
            assignedType = Byzantine.get_random_byzantine_type(rand.nextInt(5));
            node = rand.nextInt(nqueen);
            gc[node].set_byzantine(assignedType);
            System.out.println("Set Node "+node+" to byzatine type "+assignedType);

            waitn(gc, nqueen -1);

            cleanup(gc); 
        }


    }
    /*
    @Test
    public void TestDeaf(){

        final int nqueen = 5;
        GradeCast[] gc = initGradeCast(nqueen);

        System.out.println("Test: Deaf proposer ...");
        gc[0].Start(0, "hello");
        waitn(gc, 0, nqueen);

        gc[1].ports[0]= 1;
        gc[1].ports[nqueen-1]= 1;
        gc[1].Start(1, "goodbye");
        waitmajority(gc, 1);
        try {
            Thread.sleep(1000);
        } catch (Exception e){
            e.printStackTrace();
        }
        int nd = ndecided(gc, 1);
        assertFalse("a deaf peer heard about a decision " + nd, nd != nqueen-2);

        gc[0].Start(1, "xxx");
        waitn(gc, 1, nqueen-1);
        try {
            Thread.sleep(1000);
        } catch (Exception e){
            e.printStackTrace();
        }
        nd = ndecided(gc, 1);
        assertFalse("a deaf peer heard about a decision " + nd, nd != nqueen-1);

        gc[nqueen-1].Start(1, "yyy");
        waitn(gc, 1, nqueen);
        System.out.println("... Passed");
        cleanup(gc);

    }

    @Test
    public void TestForget(){

        final int nqueen = 6;
        GradeCast[] gc = initGradeCast(nqueen);

        System.out.println("Test: Forgetting ...");

        for(int i = 0; i < nqueen; i++){
            int m = gc[i].Min();
            assertFalse("Wrong initial Min() " + m, m > 0);
        }

        gc[0].Start(0,"00");
        gc[1].Start(1,"11");
        gc[2].Start(2,"22");
        gc[0].Start(6,"66");
        gc[1].Start(7,"77");

        waitn(gc, 0, nqueen);
        for(int i = 0; i < nqueen; i++){
            int m = gc[i].Min();
            assertFalse("Wrong Min() " + m + "; expected 0", m != 0);
        }

        waitn(gc, 1, nqueen);
        for(int i = 0; i < nqueen; i++){
            int m = gc[i].Min();
            assertFalse("Wrong Min() " + m + "; expected 0", m != 0);
        }

        for(int i = 0; i < nqueen; i++){
            gc[i].Done(0);
        }

        for(int i = 1; i < nqueen; i++){
            gc[i].Done(1);
        }

        for(int i = 0; i < nqueen; i++){
            gc[i].Start(8+i, "xx");
        }

        boolean ok = false;
        for(int iters = 0; iters < 12; iters++){
            ok = true;
            for(int i = 0; i < nqueen; i++){
                int s = gc[i].Min();
                if(s != 1){
                    ok = false;
                }
            }
            if(ok) break;
            try {
                Thread.sleep(1000);
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        assertFalse("Min() did not advance after Done()", ok != true);
        System.out.println("... Passed");
        cleanup(gc);


    }

    */
}
