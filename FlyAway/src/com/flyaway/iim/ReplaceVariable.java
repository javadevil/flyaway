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

package com.flyaway.iim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

/**
 *
 * @author wt
 */
public class ReplaceVariable implements Instruction{
    private final HashMap<String, String> dict;
    
    public ReplaceVariable(IIM csv) throws IOException{
        BufferedReader reader = new BufferedReader(new StringReader(csv.getData()));
        String line = reader.readLine();
        line = line.replace(IIM.BOM, "");
        String[] keys = line.split(",");
        
        dict = new HashMap<>();
        int idx = 1;
        for(String key : keys){
            dict.put(key, "{{!COL"+idx+"}}");
            idx++;
        }
    }

    @Override
    public String process(String data) {
        for(String key : dict.keySet()){
            data = data.replace(key, dict.get(key));
        }
        return data;
    }
}
