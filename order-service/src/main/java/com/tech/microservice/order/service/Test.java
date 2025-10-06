package com.tech.microservice.order.service;

public class Test {

    private static int v = 1;

    private static int doTest(int n) {
        v++;
        return n +=v;
    }

private static void check() {

    Object a = 1 + 2 + "a";
    String b = "a" + 1 + 2;

    System.out.println(a);
    System.out.println(b);

}


    public static void main(String[] args) {
        int res  = doTest(10);
        System.out.println(res);

        check();
    }
}



