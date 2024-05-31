import Demo.Callback;

public class CallbackImp implements Callback {

    @Override
    public void responseCallback(String r, com.zeroc.Ice.Current current) {
        System.out.println("El servidor responde: "+r);
    }

    @Override
    public int returnCallback(int value, com.zeroc.Ice.Current current) {
        return value;
    }
    
}
