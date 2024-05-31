module Demo
{
    class Response{
        long responseTime;
        string value;
    }
    interface Callback{
        void responseCallback(string r);
        int returnCallback(int value);
    }

     class Request{
        string hostname;
        string message;
    }
    
    interface Printer
    {   
        void register(string hostname, Callback* proxy);
        Response printString(string s);
        void broadcastMessage(string message, Callback* proxy);
        void sendMessage(string recipient, string message, Callback* proxy);
         void listClients(Callback* proxy);
    }
}