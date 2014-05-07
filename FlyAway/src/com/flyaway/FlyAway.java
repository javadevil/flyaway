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

package com.flyaway;

import com.flyaway.iim.AddCapcha;
import com.flyaway.iim.AddHeader;
import com.flyaway.iim.AddVersion;
import com.flyaway.iim.IIM;
import com.flyaway.iim.RemoveHeader;
import com.flyaway.iim.RemoveVersion;
import com.flyaway.iim.ReplaceVariable;
import com.flyaway.ui.FlyAwayMain;
import com.flyaway.ui.WebFace;
import java.util.prefs.Preferences;

/**
 * @version 1.0
 * @author wt
 */
public class FlyAway {
    
    public static void main(String[] args) throws Exception{
        FlyAwayMain.main(args);
        //WebFace.main(args);
    }
    
    public static void compile(String macroname) throws Exception{
        Preferences prefs = Preferences.userNodeForPackage(FlyAway.class);
        String path = prefs.get("imacros_path", null);
        IIM current = IIM.read(path+"/Macros/"+macroname+".iim");
        IIM header = IIM.read(path+"/Macros/Header.iim");
        IIM dataset = IIM.read(path+"/Datasources/dataset.csv");
        dataset.save(path+"/Datasources/dataset.csv");
        current.process(new RemoveVersion());
        current.process(new AddHeader(header));
        current.process(new ReplaceVariable(dataset));
        current.process(new AddCapcha());
        current.process(new AddVersion());
        System.out.println(current.getData());
        current.save(path+"/Macros/play.iim");
        current.save(path+"/Macros/"+current.getDescription()+".iim");
    }
    
    public static void mix(String[] paths) throws Exception{
        Preferences prefs = Preferences.userNodeForPackage(FlyAway.class);
        String path = prefs.get("imacros_path", null);
        IIM header = IIM.read(path+"/Macros/Header.iim");
        for(String p:paths){
            IIM data = IIM.read(path+p);
            data.process(new RemoveVersion());
            data.process(new RemoveHeader(header));
            System.out.println(data);
        }
    }
}
