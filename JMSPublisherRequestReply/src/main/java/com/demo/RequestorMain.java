package com.demo;

import com.demo.requestor.Requestor;

public class RequestorMain {
    public static void main(String[] args) throws Exception {
        Requestor requestor = new Requestor();
        requestor.setUp();
        requestor.run();
    }
}
