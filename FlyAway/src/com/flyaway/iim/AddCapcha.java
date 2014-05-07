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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author wt
 */
public class AddCapcha implements Instruction{
    private final String CAPCHA = "SAVE_ELEMENT_SCREENSHOT\n"
            + "TAB OPEN\n"
            + "TAB T=2\n"
            + "SET !EXTRACT NULL\n"
            + "URL GOTO=http://api.dbcapi.me/decaptcher?function=picture2&print_format=html\n"
            + "TAG POS=1 TYPE=INPUT ATTR=NAME:username CONTENT={{C_USER}}\n"
            + "TAG POS=1 TYPE=INPUT ATTR=NAME:password CONTENT={{C_PASS}}\n"
            + "TAG POS=1 TYPE=INPUT ATTR=NAME:pict CONTENT=\"{{D_CAPCHA}}{{F_CAPCHA}}.png\"\n"
            + "TAG POS=1 TYPE=INPUT ATTR=TYPE:submit\n"
            + "TAG POS=6 TYPE=TD ATTR=* EXTRACT=TXT\n"
            + "SET CAPCHA {{!EXTRACT}}\n"
            + "TAB CLOSE\n";
    
    @Override
    public String process(String data) {
        String url = "";
        String fix = "";
        
        Pattern pattern = Pattern.compile("TAG.*SAVEPICTUREAS");
        Matcher matcher = pattern.matcher(data);
        while(matcher.find()){
            url = matcher.group();
        }
        
        pattern = Pattern.compile("http[s]?://([a-zA-Z]|[0-9]|[$-_@.&+])*");
        matcher = pattern.matcher(url);
        while(matcher.find()){
            url = matcher.group();
        }
        
        pattern = Pattern.compile("http[s]?://([a-zA-Z]|[0-9]|[$-_@.&+]).*/");
        matcher = pattern.matcher(url);
        while(matcher.find()){
            fix = matcher.group();
        }
        if(!url.equals("")){
            data = data.replaceFirst(url, fix+"*");
        }
        
        data = data.replaceAll("ONDOWNLOAD.*", "ONDOWNLOAD FOLDER={{D_CAPCHA}} FILE={{F_CAPCHA}} WAIT=YES");
        data = data.replace("SAVEPICTUREAS\n", CAPCHA);
        data = data.replace("[cc]", "{{CAPCHA}}");
        return data;
    }
    
}
