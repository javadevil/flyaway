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

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * @author wt
 */
public class IIM {
    
    public static final String BOM = "\uFEFF"; 
    
    private String data;
    private String description;
    
    protected IIM(){
    }
    
    public void process(Instruction inst){
        this.data = inst.process(this.data);
    }
    
    public String getDescription() {
        return description;
    }

    public String getData() {
        return data;
    }
    
    public void save(Path path) throws Exception{
        //Check UTF-8 BOM
        if(!checkBOM(this)){
            //add FEFF if doesn't exist.
            data = "\uFEFF" + data;
        }
        Files.write(path, data.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);
    }
    public void save(String path) throws Exception{
        this.save(Paths.get(path));
    }
    
    public static IIM read(Path path) throws Exception{
        IIM obj = new IIM();
        obj.data = new String(Files.readAllBytes(path),"UTF-8");
        
        //Set description by first goto url
        Matcher matcher = Pattern.compile("(https?|ftp|file)://.*/").matcher(obj.data);
        if(matcher.find()){
            obj.description = matcher.group().split("(https?|ftp|file)://")[1].replace("/", "");
        }
        return obj;
    }
    public static IIM read(String path) throws Exception{
        return read(Paths.get(path));
    }
    
    public static boolean checkBOM(IIM data){
        return data.data.startsWith(BOM);
    }
    @Override
    public String toString() {
        return data;
    }
    
    
}
