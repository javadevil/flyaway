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
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * @version 1.0
 * @author wt
 */
public class FlyAway {

    public static void main(String[] args) throws Exception {
        SwingFace.main(args);
        //WebFace.main(args);

    }

    public static String initialize() throws Exception {
        //Resource initializing.
        ByteArrayOutputStream report = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(report);
        Preferences pref = Preferences.userNodeForPackage(FlyAway.class);

        //Get system home directory
        String home = System.getProperty("user.home");
        //Check Macros path
        Path macPath = Paths.get(home, "iMacros", "Macros");
        pref.put("macPath", macPath.toString());
        out.println(macPath + " " + (Files.isReadable(macPath) ? "OK" : "FAIL"));
        //Check #Current.iim
        Path curPath = Paths.get(home, "iMacros", "Macros", "#Current.iim");
        pref.put("curPath", curPath.toString());
        out.println(curPath + " " + (Files.isReadable(curPath) ? "OK" : "FAIL"));

        //Chekc Header.iim
        Path hdrPath = Paths.get(home, "iMacros", "Macros", "Header.iim");
        pref.put("hdrPath", hdrPath.toString());
        out.println(hdrPath + " " + (Files.isReadable(hdrPath) ? "OK" : "FAIL"));

        //Check DataSources dataset.csv
        Path datPath = Paths.get(home, "iMacros", "Datasources", "dataset.csv");
        pref.put("datPath", datPath.toString());
        out.println(datPath + " " + (Files.isReadable(datPath) ? "OK" : "FAIL"));

        //Check Download directory
        Path dowPath = Paths.get(home, "iMacros", "Downloads");
        pref.put("dowPath", dowPath.toString() + System.getProperty("file.separator"));
        if (!Files.isDirectory(dowPath, LinkOption.NOFOLLOW_LINKS)) {
            Set<PosixFilePermission> prem = PosixFilePermissions.fromString("rwxrw----");
            Files.createDirectory(dowPath, PosixFilePermissions.asFileAttribute(prem));
        }
        out.println(dowPath + " " + (Files.isWritable(dowPath) ? "OK" : "FAIL"));

        return report.toString("UTF-8");
    }

    public static void createHeader() throws Exception {
        Preferences pref = Preferences.userNodeForPackage(FlyAway.class);
        Path hdrPath = Paths.get(pref.get("hdrPath", null));
        if (Files.isWritable(hdrPath)) {
            Desktop.getDesktop().open(hdrPath.toFile());
        } else {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(os);
            out.print(IIM.BOM);
            out.println("'HEADER'");
            out.println("SET !EXTRACT_TEST_POPUP NO");
            out.println("SET !ERRORIGNORE YES");
            out.println("SET !ERRORCONTINUE YES");
            out.println("SET !DATASOURCE dataset.csv");
            out.println("SET !DATASOURCE_COLUMNS 100");
            out.println("SET !DATASOURCE_LINE 2");
            out.println("'Deadth by Capcha.'");
            out.println("SET C_USER DBC_USER");
            out.println("SET C_PASS DBC_PASS");
            out.println("SET F_CAPCHA \"CAPCHA_{{!NOW:ddmmyy_hhnnss}}\"");
            out.println("SET D_CAPCHA \"" + pref.get("dowPath", null) + "\"");
            out.println("'/HEADER'");
            Files.write(hdrPath, os.toByteArray(), StandardOpenOption.CREATE);
            createHeader();
        }
    }

    public static String compile(String macroname) throws Exception {
        ByteArrayOutputStream report = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(report);
        Preferences prefs = Preferences.userNodeForPackage(FlyAway.class);

        Path macPath = Paths.get(prefs.get("macPath", null));

        long start = System.nanoTime();
        IIM cur = IIM.read(prefs.get("curPath", null));
        IIM hdr = IIM.read(prefs.get("hdrPath", null));
        IIM dat = IIM.read(prefs.get("datPath", null));

        cur.process(new RemoveVersion());
        cur.process(new AddHeader(hdr));
        cur.process(new ReplaceVariable(dat));
        cur.process(new AddCapcha());
        cur.process(new AddVersion());
        cur.save(macPath.resolve("play.iim"));
        cur.save(macPath.resolve(cur.getDescription() + ".iim"));

        out.println(macPath.resolve("play.iim"));
        out.println(macPath.resolve(cur.getDescription() + ".iim"));
        out.println("Process Time:" + ((System.nanoTime() - start) / 1000000.0) + "ms");
        return report.toString("UTF-8");
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
        
        out.println(macPath.resolve("play.iim"));
        out.println(macPath.resolve(mixed.getDescription() + ".iim"));
        out.println("Process Time:" + ((System.nanoTime() - start) / 1000000.0) + "ms");
        return report.toString("UTF-8");
    }
}
