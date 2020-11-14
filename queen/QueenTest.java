package queen;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/**
 * This is a subset of entire test cases
 * For your reference only.
 */
public class QueenTest {

    private int ndecided(Queen[] qn){
        int counter = 0;
        Object v = null;
        Queen.retStatus ret;
        for(int i = 0; i < qn.length; i++){
            if(qn[i] != null){
                ret = qn[i].Status();
                if(ret.state == true) {
                    assertFalse("decided values do not match: i=" + i + " v=" + v + " v1=" + ret.v, counter > 0 && !v.equals(ret.v));
                    counter++;
                    v = ret.v;
                }

            }
        }
        return counter;
    }

    private void waitn(Queen[] qn, int wanted){
        int to = 10;
        for(int i = 0; i < 30; i++){
            if(ndecided(qn) >= wanted){
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

        int nd = ndecided(qn);
        assertFalse("too few decided; ndecided=" + nd + " wanted=" + wanted, nd < wanted);

    }

    private void waitmajority(Queen[] qn){
        waitn(qn, (qn.length/2) + 1);
    }

    private void cleanup(Queen[] qn){
        for(int i = 0; i < qn.length; i++){
            if(qn[i] != null){
                qn[i].Kill();
            }
        }
    }

    private Queen[] initQueen(int nqueen, Float[] weights){
        String host = "127.0.0.1";
        String[] peers = new String[nqueen];
        int[] ports = new int[nqueen];
        Queen[] qn = new Queen[nqueen];
        for(int i = 0 ; i < nqueen; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        for(int i = 0; i < nqueen; i++){
            qn[i] = new Queen(i, peers, ports, weights);
        }
        return qn;
    }

    @Test 
    public void TestSingleQueen()
    {
        final int nqueen = 1; 
        final Float[] weights = {1.0f}; 
        Queen[] qn = initQueen(nqueen, weights);

        System.out.println("Test: Single Queen ...");

        qn[0].Start(1);
        
        waitn(qn, 1); 
        assertFalse("Validity not satisfied, qn has value" + qn[0].V, qn[0].V != 1);
        cleanup(qn); 

        qn = initQueen(nqueen, weights);

        qn[0].Start(0);

        waitn(qn, 1); 
        assertFalse("Validity not satisfied, qn has value" + qn[0].V, qn[0].V != 0);

        cleanup(qn);
        System.out.println("... Passed");
    }

    
    @Test
    public void TestBasic(){

        final int nqueen = 5;
        final Float[] weights = {0.2f,0.2f,0.2f,0.2f,0.2f}; 

        Queen[] qn = initQueen(nqueen, weights);

        System.out.println("Basic Test with 5 actors");
        // should be 0 (inconclusive weight followed by qn[0] queen)
        qn[0].Start(0);
        qn[1].Start(1);
        qn[2].Start(0);
        qn[3].Start(1);
        qn[4].Start(0);
        waitn(qn, nqueen);

        assertFalse("Expecting 0", qn[0].V != 0);
        cleanup(qn); 

        qn = initQueen(nqueen, weights);
        // should be 0 (consortium in iter 1 phase 1)
        qn[0].Start(1);
        qn[1].Start(0);
        qn[2].Start(0);
        qn[3].Start(0);
        qn[4].Start(0);

        waitn(qn, nqueen);
        assertFalse("Expecting 0", qn[0].V != 0);

        cleanup(qn); 

        qn = initQueen(nqueen, weights);
        // should be 1 (consortium in iter 1 phase 1)
        qn[0].Start(0);
        qn[1].Start(1);
        qn[2].Start(1);
        qn[3].Start(1);
        qn[4].Start(1);

        waitn(qn, nqueen);
        assertFalse("Expecting 1", qn[0].V != 1);

        cleanup(qn); 

        qn = initQueen(nqueen, weights);
        // should be 1 (inconclusive followed by queen value of 1)
        qn[0].Start(1);
        qn[1].Start(1);
        qn[2].Start(0);
        qn[3].Start(0);
        qn[4].Start(1);

        waitn(qn, nqueen);
        assertFalse("Expecting 1", qn[0].V != 1);

        System.out.println("... Passed");

        /*
        System.out.println("Test: Many proposers, same value ...");
        for(int i = 0; i < nqueen; i++){
            qn[i].Start(1, 77);
        }
        waitn(qn, 1, nqueen);
        System.out.println("... Passed");

        System.out.println("Test: Many proposers, different values ...");
        qn[0].Start(2, 100);
        qn[1].Start(2, 101);
        qn[2].Start(2, 102);
        waitn(qn, 2, nqueen);
        System.out.println("... Passed");

        System.out.println("Test: Out-of-order instances ...");
        qn[0].Start(7, 700);
        try {
            Thread.sleep(10);
        } catch (Exception e){
            e.printStackTrace();
        }
        qn[0].Start(6, 600);
        qn[1].Start(5, 500);
        waitn(qn, 7, nqueen);
        qn[0].Start(4, 400);
        qn[1].Start(3, 300);
        waitn(qn, 6, nqueen);
        waitn(qn, 5, nqueen);
        waitn(qn, 4, nqueen);
        waitn(qn, 3, nqueen);
        System.out.println("... Passed");
        */
        cleanup(qn);

    }
    /*
    @Test
    public void TestDeaf(){

        final int nqueen = 5;
        Queen[] qn = initQueen(nqueen);

        System.out.println("Test: Deaf proposer ...");
        qn[0].Start(0, "hello");
        waitn(qn, 0, nqueen);

        qn[1].ports[0]= 1;
        qn[1].ports[nqueen-1]= 1;
        qn[1].Start(1, "goodbye");
        waitmajority(qn, 1);
        try {
            Thread.sleep(1000);
        } catch (Exception e){
            e.printStackTrace();
        }
        int nd = ndecided(qn, 1);
        assertFalse("a deaf peer heard about a decision " + nd, nd != nqueen-2);

        qn[0].Start(1, "xxx");
        waitn(qn, 1, nqueen-1);
        try {
            Thread.sleep(1000);
        } catch (Exception e){
            e.printStackTrace();
        }
        nd = ndecided(qn, 1);
        assertFalse("a deaf peer heard about a decision " + nd, nd != nqueen-1);

        qn[nqueen-1].Start(1, "yyy");
        waitn(qn, 1, nqueen);
        System.out.println("... Passed");
        cleanup(qn);

    }

    @Test
    public void TestForget(){

        final int nqueen = 6;
        Queen[] qn = initQueen(nqueen);

        System.out.println("Test: Forgetting ...");

        for(int i = 0; i < nqueen; i++){
            int m = qn[i].Min();
            assertFalse("Wrong initial Min() " + m, m > 0);
        }

        qn[0].Start(0,"00");
        qn[1].Start(1,"11");
        qn[2].Start(2,"22");
        qn[0].Start(6,"66");
        qn[1].Start(7,"77");

        waitn(qn, 0, nqueen);
        for(int i = 0; i < nqueen; i++){
            int m = qn[i].Min();
            assertFalse("Wrong Min() " + m + "; expected 0", m != 0);
        }

        waitn(qn, 1, nqueen);
        for(int i = 0; i < nqueen; i++){
            int m = qn[i].Min();
            assertFalse("Wrong Min() " + m + "; expected 0", m != 0);
        }

        for(int i = 0; i < nqueen; i++){
            qn[i].Done(0);
        }

        for(int i = 1; i < nqueen; i++){
            qn[i].Done(1);
        }

        for(int i = 0; i < nqueen; i++){
            qn[i].Start(8+i, "xx");
        }

        boolean ok = false;
        for(int iters = 0; iters < 12; iters++){
            ok = true;
            for(int i = 0; i < nqueen; i++){
                int s = qn[i].Min();
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
        cleanup(qn);


    }

    */
}
