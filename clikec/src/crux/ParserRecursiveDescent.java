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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Perform syntactic analysis on a token stream using recursive descent (LL(1)
 * grammar).
 *
 * @author davis
 */
public class ParserRecursiveDescent implements IParser {

        private Token m_curr_tok;
        private IScanner m_scanner;
        private StringBuilder m_err_buf;
        
        private class QuitParseException extends RuntimeException {

                private static final long serialVersionUID = 1L;

                public QuitParseException(String err) {
                        super(err);
                }
        }

        private String report_syntax_error(NonTerminal.Type nt) {
                String message = "SyntaxError(" + m_curr_tok.file_pointer().line_no() + ","
                                 + m_curr_tok.file_pointer().column()
                                 + ")[Expected a token from " + nt.toString()
                                 + " but got " + m_curr_tok.type() + ".]";
                m_err_buf.append(message).append("\n");
                return message;
        }

        private String report_syntax_error(Token.Lexeme type) {
                String message = "SyntaxError(" + m_curr_tok.file_pointer().line_no() + ","
                                 + m_curr_tok.file_pointer().column()
                                 + ")[Expected " + type + " but got " + m_curr_tok.type() + ".]";
                m_err_buf.append(message).append("\n");
                return message;
        }

        private boolean have(Token.Lexeme tok) {
                return m_curr_tok.type() == tok;
        }

        private boolean have(Set<Token.Lexeme> toks) {
                return toks.contains(m_curr_tok.type());
        }

        private boolean accept(Token.Lexeme tok) throws IOException {
                if (have(tok)) {
                        m_curr_tok = m_scanner.scan_next();
                        return true;
                }
                return false;
        }

        private boolean accept(Set<Token.Lexeme> toks) throws IOException {
                if (have(toks)) {
                        m_curr_tok = m_scanner.scan_next();
                        return true;
                }
                return false;
        }

        private boolean expect(Token.Lexeme tok) throws IOException {
                if (accept(tok)) {
                        return true;
                }
                throw new QuitParseException(report_syntax_error(tok));
                //return false;
        }

        private boolean expect(NonTerminal.Type nt,
                               Set<Token.Lexeme> toks) throws IOException {
                if (accept(toks)) {
                        return true;
                }
                throw new QuitParseException(report_syntax_error(nt));
                //return false;
        }

        // literal := INTEGER | FLOAT | TRUE | FALSE .
        private void literal(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(NonTerminal.Type.LITERAL, new HashSet() {{
                        add(Token.Lexeme.INTEGER);
                        add(Token.Lexeme.FLOAT);
                        add(Token.Lexeme.TRUE);
                        add(Token.Lexeme.FALSE);
                }});
        }
        
        // op0 := ">=" | "<=" | "!=" | "==" | ">" | "<" 
        private void op0(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(NonTerminal.Type.OP0, new HashSet() {{
                        add(Token.Lexeme.GREATER_EQUAL);
                        add(Token.Lexeme.LESSER_EQUAL);
                        add(Token.Lexeme.NOT_EQUAL);
                        add(Token.Lexeme.EQUAL);
                        add(Token.Lexeme.LESS_THAN);
                        add(Token.Lexeme.GREATER_THAN);
                }});
        }
        
        // op1 := "+" | "-" | "or" .
        private void op1(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(NonTerminal.Type.OP1, new HashSet() {{
                        add(Token.Lexeme.ADD);
                        add(Token.Lexeme.SUB);
                        add(Token.Lexeme.OR);
                }});
        }
        
        // op2 := "*" | "/" | "and" .
        private void op2(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(NonTerminal.Type.OP1, new HashSet() {{
                        add(Token.Lexeme.MUL);
                        add(Token.Lexeme.DIV);
                        add(Token.Lexeme.AND);
                }});
        }
        
        // expression0 := expression1 [ op0 expression1 ] .
        private void expression0(GeneralNode node) throws IOException {
                expression1(node.add_child(0, new NonTerminal(NonTerminal.Type.EXPRESSION1)));
                
                if (have(new HashSet() {{
                        add(Token.Lexeme.GREATER_EQUAL);
                        add(Token.Lexeme.LESSER_EQUAL);
                        add(Token.Lexeme.NOT_EQUAL);
                        add(Token.Lexeme.EQUAL);
                        add(Token.Lexeme.LESS_THAN);
                        add(Token.Lexeme.GREATER_THAN);
                }})) {
                        op0(node.add_child(1, new NonTerminal(NonTerminal.Type.OP0)));
                        expression1(node.add_child(2, new NonTerminal(NonTerminal.Type.EXPRESSION1)));
                }
        }
        
        // expression1 := expression2 { op1 expression2 } .
        private void expression1(GeneralNode node) throws IOException {
                expression2(node.add_child(0, new NonTerminal(NonTerminal.Type.EXPRESSION2)));
                
                int i_child = 1;
                while (have(new HashSet<Token.Lexeme>() {{
                        add(Token.Lexeme.ADD);
                        add(Token.Lexeme.SUB);
                        add(Token.Lexeme.OR);
                }})) {
                        op1(node.add_child(i_child, new NonTerminal(NonTerminal.Type.OP1)));
                        expression2(node.add_child(i_child + 1, new NonTerminal(NonTerminal.Type.EXPRESSION2)));
                        i_child += 2;
                }
        }
        
        // expression2 := expression3 { op2 expression3 } .
        private void expression2(GeneralNode node) throws IOException {
                expression3(node.add_child(0, new NonTerminal(NonTerminal.Type.EXPRESSION3)));
                
                int i_child = 1;
                while (have(new HashSet<Token.Lexeme>() {{
                        add(Token.Lexeme.MUL);
                        add(Token.Lexeme.DIV);
                        add(Token.Lexeme.AND);
                }})) {
                        op2(node.add_child(i_child, new NonTerminal(NonTerminal.Type.OP2)));
                        expression3(node.add_child(i_child + 1, new NonTerminal(NonTerminal.Type.EXPRESSION3)));
                        i_child += 2;
                }
        }
        
        // expression3 := "not" expression3
        //      | "(" expression0 ")"
        //      | designator
        //      | call-expression
        //      | literal .
        private void expression3(GeneralNode node) throws IOException {
                switch (m_curr_tok.type()) {
                        case NOT:
                                node.add_child(0, m_curr_tok);
                                expect(Token.Lexeme.NOT);
                                expression3(node.add_child(1, new NonTerminal(NonTerminal.Type.EXPRESSION3)));
                                break;
                        case OPEN_PAREN:
                                node.add_child(0, m_curr_tok);
                                expect(Token.Lexeme.OPEN_PAREN);
                                
                                expression0(node.add_child(1, new NonTerminal(NonTerminal.Type.EXPRESSION0)));
                                
                                node.add_child(2, m_curr_tok);
                                expect(Token.Lexeme.CLOSE_PAREN);
                                break;
                        case IDENTIFIER:
                                designator(node.add_child(0, new NonTerminal(NonTerminal.Type.DESIGNATOR)));
                                break;
                        case CALL:
                                call_expression(node.add_child(0, new NonTerminal(NonTerminal.Type.CALL_EXPRESSION)));
                                break;
                        case INTEGER:
                        case FLOAT:
                        case TRUE:
                        case FALSE:
                                literal(node.add_child(0, new NonTerminal(NonTerminal.Type.LITERAL)));
                                break;
                        default:
                                expect(NonTerminal.Type.EXPRESSION3,
                                       new HashSet<Token.Lexeme>() {{
                                               add(Token.Lexeme.NOT);
                                               add(Token.Lexeme.OPEN_PAREN);
                                               add(Token.Lexeme.IDENTIFIER);
                                               add(Token.Lexeme.CALL);
                                               add(Token.Lexeme.INTEGER);
                                               add(Token.Lexeme.FLOAT);
                                               add(Token.Lexeme.TRUE);
                                               add(Token.Lexeme.FALSE);
                                       }});
                                break;
                }
        }
        
        // expression-list := [ expression0 { "," expression0 } ] .
        private void expression_list(GeneralNode node) throws IOException {
                if (have(new HashSet<Token.Lexeme>() {{
                        add(Token.Lexeme.INTEGER);
                        add(Token.Lexeme.FLOAT);
                        add(Token.Lexeme.TRUE);
                        add(Token.Lexeme.FALSE);
                        
                        add(Token.Lexeme.NOT);
                        add(Token.Lexeme.OPEN_PAREN);
                        add(Token.Lexeme.IDENTIFIER);
                        add(Token.Lexeme.CALL);
                }})) {
                        expression0(node.add_child(0, new NonTerminal(NonTerminal.Type.EXPRESSION0)));
                        int i_child = 1;
                        while (have(Token.Lexeme.COMMA)) {
                                node.add_child(i_child, m_curr_tok);
                                expect(Token.Lexeme.COMMA);
                                expression0(node.add_child(i_child + 1, 
                                                           new NonTerminal(NonTerminal.Type.EXPRESSION0)));
                                i_child += 2;
                        }
                }
        }
        
        // call-expression := "::" IDENTIFIER "(" expression-list ")" .
        private void call_expression(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(Token.Lexeme.CALL);
                
                node.add_child(1, m_curr_tok);
                expect(Token.Lexeme.IDENTIFIER);
                
                node.add_child(2, m_curr_tok);
                expect(Token.Lexeme.OPEN_PAREN);
                
                expression_list(node.add_child(3, new NonTerminal(NonTerminal.Type.EXPRESSION_LIST)));
                
                node.add_child(4, m_curr_tok);
                expect(Token.Lexeme.CLOSE_PAREN);
        }


        // designator := IDENTIFIER { "[" expression0 "]" } .
        private void designator(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(Token.Lexeme.IDENTIFIER);
                
                int i_child = 1;
                while (have(Token.Lexeme.OPEN_BRACKET)) {
                        node.add_child(i_child, m_curr_tok);
                        expect(Token.Lexeme.OPEN_BRACKET);
                        
                        expression0(node.add_child(
                                i_child + 1, new NonTerminal(NonTerminal.Type.EXPRESSION0)));
                        
                        node.add_child(i_child + 2, m_curr_tok);
                        expect(Token.Lexeme.CLOSE_BRACKET);
                        i_child += 3;
                }
        }
        
        // type := IDENTIFIER .
        private void type(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(Token.Lexeme.IDENTIFIER);
        }
        
        // variable-declaration := "var" IDENTIFIER ":" type ";" .
        private void variable_declaration(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(Token.Lexeme.VAR);
                
                node.add_child(1, m_curr_tok);
                expect(Token.Lexeme.IDENTIFIER);
                
                node.add_child(2, m_curr_tok);
                expect(Token.Lexeme.COLON);
                
                type(node.add_child(3, new NonTerminal(NonTerminal.Type.TYPE)));
                
                node.add_child(4, m_curr_tok);
                expect(Token.Lexeme.SEMICOLON);
        }
        
        // array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";" .
        private void array_declaration(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(Token.Lexeme.ARRAY);
                
                node.add_child(1, m_curr_tok);
                expect(Token.Lexeme.IDENTIFIER);
                
                node.add_child(2, m_curr_tok);
                expect(Token.Lexeme.COLON);
                
                type(node.add_child(3, new NonTerminal(NonTerminal.Type.TYPE)));
                
                int i_child = 4;
                
                do {
                        node.add_child(i_child, m_curr_tok);
                        expect(Token.Lexeme.OPEN_BRACKET);

                        node.add_child(i_child + 1, m_curr_tok);
                        expect(Token.Lexeme.INTEGER);

                        node.add_child(i_child + 2, m_curr_tok);
                        expect(Token.Lexeme.CLOSE_BRACKET);
                        
                        i_child += 3;
                } while (!have(Token.Lexeme.SEMICOLON));
                
                node.add_child(i_child, m_curr_tok);
                expect(Token.Lexeme.SEMICOLON);
        }
        
        // parameter := IDENTIFIER ":" type .
        private void parameter(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(Token.Lexeme.IDENTIFIER);
                
                node.add_child(1, m_curr_tok);
                expect(Token.Lexeme.COLON);
                
                type(node.add_child(2, new NonTerminal(NonTerminal.Type.TYPE)));
        }
        
        // parameter-list := [ parameter { "," parameter } ] .
        private void parameter_list(GeneralNode node) throws IOException {
                if (have(Token.Lexeme.IDENTIFIER)) {
                        parameter(node.add_child(0, new NonTerminal(NonTerminal.Type.PARAMETER)));
                        int i_child = 1;
                        while (have(Token.Lexeme.COMMA)) {
                                node.add_child(i_child, m_curr_tok);
                                expect(Token.Lexeme.COMMA);
                                
                                parameter(node.add_child(i_child + 1, new NonTerminal(NonTerminal.Type.PARAMETER)));
                                i_child += 2;
                        }
                }
        }
        
        // assignment-statement := "let" designator "=" expression0 ";" .
        private void assignment_statement(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(Token.Lexeme.LET);
                
                designator(node.add_child(1, new NonTerminal(NonTerminal.Type.DESIGNATOR)));
                
                node.add_child(2, m_curr_tok);
                expect(Token.Lexeme.ASSIGN);
                
                expression0(node.add_child(3, new NonTerminal(NonTerminal.Type.EXPRESSION0)));
                
                node.add_child(4, m_curr_tok);
                expect(Token.Lexeme.SEMICOLON);
        }
        
        // call-statement := call-expression ";" .
        private void call_statement(GeneralNode node) throws IOException {
                call_expression(node.add_child(0, new NonTerminal(NonTerminal.Type.CALL_EXPRESSION)));
                node.add_child(1, m_curr_tok);
                expect(Token.Lexeme.SEMICOLON);
        }
        
        // if-statement := "if" expression0 statement-block [ "else" statement-block ] .
        private void if_statement(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(Token.Lexeme.IF);
                
                expression0(node.add_child(1, new NonTerminal(NonTerminal.Type.EXPRESSION0)));
                statement_block(node.add_child(2, new NonTerminal(NonTerminal.Type.STATEMENT_BLOCK)));
                
                if (have(Token.Lexeme.ELSE)) {
                        node.add_child(3, m_curr_tok);
                        expect(Token.Lexeme.ELSE);
                        
                        statement_block(node.add_child(4, new NonTerminal(NonTerminal.Type.STATEMENT_BLOCK)));
                }
        }
        
        // while-statement := "while" expression0 statement-block .
        private void while_statement(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(Token.Lexeme.WHILE);
                
                expression0(node.add_child(1, new NonTerminal(NonTerminal.Type.EXPRESSION0)));
                statement_block(node.add_child(2, new NonTerminal(NonTerminal.Type.STATEMENT_BLOCK)));
        }
        
        // return-statement := "return" expression0 ";" .
        private void return_statement(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(Token.Lexeme.RETURN);
                
                expression0(node.add_child(1, new NonTerminal(NonTerminal.Type.EXPRESSION0)));
                
                node.add_child(2, m_curr_tok);
                expect(Token.Lexeme.SEMICOLON);
        }
        
        // statement := variable-declaration
        //      | call-statement
        //      | assignment-statement
        //      | if-statement
        //      | while-statement
        //      | return-statement .
        private void statement(GeneralNode node) throws IOException {
                switch (m_curr_tok.type()) {
                        case VAR:
                                variable_declaration(node.add_child(0, new NonTerminal(
                                        NonTerminal.Type.VARIABLE_DECLARATION)));
                                break;
                        case CALL:
                                call_statement(node.add_child(0, new NonTerminal(
                                        NonTerminal.Type.CALL_STATEMENT)));
                                break;
                        case LET:
                                assignment_statement(node.add_child(0, new NonTerminal(
                                        NonTerminal.Type.ASSIGNMENT_STATEMENT)));
                                break;
                        case IF:
                                if_statement(node.add_child(0, new NonTerminal(
                                        NonTerminal.Type.IF_STATEMENT)));
                                break;
                        case WHILE:
                                while_statement(node.add_child(0, new NonTerminal(
                                        NonTerminal.Type.WHILE_STATEMENT)));
                                break;
                        case RETURN:
                                return_statement(node.add_child(0, new NonTerminal(
                                        NonTerminal.Type.RETURN_STATEMENT)));
                                break;
                        default:
                                expect(NonTerminal.Type.STATEMENT, new HashSet<Token.Lexeme>() {{
                                        add(Token.Lexeme.VAR);
                                        add(Token.Lexeme.CALL);
                                        add(Token.Lexeme.LET);
                                        add(Token.Lexeme.IF);
                                        add(Token.Lexeme.WHILE);
                                        add(Token.Lexeme.RETURN);
                                }});
                                break;
                }
        }
        
        // statement-list := { statement } .
        private void statement_list(GeneralNode node) throws IOException {
                int i_child = 0;
                while (have(new HashSet<Token.Lexeme>() {{
                        add(Token.Lexeme.VAR);
                        add(Token.Lexeme.CALL);
                        add(Token.Lexeme.LET);
                        add(Token.Lexeme.IF);
                        add(Token.Lexeme.WHILE);
                        add(Token.Lexeme.RETURN);
                }})) {
                        statement(node.add_child(i_child, new NonTerminal(NonTerminal.Type.STATEMENT)));
                        i_child ++;
                }
        }
        
        // statement-block := "{" statement-list "}" .
        private void statement_block(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(Token.Lexeme.OPEN_BRACE);
                if (have(new HashSet<Token.Lexeme>() {{
                        add(Token.Lexeme.VAR);
                        add(Token.Lexeme.CALL);
                        add(Token.Lexeme.LET);
                        add(Token.Lexeme.IF);
                        add(Token.Lexeme.WHILE);
                        add(Token.Lexeme.RETURN);
                }})) {
                        statement_list(node.add_child(1, new NonTerminal(NonTerminal.Type.STATEMENT_LIST)));
                        node.add_child(2, m_curr_tok);
                        expect(Token.Lexeme.CLOSE_BRACE);
                } else {
                        node.add_child(1, m_curr_tok);
                        expect(Token.Lexeme.CLOSE_BRACE);
                }
        }
        
        // function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block .
        private void function_definition(GeneralNode node) throws IOException {
                node.add_child(0, m_curr_tok);
                expect(Token.Lexeme.FUNC);
                
                node.add_child(1, m_curr_tok);
                expect(Token.Lexeme.IDENTIFIER);
                
                node.add_child(2, m_curr_tok);
                expect(Token.Lexeme.OPEN_PAREN);
                
                parameter_list(node.add_child(3, new NonTerminal(NonTerminal.Type.PARAMETER_LIST)));
                
                node.add_child(4, m_curr_tok);
                expect(Token.Lexeme.CLOSE_PAREN);
                
                node.add_child(5, m_curr_tok);
                expect(Token.Lexeme.COLON);
                
                type(node.add_child(6, new NonTerminal(NonTerminal.Type.TYPE)));
                
                statement_block(node.add_child(7, new NonTerminal(NonTerminal.Type.STATEMENT_BLOCK)));
        }
        
        // declaration := variable-declaration | array-declaration | function-definition .
        private void declaration(GeneralNode node) throws IOException {                
                switch (m_curr_tok.type()) {
                        case VAR:
                                variable_declaration(node.add_child(0, new NonTerminal(
                                        NonTerminal.Type.VARIABLE_DECLARATION)));
                                break;
                        case FUNC:
                                function_definition(node.add_child(0, new NonTerminal(
                                        NonTerminal.Type.FUNCTION_DEFINITION)));
                                break;
                        case ARRAY:
                                array_declaration(node.add_child(0, new NonTerminal(
                                        NonTerminal.Type.ARRAY_DECLARATION)));
                                break;
                        default:
                                expect(NonTerminal.Type.DECLARATION, new HashSet<Token.Lexeme>() {{
                                        add(Token.Lexeme.VAR);
                                        add(Token.Lexeme.FUNC);
                                        add(Token.Lexeme.ARRAY);
                                }});
                                break;
                }
        }
        
        // declaration-list := { declaration } .
        private void declaration_list(GeneralNode node) throws IOException {
                int i_child = 0;
                while (have(new HashSet<Token.Lexeme>() {{
                        add(Token.Lexeme.VAR);
                        add(Token.Lexeme.FUNC);
                        add(Token.Lexeme.ARRAY);
                }})) {
                        declaration(node.add_child(i_child, new NonTerminal(NonTerminal.Type.DECLARATION)));
                        i_child ++;
                }
        }

        // program := declaration-list EOF .
        private void program(ParseTree tree) throws IOException {
                GeneralNode root = tree.create_root(new NonTerminal(NonTerminal.Type.PROGRAM));
                declaration_list(root.add_child(0, new NonTerminal(NonTerminal.Type.DECLARATION_LIST)));
        }

        @Override
        public ParseTree parse(IScanner s) {
                ParseTree tree = new ParseTree();
                try {
                        m_err_buf = new StringBuilder();
                        m_scanner = s;
                        m_curr_tok = s.scan_next();
                        program(tree);
                } catch (IOException ex) {
                        Logger.getLogger(ParserRecursiveDescent.class.getName()).log(Level.SEVERE, null, ex);
                }
                return tree;
        }
}
