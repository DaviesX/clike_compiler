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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	private boolean m_build_pt = false;

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
	private void literal(ASTNode literal_node, GeneralNode node) throws IOException {
		List<Token> symbols = new ArrayList<>();
		symbols.add(m_curr_tok);
                
                AbstractMetaData meta;
		FilePointer pos = m_curr_tok.file_pointer();
		switch (m_curr_tok.type()) {
			case INTEGER:
                                meta = new AbstractMetaData(AbstractMetaData.Type.LiteralInt, pos, symbols);
                                meta.set_type(new StaticType("int"));
				literal_node.set_element(meta);
				break;
			case FLOAT:
                                meta = new AbstractMetaData(AbstractMetaData.Type.LiteralFloat, pos, symbols);
                                meta.set_type(new StaticType("float"));
				literal_node.set_element(meta);
				break;
			case TRUE:
			case FALSE:
                                meta = new AbstractMetaData(AbstractMetaData.Type.LiteralFloat, pos, symbols);
                                meta.set_type(new StaticType("bool"));
				literal_node.set_element(meta);
				break;
		}
		
		node.add_child(0, m_curr_tok);
		expect(NonTerminal.Type.LITERAL, new HashSet() {
			{
				add(Token.Lexeme.INTEGER);
				add(Token.Lexeme.FLOAT);
				add(Token.Lexeme.TRUE);
				add(Token.Lexeme.FALSE);
			}
		});
	}

	// op0 := ">=" | "<=" | "!=" | "==" | ">" | "<" 
	private void op0(ASTNode op_node, GeneralNode node) throws IOException {
		FilePointer pos = m_curr_tok.file_pointer();
		List<Token> symbols = new ArrayList<>();
		symbols.add(m_curr_tok);
		op_node.set_element(new AbstractMetaData(AbstractMetaData.Type.Comparison, pos, symbols));
		
		node.add_child(0, m_curr_tok);
		expect(NonTerminal.Type.OP0, new HashSet() {
			{
				add(Token.Lexeme.GREATER_EQUAL);
				add(Token.Lexeme.LESSER_EQUAL);
				add(Token.Lexeme.NOT_EQUAL);
				add(Token.Lexeme.EQUAL);
				add(Token.Lexeme.LESS_THAN);
				add(Token.Lexeme.GREATER_THAN);
			}
		});
	}

	// op1 := "+" | "-" | "or" .
	private void op1(ASTNode op_node, GeneralNode node) throws IOException {
		FilePointer pos = m_curr_tok.file_pointer();
		switch (m_curr_tok.type()) {
			case ADD:
				op_node.set_element(new AbstractMetaData(AbstractMetaData.Type.Addition, pos));
				break;
			case SUB:
				op_node.set_element(new AbstractMetaData(AbstractMetaData.Type.Subtraction, pos));
				break;
			case OR:
				op_node.set_element(new AbstractMetaData(AbstractMetaData.Type.LogicalOr, pos));
				break;
		}

		node.add_child(0, m_curr_tok);
		expect(NonTerminal.Type.OP1, new HashSet() {
			{
				add(Token.Lexeme.ADD);
				add(Token.Lexeme.SUB);
				add(Token.Lexeme.OR);
			}
		});
	}

	// op2 := "*" | "/" | "and" .
	private void op2(ASTNode op_node, GeneralNode node) throws IOException {
		FilePointer pos = m_curr_tok.file_pointer();
		switch (m_curr_tok.type()) {
			case MUL:
				op_node.set_element(new AbstractMetaData(AbstractMetaData.Type.Multiplication, pos));
				break;
			case DIV:
				op_node.set_element(new AbstractMetaData(AbstractMetaData.Type.Division, pos));
				break;
			case AND:
				op_node.set_element(new AbstractMetaData(AbstractMetaData.Type.LogicalAnd, pos));
				break;
		}
		
		node.add_child(0, m_curr_tok);
		expect(NonTerminal.Type.OP1, new HashSet() {
			{
				add(Token.Lexeme.MUL);
				add(Token.Lexeme.DIV);
				add(Token.Lexeme.AND);
			}
		});
	}

	// expression0 := expression1 [ op0 expression1 ] .
	private ASTNode expression0(ASTNode expr_node, GeneralNode node) throws IOException {
		expr_node = expression1(expr_node, node.add_child(0, new NonTerminal(NonTerminal.Type.EXPRESSION1)));

		ASTNode lhs = expr_node;
		if (have(new HashSet() {
			{
				add(Token.Lexeme.GREATER_EQUAL);
				add(Token.Lexeme.LESSER_EQUAL);
				add(Token.Lexeme.NOT_EQUAL);
				add(Token.Lexeme.EQUAL);
				add(Token.Lexeme.LESS_THAN);
				add(Token.Lexeme.GREATER_THAN);
			}
		})) {
			ASTNode op = new ASTNode((ASTNode) lhs.get_parent(), m_curr_tok.file_pointer(), lhs.get_id());
			lhs.set_parent(op);
			((ASTNode) op.get_parent()).set_child(op.get_id(), op);
			
			op.set_child(0, lhs);
			op0(op, node.add_child(1, new NonTerminal(NonTerminal.Type.OP0)));
			expression1(op.make_child(1, m_curr_tok.file_pointer()), 
                                    node.add_child(2, new NonTerminal(NonTerminal.Type.EXPRESSION1)));
			
			lhs = op;
		}
		return lhs;
	}

	// expression1 := expression2 { op1 expression2 } .
	private ASTNode expression1(ASTNode expr_node, GeneralNode node) throws IOException {
		expr_node = expression2(expr_node, node.add_child(0, new NonTerminal(NonTerminal.Type.EXPRESSION2)));
		
		ASTNode lhs = expr_node;
		int i_child = 1;
		while (have(new HashSet<Token.Lexeme>() {
			{
				add(Token.Lexeme.ADD);
				add(Token.Lexeme.SUB);
				add(Token.Lexeme.OR);
			}
		})) {
			ASTNode op = new ASTNode((ASTNode) lhs.get_parent(), m_curr_tok.file_pointer(), lhs.get_id());
			lhs.set_parent(op);
			((ASTNode) op.get_parent()).set_child(op.get_id(), op);
			
			op.set_child(0, lhs);
			op1(op, node.add_child(i_child, new NonTerminal(NonTerminal.Type.OP1)));
			expression2(op.make_child(1, m_curr_tok.file_pointer()), 
                                    node.add_child(i_child + 1, new NonTerminal(NonTerminal.Type.EXPRESSION2)));
			
			lhs = op;
			i_child += 2;
		}
		return lhs;
	}

	// expression2 := expression3 { op2 expression3 } .
	private ASTNode expression2(ASTNode expr_node, GeneralNode node) throws IOException {
		expression3(expr_node, node.add_child(0, new NonTerminal(NonTerminal.Type.EXPRESSION3)));

		ASTNode lhs = expr_node;
		int i_child = 1;
		while (have(new HashSet<Token.Lexeme>() {
			{
				add(Token.Lexeme.MUL);
				add(Token.Lexeme.DIV);
				add(Token.Lexeme.AND);
			}
		})) {
			ASTNode op = new ASTNode((ASTNode) lhs.get_parent(), m_curr_tok.file_pointer(), lhs.get_id());
			lhs.set_parent(op);
			((ASTNode) op.get_parent()).set_child(op.get_id(), op);
			
			op.set_child(0, lhs);
			op2(op, node.add_child(i_child, new NonTerminal(NonTerminal.Type.OP2)));
			expression3(op.make_child(1, m_curr_tok.file_pointer()), 
                                    node.add_child(i_child + 1, new NonTerminal(NonTerminal.Type.EXPRESSION3)));

			lhs = op;
			i_child += 2;
		}
		return lhs;
	}

	// expression3 := "not" expression3
	//      | "(" expression0 ")"
	//      | designator
	//      | call-expression
	//      | literal .
	private void expression3(ASTNode expr_node, GeneralNode node) throws IOException {
		switch (m_curr_tok.type()) {
			case NOT:
				expr_node.set_element(new AbstractMetaData(AbstractMetaData.Type.LogicalNot, m_curr_tok.file_pointer()));
				node.add_child(0, m_curr_tok);
				expect(Token.Lexeme.NOT);
				expression3(expr_node.make_child(0, m_curr_tok.file_pointer()), 
                                            node.add_child(1, new NonTerminal(NonTerminal.Type.EXPRESSION3)));
				break;
			case OPEN_PAREN:
				node.add_child(0, m_curr_tok);
				expect(Token.Lexeme.OPEN_PAREN);

				expression0(expr_node, node.add_child(1, new NonTerminal(NonTerminal.Type.EXPRESSION0)));

				node.add_child(2, m_curr_tok);
				expect(Token.Lexeme.CLOSE_PAREN);
				break;
			case IDENTIFIER:
				designator(expr_node, true, node.add_child(0, new NonTerminal(NonTerminal.Type.DESIGNATOR)));
				break;
			case CALL:
				call_expression(expr_node, node.add_child(0, new NonTerminal(NonTerminal.Type.CALL_EXPRESSION)));
				break;
			case INTEGER:
			case FLOAT:
			case TRUE:
			case FALSE:
				literal(expr_node, node.add_child(0, new NonTerminal(NonTerminal.Type.LITERAL)));
				break;
			default:
				expect(NonTerminal.Type.EXPRESSION3,
					new HashSet<Token.Lexeme>() {
					{
						add(Token.Lexeme.NOT);
						add(Token.Lexeme.OPEN_PAREN);
						add(Token.Lexeme.IDENTIFIER);
						add(Token.Lexeme.CALL);
						add(Token.Lexeme.INTEGER);
						add(Token.Lexeme.FLOAT);
						add(Token.Lexeme.TRUE);
						add(Token.Lexeme.FALSE);
					}
				});
				break;
		}
	}

	// expression-list := [ expression0 { "," expression0 } ] .
	private void expression_list(ASTNode exprl_node, GeneralNode node) throws IOException {
		exprl_node.set_element(new AbstractMetaData(AbstractMetaData.Type.ExpressionList, m_curr_tok.file_pointer()));
		if (have(new HashSet<Token.Lexeme>() {
			{
				add(Token.Lexeme.INTEGER);
				add(Token.Lexeme.FLOAT);
				add(Token.Lexeme.TRUE);
				add(Token.Lexeme.FALSE);

				add(Token.Lexeme.NOT);
				add(Token.Lexeme.OPEN_PAREN);
				add(Token.Lexeme.IDENTIFIER);
				add(Token.Lexeme.CALL);
			}
		})) {
			exprl_node = expression0(exprl_node.make_child(0, m_curr_tok.file_pointer()), 
                                                 node.add_child(0, new NonTerminal(NonTerminal.Type.EXPRESSION0)));
			int i_child = 1;
			while (have(Token.Lexeme.COMMA)) {
				node.add_child(i_child, m_curr_tok);
				expect(Token.Lexeme.COMMA);
				exprl_node = expression0(exprl_node.make_child(i_child, m_curr_tok.file_pointer()), 
                                                         node.add_child(i_child + 1, new NonTerminal(NonTerminal.Type.EXPRESSION0)));
				i_child += 2;
			}
		}
	}

	// call-expression := "::" IDENTIFIER "(" expression-list ")" .
	private void call_expression(ASTNode call_node, GeneralNode node) throws IOException {
		FilePointer pos = m_curr_tok.file_pointer();
		node.add_child(0, m_curr_tok);
		expect(Token.Lexeme.CALL);

		List<Token> symbols = new ArrayList<>();
		symbols.add(m_curr_tok);
		call_node.set_element(new AbstractMetaData(AbstractMetaData.Type.Call, pos, symbols));
		
		node.add_child(1, m_curr_tok);
		expect(Token.Lexeme.IDENTIFIER);

		node.add_child(2, m_curr_tok);
		expect(Token.Lexeme.OPEN_PAREN);

		expression_list(call_node.make_child(0, m_curr_tok.file_pointer()), 
                                node.add_child(3, new NonTerminal(NonTerminal.Type.EXPRESSION_LIST)));

		node.add_child(4, m_curr_tok);
		expect(Token.Lexeme.CLOSE_PAREN);
	}

	// designator := IDENTIFIER { "[" expression0 "]" } .
	private void designator(ASTNode desi_node, boolean is_dereference, GeneralNode node) throws IOException {
		FilePointer pos = m_curr_tok.file_pointer();
		
		if (is_dereference) {
			desi_node.set_element(new AbstractMetaData(AbstractMetaData.Type.Dereference, pos));
			desi_node = desi_node.make_child(0, m_curr_tok.file_pointer());
		}
		
		List<Token> symbols = new ArrayList<>();
		symbols.add(m_curr_tok);
		desi_node.set_element(new AbstractMetaData(AbstractMetaData.Type.AddressOf, m_curr_tok.file_pointer(), symbols));
		
		node.add_child(0, m_curr_tok);
		expect(Token.Lexeme.IDENTIFIER);

		ASTNode lhs = desi_node;
		int i_child = 1;
		while (have(Token.Lexeme.OPEN_BRACKET)) {
			node.add_child(i_child, m_curr_tok);
			expect(Token.Lexeme.OPEN_BRACKET);

			ASTNode index = new ASTNode((ASTNode) lhs.get_parent(), m_curr_tok.file_pointer(), lhs.get_id());
			lhs.set_parent(index);
			((ASTNode) index.get_parent()).set_child(index.get_id(), index);
			
			index.set_child(0, lhs);
			index.set_element(new AbstractMetaData(AbstractMetaData.Type.Index, m_curr_tok.file_pointer()));
			expression0(index.make_child(1, m_curr_tok.file_pointer()), 
                                    node.add_child(i_child + 1, new NonTerminal(NonTerminal.Type.EXPRESSION0)));

			node.add_child(i_child + 2, m_curr_tok);
			expect(Token.Lexeme.CLOSE_BRACKET);
			i_child += 3;
			
			lhs = index;
		}
	}

	// type := IDENTIFIER .
	private StaticType type(ASTNode ast_node, GeneralNode node) throws IOException {
		node.add_child(0, m_curr_tok);
                StaticType type = null;
                switch (m_curr_tok.attribute()) {
                        case "int":
                                type = new StaticType("int");
                                break;
                        case "float":
                                type = new StaticType("float");
                                break;
                        case "bool":
                                type = new StaticType("bool");
                                break;
                        case "void":
                                type = new StaticType("void");
                                break;
                        default:
                                type = new StaticType(m_curr_tok.attribute());
                                break;
                }
		expect(Token.Lexeme.IDENTIFIER);
                return type;
	}

	// variable-declaration := "var" IDENTIFIER ":" type ";" .
	private void variable_declaration(ASTNode var_node, GeneralNode node) throws IOException {
		FilePointer pos = m_curr_tok.file_pointer();
		node.add_child(0, m_curr_tok);
		expect(Token.Lexeme.VAR);

		List<Token> symbols = new ArrayList<>();
		symbols.add(m_curr_tok);
		var_node.set_element(new AbstractMetaData(AbstractMetaData.Type.VariableDeclaration,pos, symbols));
		
		node.add_child(1, m_curr_tok);
		expect(Token.Lexeme.IDENTIFIER);

		node.add_child(2, m_curr_tok);
		expect(Token.Lexeme.COLON);

		StaticType type = type(var_node, node.add_child(3, new NonTerminal(NonTerminal.Type.TYPE)));
                AbstractMetaData meta = (AbstractMetaData) var_node.get_element();
                meta.set_type(type);

		node.add_child(4, m_curr_tok);
		expect(Token.Lexeme.SEMICOLON);
	}

	// array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";" .
	private void array_declaration(ASTNode arr_node, GeneralNode node) throws IOException {
		FilePointer pos = m_curr_tok.file_pointer();
		node.add_child(0, m_curr_tok);
		expect(Token.Lexeme.ARRAY);

		
		List<Token> symbols = new ArrayList<>();
		symbols.add(m_curr_tok);
		arr_node.set_element(new AbstractMetaData(AbstractMetaData.Type.ArrayDeclaration, pos, symbols));
		
		node.add_child(1, m_curr_tok);
		expect(Token.Lexeme.IDENTIFIER);

		node.add_child(2, m_curr_tok);
		expect(Token.Lexeme.COLON);
                
                
		StaticType type = type(arr_node, node.add_child(3, new NonTerminal(NonTerminal.Type.TYPE)));

		int i_child = 4;

                List<String> arr_dims = new ArrayList<>();
		do {
                        String dim;
                        
			node.add_child(i_child, m_curr_tok);
			expect(Token.Lexeme.OPEN_BRACKET);

			node.add_child(i_child + 1, m_curr_tok);
                        arr_dims.add(m_curr_tok.attribute());
			expect(Token.Lexeme.INTEGER);
                        
			node.add_child(i_child + 2, m_curr_tok);
			expect(Token.Lexeme.CLOSE_BRACKET);
                        


			i_child += 3;
		} while (!have(Token.Lexeme.SEMICOLON));
                
                // Nesting array.
                for (int i = arr_dims.size() - 1; i >= 0; i --)
                        type = new StaticType(type, Integer.parseInt(arr_dims.get(i)));

                AbstractMetaData meta = (AbstractMetaData) arr_node.get_element();
                meta.set_type(type);
                
		node.add_child(i_child, m_curr_tok);
		expect(Token.Lexeme.SEMICOLON);
	}

	// parameter := IDENTIFIER ":" type .
	private StaticType parameter(ASTNode func_node, GeneralNode node) throws IOException {
		AbstractMetaData meta = (AbstractMetaData) func_node.get_element();
		meta.add_token(m_curr_tok);
		node.add_child(0, m_curr_tok);
		expect(Token.Lexeme.IDENTIFIER);

		node.add_child(1, m_curr_tok);
		expect(Token.Lexeme.COLON);

		return type(func_node, node.add_child(2, new NonTerminal(NonTerminal.Type.TYPE)));
	}

	// parameter-list := [ parameter { "," parameter } ] .
	private List<StaticType> parameter_list(ASTNode ast_node, GeneralNode node) throws IOException {
                List<StaticType> arg_types = new ArrayList<>();
		if (have(Token.Lexeme.IDENTIFIER)) {
			StaticType type = parameter(ast_node, node.add_child(0, new NonTerminal(NonTerminal.Type.PARAMETER)));
                        arg_types.add(type);

			int i_child = 1;
			while (have(Token.Lexeme.COMMA)) {
				node.add_child(i_child, m_curr_tok);

				expect(Token.Lexeme.COMMA);

				type = parameter(ast_node, node.add_child(i_child + 1, new NonTerminal(NonTerminal.Type.PARAMETER)));
                                arg_types.add(type);
				i_child += 2;
			}
		}
                return arg_types;
	}

	// assignment-statement := "let" designator "=" expression0 ";" .
	private void assignment_statement(ASTNode assign_node, GeneralNode node) throws IOException {
		assign_node.set_element(new AbstractMetaData(AbstractMetaData.Type.Assignment, m_curr_tok.file_pointer()));
		
		node.add_child(0, m_curr_tok);
		expect(Token.Lexeme.LET);

		designator(assign_node.make_child(0, m_curr_tok.file_pointer()), 
                           false, node.add_child(1, new NonTerminal(NonTerminal.Type.DESIGNATOR)));

		node.add_child(2, m_curr_tok);
		expect(Token.Lexeme.ASSIGN);

		expression0(assign_node.make_child(1, m_curr_tok.file_pointer()), 
                            node.add_child(3, new NonTerminal(NonTerminal.Type.EXPRESSION0)));

		node.add_child(4, m_curr_tok);
		expect(Token.Lexeme.SEMICOLON);
	}

	// call-statement := call-expression ";" .
	private void call_statement(ASTNode ast_node, GeneralNode node) throws IOException {
		call_expression(ast_node, node.add_child(0, new NonTerminal(NonTerminal.Type.CALL_EXPRESSION)));
		node.add_child(1, m_curr_tok);
		expect(Token.Lexeme.SEMICOLON);
	}

	// if-statement := "if" expression0 statement-block [ "else" statement-block ] .
	private void if_statement(ASTNode if_node, GeneralNode node) throws IOException {
		if_node.set_element(new AbstractMetaData(AbstractMetaData.Type.IfElseBranch, m_curr_tok.file_pointer()));
		
		node.add_child(0, m_curr_tok);
		expect(Token.Lexeme.IF);

		expression0(if_node.make_child(0, m_curr_tok.file_pointer()), 
                            node.add_child(1, new NonTerminal(NonTerminal.Type.EXPRESSION0)));
		statement_block(if_node.make_child(1, m_curr_tok.file_pointer()), 
                                node.add_child(2, new NonTerminal(NonTerminal.Type.STATEMENT_BLOCK)));

		if (have(Token.Lexeme.ELSE)) {
			node.add_child(3, m_curr_tok);
			expect(Token.Lexeme.ELSE);

			statement_block(if_node.make_child(2, m_curr_tok.file_pointer()), 
                                        node.add_child(4, new NonTerminal(NonTerminal.Type.STATEMENT_BLOCK)));
		} else {
			if_node.make_child(2, m_curr_tok.file_pointer()).
                                set_element(new AbstractMetaData(AbstractMetaData.Type.StatementList, m_curr_tok.file_pointer()));
		}
	}

	// while-statement := "while" expression0 statement-block .
	private void while_statement(ASTNode while_node, GeneralNode node) throws IOException {
		while_node.set_element(new AbstractMetaData(AbstractMetaData.Type.WhileLoop, m_curr_tok.file_pointer()));
		
		node.add_child(0, m_curr_tok);
		expect(Token.Lexeme.WHILE);
		
		expression0(while_node.make_child(0, m_curr_tok.file_pointer()), 
                            node.add_child(1, new NonTerminal(NonTerminal.Type.EXPRESSION0)));
		statement_block(while_node.make_child(1, m_curr_tok.file_pointer()), 
                                node.add_child(2, new NonTerminal(NonTerminal.Type.STATEMENT_BLOCK)));
	}

	// return-statement := "return" expression0 ";" .
	private void return_statement(ASTNode return_node, GeneralNode node) throws IOException {
		return_node.set_element(new AbstractMetaData(AbstractMetaData.Type.Return, m_curr_tok.file_pointer()));
		
		node.add_child(0, m_curr_tok);
		expect(Token.Lexeme.RETURN);

		expression0(return_node.make_child(0, m_curr_tok.file_pointer()), 
                            node.add_child(1, new NonTerminal(NonTerminal.Type.EXPRESSION0)));

		node.add_child(2, m_curr_tok);
		expect(Token.Lexeme.SEMICOLON);
	}

	// statement := variable-declaration
	//      | call-statement
	//      | assignment-statement
	//      | if-statement
	//      | while-statement
	//      | return-statement .
	private void statement(ASTNode ast_node, GeneralNode node) throws IOException {
		switch (m_curr_tok.type()) {
			case VAR:
				variable_declaration(ast_node, node.add_child(0, new NonTerminal(
					NonTerminal.Type.VARIABLE_DECLARATION)));
				break;
			case ARRAY:
				array_declaration(ast_node, node.add_child(0, new NonTerminal(
					NonTerminal.Type.ARRAY_DECLARATION)));
				break;
			case CALL:
				call_statement(ast_node, node.add_child(0, new NonTerminal(
					NonTerminal.Type.CALL_STATEMENT)));
				break;
			case LET:
				assignment_statement(ast_node, node.add_child(0, new NonTerminal(
					NonTerminal.Type.ASSIGNMENT_STATEMENT)));
				break;
			case IF:
				if_statement(ast_node, node.add_child(0, new NonTerminal(
					NonTerminal.Type.IF_STATEMENT)));
				break;
			case WHILE:
				while_statement(ast_node, node.add_child(0, new NonTerminal(
					NonTerminal.Type.WHILE_STATEMENT)));
				break;
			case RETURN:
				return_statement(ast_node, node.add_child(0, new NonTerminal(
					NonTerminal.Type.RETURN_STATEMENT)));
				break;
			default:
				expect(NonTerminal.Type.STATEMENT, new HashSet<Token.Lexeme>() {
					{
						add(Token.Lexeme.VAR);
						add(Token.Lexeme.CALL);
						add(Token.Lexeme.LET);
						add(Token.Lexeme.IF);
						add(Token.Lexeme.WHILE);
						add(Token.Lexeme.RETURN);
					}
				});
				break;
		}
	}

	// statement-list := { statement } .
	private void statement_list(ASTNode stmt_node, GeneralNode node) throws IOException {
		stmt_node.set_element(new AbstractMetaData(AbstractMetaData.Type.StatementList, m_curr_tok.file_pointer()));
		
		int i_child = 0;
		while (have(new HashSet<Token.Lexeme>() {
			{
				add(Token.Lexeme.VAR);
				add(Token.Lexeme.CALL);
				add(Token.Lexeme.LET);
				add(Token.Lexeme.IF);
				add(Token.Lexeme.WHILE);
				add(Token.Lexeme.RETURN);
			}
		})) {
			statement(stmt_node.make_child(i_child, m_curr_tok.file_pointer()), 
                                  node.add_child(i_child, new NonTerminal(NonTerminal.Type.STATEMENT)));
			i_child++;
		}
	}

	// statement-block := "{" statement-list "}" .
	private void statement_block(ASTNode ast_node, GeneralNode node) throws IOException {
		node.add_child(0, m_curr_tok);
		expect(Token.Lexeme.OPEN_BRACE);
		
		if (have(new HashSet<Token.Lexeme>() {
			{
				add(Token.Lexeme.VAR);
				add(Token.Lexeme.CALL);
				add(Token.Lexeme.LET);
				add(Token.Lexeme.IF);
				add(Token.Lexeme.WHILE);
				add(Token.Lexeme.RETURN);
			}
		})) {
			statement_list(ast_node, node.add_child(1, new NonTerminal(NonTerminal.Type.STATEMENT_LIST)));
			node.add_child(2, m_curr_tok);
			expect(Token.Lexeme.CLOSE_BRACE);
		} else {
			node.add_child(1, m_curr_tok);
			expect(Token.Lexeme.CLOSE_BRACE);
		}
	}

	// function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block .
	private void function_definition(ASTNode func_node, GeneralNode node) throws IOException {
		FilePointer pos = m_curr_tok.file_pointer();
		
		node.add_child(0, m_curr_tok);
		expect(Token.Lexeme.FUNC);
		
		List<Token> symbols = new ArrayList<>();
		symbols.add(m_curr_tok);
		func_node.set_element(new AbstractMetaData(AbstractMetaData.Type.FunctionDefinition, pos, symbols));
		
		node.add_child(0, m_curr_tok);
		expect(Token.Lexeme.IDENTIFIER);

		node.add_child(2, m_curr_tok);
		expect(Token.Lexeme.OPEN_PAREN);

		List<StaticType> arg_types = parameter_list(func_node, node.add_child(3, new NonTerminal(NonTerminal.Type.PARAMETER_LIST)));

		node.add_child(4, m_curr_tok);
		expect(Token.Lexeme.CLOSE_PAREN);

		node.add_child(5, m_curr_tok);
		expect(Token.Lexeme.COLON);

		StaticType ret_type = type(func_node, node.add_child(6, new NonTerminal(NonTerminal.Type.TYPE)));
                AbstractMetaData meta = (AbstractMetaData) func_node.get_element();
                meta.set_type(new StaticType(ret_type, arg_types));

		statement_block(func_node.make_child(0, m_curr_tok.file_pointer()), 
                                node.add_child(7, new NonTerminal(NonTerminal.Type.STATEMENT_BLOCK)));
	}

	// declaration := variable-declaration | array-declaration | function-definition .
	private void declaration(ASTNode ast_node, GeneralNode node) throws IOException {
		switch (m_curr_tok.type()) {
			case VAR:
				variable_declaration(ast_node, node.add_child(0, new NonTerminal(
					NonTerminal.Type.VARIABLE_DECLARATION)));
				break;
			case FUNC:
				function_definition(ast_node, node.add_child(0, new NonTerminal(
					NonTerminal.Type.FUNCTION_DEFINITION)));
				break;
			case ARRAY:
				array_declaration(ast_node, node.add_child(0, new NonTerminal(
					NonTerminal.Type.ARRAY_DECLARATION)));
				break;
			default:
				expect(NonTerminal.Type.DECLARATION, new HashSet<Token.Lexeme>() {
					{
						add(Token.Lexeme.VAR);
						add(Token.Lexeme.FUNC);
						add(Token.Lexeme.ARRAY);
					}
				});
				break;
		}
	}

	// declaration-list := { declaration } .
	private void declaration_list(ASTNode ast_node, GeneralNode node) throws IOException {
		ast_node.set_element(new AbstractMetaData(AbstractMetaData.Type.DeclarationList, m_curr_tok.file_pointer()));

		int i_child = 0;
		while (have(new HashSet<Token.Lexeme>() {
			{
				add(Token.Lexeme.VAR);
				add(Token.Lexeme.FUNC);
				add(Token.Lexeme.ARRAY);
			}
		})) {
			declaration(ast_node.make_child(i_child, m_curr_tok.file_pointer()), 
                                    node.add_child(i_child, new NonTerminal(NonTerminal.Type.DECLARATION)));
			i_child++;
		}
	}

	// program := declaration-list EOF .
	private void program(AST ast, ParseTree tree) throws IOException {
		GeneralNode root = tree.create_root(new NonTerminal(NonTerminal.Type.PROGRAM));
		ASTNode ast_root = ast.create_root(m_curr_tok.file_pointer());

		declaration_list(ast_root, root.add_child(0, new NonTerminal(NonTerminal.Type.DECLARATION_LIST)));
	}

	@Override
	public void parse(IScanner s, AST ast, ParseTree pt) throws ErrorReport {
		try {
			m_build_pt = pt != null;

			m_err_buf = new StringBuilder();
			m_scanner = s;
			m_curr_tok = s.scan_next();
			program(ast, pt);
		} catch (IOException ex) {
			Logger.getLogger(ParserRecursiveDescent.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
