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

import com.flyaway.iim.IIM;
import com.flyaway.iim.Instruction;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wt
 */
public class Compiler implements Runnable {

    private final Path input;
    private final List<Path> output;
    private final List<Instruction> instructions;
    
    
    public Compiler(Path input,List<Path> output,List<Instruction> instructions){
        this.input = input;
        this.output = output;
        this.instructions = instructions;
    }
    @Override
    public void run() {
        try {
            IIM source = IIM.read(input);
            
            for(Instruction inst : instructions){
                source.process(inst);
            }
            
            for(Path path : output){
                source.save(path);
            }
        } catch (Exception ex) {
            System.err.println(ex);
            Logger.getLogger(Compiler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
