package org.pact.example;

import org.pact.consumer.ConsumerPact;


public class SimpleConsumer {
    private int input;
    SimpleConsumer(int input){
        this.input=input;
    }
    public void request()
    {
        SimpleRequest request = new SimpleRequest(input);
        request.add_step(s->{
            try{
                Thread.sleep(1000);
            }catch(Exception e){
                e.printStackTrace();
            }
            System.out.println("+1");
            return (Integer)s+1;
        });

        request.add_step(s->{
            try{
                Thread.sleep(1000);
            }catch(Exception e){
                e.printStackTrace();
            }
            System.out.println("+1000");
            return (Integer)s+1000;
        });

        ConsumerPact pact = request.submit();

        try{
            Object result = pact.wait(0);
            System.out.print("now we have promise 0 fulfilled with value "+(Integer)result);
        }catch(Exception e){
            System.err.print(e.toString());
            e.printStackTrace();
        }

        try{
            Object result = pact.wait(1);
            System.out.print("now we have promise 1 fulfilled with value "+(Integer)result);
        }catch(Exception e){
            System.err.print(e.toString());
            e.printStackTrace();
        }

        try{
            Object result = pact.wait(2);
            System.out.print("now we have promise 2 fulfilled with value "+(Integer)result);
        }catch(Exception e){
            System.err.print(e.toString());
            e.printStackTrace();
        }
    }

    public static void main(String args[])
    {
        SimpleProvider.instance().start();
        new SimpleConsumer(123).request();
        //new SimpleConsumer(66).request();
        while(true){}
    }


}
