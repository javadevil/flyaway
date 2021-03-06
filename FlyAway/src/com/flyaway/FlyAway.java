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
import com.flyaway.iim.RemoveCAT;
import com.flyaway.iim.RemoveHeader;
import com.flyaway.iim.RemoveVersion;
import com.flyaway.iim.ReplaceVariable;
import com.flyaway.iim.TabIndex;
import com.flyaway.ui.SwingFace;
import com.flyaway.ui.WebFace;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;

/**
 * @version 1.0
 * @author wt
 */
public class FlyAway {
    
    //FlyAway Properties
    public static Properties properties = new Properties();
    
    public static void main(String[] args) throws Exception {
        setup();
        SwingFace.main(args);
        //System.getProperties().put("test", "test");
        //WebFace.main(args);
        //Path p = Paths.get(System.getProperty("user.home"),"iMacros","Macros","#Current.iim");
        //System.buffer.append(read(p));
    }
    public static String read(Path path) throws IOException{
        System.out.println("READ:"+path);
        
        String data = new String(Files.readAllBytes(path));
        if(data.startsWith("\uFEFF")){
            data = data.replace("\uFEFF", "");
        }
        return data;
    }
    
    public static void write(Path path,String data) throws IOException{
        System.out.print("WRITE:"+path);
        
        if(!data.startsWith("\uFEFF")){
            data = "\uFEFF" + data;
        }
        
        Files.write(path, data.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    private static void setup() throws IOException{
        File configFile = new File("config.properties");
        if(configFile.exists()){
            properties.load(new FileInputStream(configFile));
        } 
    }

    public static void createHeader() throws Exception {
        Path headerPath = Paths.get(properties.getProperty("imPath"),"Macros","Header.iim");
        Path downloadPath = Paths.get(properties.getProperty("imPath"),"Downloads");
        
        StringBuffer buffer = new StringBuffer();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(os);
        buffer.append("'HEADER'");
        buffer.append("SET !EXTRACT_TEST_POPUP NO");
        buffer.append("SET !ERRORIGNORE YES");
        buffer.append("SET !ERRORCONTINUE YES");
        buffer.append("SET !DATASOURCE dataset.csv");
        buffer.append("SET !DATASOURCE_COLUMNS 100");
        buffer.append("SET !DATASOURCE_LINE 2");
        buffer.append("'Deadth by Capcha.'");
        buffer.append("SET C_USER DBC_USER");
        buffer.append("SET C_PASS DBC_PASS");
        buffer.append("SET F_CAPCHA \"CAPCHA_{{!NOW:ddmmyy_hhnnss}}\"");
        buffer.append("SET D_CAPCHA \"").append(downloadPath).append(File.separator).append("\"");
        buffer.append("'/HEADER'");
        write(headerPath, buffer.toString());
    }
    public static void compile(String macroname) throws Exception {
        System.out.print("Compile:"+macroname);

        IIM cur = IIM.read(Paths.get(properties.getProperty("imPath"),"Macros",macroname));
        IIM hdr = IIM.read(Paths.get(properties.getProperty("imPath"),"Macros","Header.iim"));
        IIM dat = IIM.read(Paths.get(properties.getProperty("imPath"),"Datasources","dataset.csv"));

        cur.process(new RemoveVersion());
        cur.process(new AddHeader(hdr));
        cur.process(new ReplaceVariable(dat));
        cur.process(new AddCapcha());
        cur.process(new AddVersion());
        write(Paths.get(properties.getProperty("imPath"),"Macros","play.iim"),cur.getData());
        write(Paths.get(properties.getProperty("imPath"),"Macros",cur.getDescription()),cur.getData());
    }

    public static String mix(File[] paths) throws Exception {
        ByteArrayOutputStream report = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(report);
        Preferences prefs = Preferences.userNodeForPackage(FlyAway.class);
        
        Path macPath = Paths.get(prefs.get("macPath", null));
        IIM hdr = IIM.read(prefs.get("hdrPath", null));
        
        StringBuffer buffer = new StringBuffer();
        long start = System.nanoTime();
        
        for (int i = 0; i < paths.length; i++) {
            IIM data = IIM.read(paths[i].toPath());
            data.process(new RemoveVersion());
            data.process(new RemoveHeader());
            data.process(new TabIndex(i));
            data.process(new RemoveCAT());
            
            if( i > 0){
                buffer.append("TAB OPEN\r\n");
            }
            
            buffer.append(data.getData().replace(IIM.BOM, ""));
            buffer.append("\r\n");
        }
        IIM mixed = new IIM(buffer, "mix@"+buffer.hashCode());
        mixed.process(new AddHeader(hdr));
        mixed.process(new AddVersion());
        mixed.save(macPath.resolve(mixed.getDescription()+".iim"));
        mixed.save(macPath.resolve("play.iim"));
        
        buffer.append(macPath.resolve("play.iim"));
        buffer.append(macPath.resolve(mixed.getDescription() + ".iim"));
        buffer.append("Process Time:" + ((System.nanoTime() - start) / 1000000.0) + "ms");
        return report.toString("UTF-8");
    }
}
