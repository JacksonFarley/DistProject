package general;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface MessageRMI extends Remote{
    void Receive(Message msg) throws RemoteException;
    //void Advance(Messgae msg) throws RemoteException;
}
