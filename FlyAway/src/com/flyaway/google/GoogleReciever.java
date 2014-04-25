/*
 * The MIT License
 *
 * Copyright 2014 wt.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flyaway.google;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import javax.net.ssl.SSLContext;

/**
 *
 * @author wt
 */
public class GoogleReciever extends Thread {

    private static final int PORT = 1788;
    private final HttpServer reciever;
    private String data = null;
    public GoogleReciever() throws Exception {
        this.reciever = HttpServer.create(new InetSocketAddress(PORT), 0);

        this.reciever.createContext("/", (HttpExchange he) -> {
            //get Query String from URI
            String query = he.getRequestURI().getQuery();
            
            //Extract by google specification
            data = query.split("=")[1];
            
            he.sendResponseHeaders(200, data.length());
            he.getResponseBody().write(data.getBytes(Charset.forName("UTF-8")));
            
            synchronized(reciever){
                reciever.notify();
            }
        });
    }

    @Override
    public void run() {
        this.reciever.start();
    }
    
    public String recieve() throws Exception{
        synchronized(reciever){
            reciever.wait();
            reciever.stop(0);
        }
        return data;
    }

}
