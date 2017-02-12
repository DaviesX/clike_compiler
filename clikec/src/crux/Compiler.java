/*
 * Copyright (C) 2017 davis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package crux;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Main class.
 * @author davis
 */
public class Compiler {

        public static void run_lexical_test(String[] args) throws Exception {
                String src = args[0];
                IScanner s = null;

                try {
                        s = new Scanner(new BufferedReader(new FileReader(src)));
                } catch (IOException e) {
                        System.err.println("Error accessing the source file: \"" + src + "\"");
                        System.exit(-2);
                }

                Token t = s.scan_next();
                while (!t.is_eof()) {
                        System.out.println(t);
                        t = s.scan_next();
                }
                System.out.println(t);
        }
        
        public static void run_syntactical_test(String[] args) throws Exception {
                String src = args[0];
                IScanner s = null;

                try {
                        s = new Scanner(new BufferedReader(new FileReader(src)));
                } catch (IOException e) {
                        System.err.println("Error accessing the source file: \"" + src + "\"");
                        System.exit(-2);
                }
                
                IParser p = new ParserRecursiveDescent();
                AST ast = p.parse(s);
                System.out.println(ast.toString());
        }
        
        public static void run_semantics_test(String[] args) throws Exception {
                String src = args[0];
                IScanner s = null;

                try {
                        s = new Scanner(new BufferedReader(new FileReader(src)));
                } catch (IOException e) {
                        System.err.println("Error accessing the source file: \"" + src + "\"");
                        System.exit(-2);
                }
                
                IParser p = new ParserRecursiveDescent();
                AST ast = p.parse(s);
                
                ISemanticsAnalyzer sa = new SemanticsAnalyzer();
                try {
                        sa.analyze(ast);
                } catch (ErrorReport err) {
                        System.out.println(err.toString());
                }
                System.out.println("Crux program successfully parsed.");
        }

        /**
         * @param args the command line arguments
         * @throws java.lang.Exception
         */
        public static void main(String[] args) throws Exception {
                //run_lexical_test(args);
                //run_syntactical_test(args);
                run_semantics_test(args);
        }

}
