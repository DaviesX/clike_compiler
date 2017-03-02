package crux;

import java.util.ArrayList;
import java.util.List;

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
/**
 * @author davis
 */
public class SemanticsAnalyzer implements ISemanticsAnalyzer {

        private SymbolTable m_table = new SymbolTable();
        private final ErrorReport m_errs = new ErrorReport();

        public SemanticsAnalyzer() {
                // Preload symbols.
                m_table.put("readInt", new Symbol("readInt"));
                m_table.put("readFloat", new Symbol("readFloat"));
                m_table.put("printBool", new Symbol("printBool"));
                m_table.put("printInt", new Symbol("printInt"));
                m_table.put("printFloat", new Symbol("printFloat"));
                m_table.put("println", new Symbol("println"));
        }

        private void log_current_symbols() {
                List<Symbol> symbols = m_table.log_symbols();
                for (int i = 0; i < symbols.size(); i++) {
                        Symbol s = symbols.get(i);
                        String indent = "";
                        for (int j = 0; j < s.get_depth(); j++) {
                                indent += "  ";
                        }
                        m_errs.add(new CompilingError(indent + s.toString()));
                }
        }

        private void declare_symbol(Token t) {
                if (!m_table.put(t.attribute(), new Symbol(t.attribute()))) {
                        m_errs.add(new DeclareSymbolError(t));
                        log_current_symbols();
                }
        }

        private void resolve_symbol(Token t) {
                if (null == m_table.search(t.attribute())) {
                        m_errs.add(new ResolveSymbolError(t));
                        log_current_symbols();
                }
        }

        private void check(GeneralNode node) throws ErrorReport {
                if (node == null) {
                        return;
                }
                
                if (node.element().is(SyntacticElement.Type.NonTerminal)) {
                        NonTerminal nt = (NonTerminal) node.element();
                        switch (nt.type()) {
                                case STATEMENT_BLOCK:
                                        if (!m_table.is_function_scope()) {
                                                m_table = m_table.enter_scope();
                                        } else {
                                                m_table.unset_function_scope();
                                        }
                                        break;
                                case FUNCTION_DEFINITION:
                                        declare_symbol((Token) node.get_child(1).element());
                                        m_table = m_table.enter_scope();
                                        m_table.set_function_scope();
                                        break;
                                case VARIABLE_DECLARATION:
                                case ARRAY_DECLARATION:
                                        declare_symbol((Token) node.get_child(1).element());
                                        break;
                                case PARAMETER:
                                        declare_symbol((Token) node.get_child(0).element());
                                        break;
                                case CALL_EXPRESSION:
                                        resolve_symbol((Token) node.get_child(1).element());
                                        break;
                                case DESIGNATOR:
                                        resolve_symbol((Token) node.get_child(0).element());
                                        break;
                        }
                        for (int i = 0; i < node.children_size(); i++) {
                                check(node.get_child(i));
                        }
                        switch (nt.type()) {
                                case STATEMENT_BLOCK:
                                        m_table = m_table.leave_scope();
                                        break;
                        }
                }
        }
        
        private void reduce_expression(GeneralNode node, GeneralNode ast) {
        }
        
        private GeneralNode process(GeneralNode node, GeneralNode ast, int id) {
                SyntacticElement elm = node.element();
                if (elm.is(SyntacticElement.Type.NonTerminal)) {
                        SyntacticElement e = node.bottom_left();
                        FilePointer pos = null;
                        if (e.is(SyntacticElement.Type.Terminal)) 
                                pos = ((Token) e).file_pointer();
                        switch (((NonTerminal) elm).type()) {
                                case FUNCTION_DEFINITION: {
                                        Token func_name = (Token) node.get_child(1).element();
                                        List<Token> symbols = new ArrayList<>();
                                        symbols.add(func_name);
                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.FunctionDefinition, pos, symbols));
                                } 
                                
                                case VARIABLE_DECLARATION: {
                                        Token var_name = (Token) node.get_child(1).element();
                                        List<Token> symbols = new ArrayList<>();
                                        symbols.add(var_name);
                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.VariableDeclaration, pos, symbols));
                                }
                                
                                case ARRAY_DECLARATION: {
                                        Token arr_name = (Token) node.get_child(1).element();
                                        List<Token> symbols = new ArrayList<>();
                                        symbols.add(arr_name);
                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.ArrayDeclaration, pos, symbols));
                                }
                                
                                case STATEMENT_LIST: {
                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.StatementList, pos));
                                }
                                
                                case CALL_EXPRESSION: {
                                        Token func_name = (Token) node.get_child(1).element();
                                        List<Token> symbols = new ArrayList<>();
                                        symbols.add(func_name);
                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.Call, pos, symbols));
                                }
                                
                                case IF_STATEMENT: {
                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.IfElseBranch, pos));
                                }
                                
                                case WHILE_STATEMENT: {
                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.WhileLoop, pos));
                                }
                                
                                case RETURN_STATEMENT: {
                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.Return, pos));
                                }
                                
                                case PARAMETER: {
                                        GeneralNode func = ast;
                                        AbstractMetaData meta = (AbstractMetaData) func.element();
                                        Token param_name = (Token) node.get_child(0).element();
                                        meta.add_token(param_name);
                                        break;
                                }
                                
                                case EXPRESSION_LIST: {
                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.ExpressionList, pos));
                                }
                                
                                case OP0: {
                                        Token op_name = (Token) node.get_child(0).element();
                                        List<Token> symbols = new ArrayList<>();
                                        symbols.add(op_name);
                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.Comparison, pos, symbols));
                                }
                                
                                case OP1: {
                                        Token op_name = (Token) node.get_child(0).element();
                                        switch (op_name.type()) {
                                                case ADD:
                                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.Addition, pos));
                                                case SUB:
                                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.Subtraction, pos));
                                                case OR:
                                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.LogicalOr, pos));
                                                
                                        }
                                        break;
                                }
                                
                                case OP2: {
                                        Token op_name = (Token) node.get_child(0).element();
                                        switch (op_name.type()) {
                                                case MUL:
                                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.Multiplication, pos));
                                                case DIV:
                                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.Division, pos));
                                                case AND:
                                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.LogicalAnd, pos));
                                                
                                        }
                                        break;
                                }
                                
                                case EXPRESSION0: {
                                        if (node.children_size() > 1) {
                                                Token op_name = (Token) node.get_child(1).get_child(0).element();
                                                List<Token> symbols = new ArrayList<>();
                                                symbols.add(op_name);
                                                return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.LogicalAnd, pos, symbols));
                                        } else {
                                                break;
                                        }
                                }
                                
                                case EXPRESSION1: {
                                        if (node.children_size() > 1) {
                                                Token op_name = (Token) node.get_child(1).get_child(0).element();
                                                List<Token> symbols = new ArrayList<>();
                                                symbols.add(op_name);
                                                return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.LogicalAnd, pos, symbols));
                                        } else {
                                                break;
                                        }
                                }
                                
                                case EXPRESSION2: {
                                        break;
                                }

                                case EXPRESSION3: {
                                        SyntacticElement child_elm = node.get_child(0).element();
                                        if (child_elm.is(SyntacticElement.Type.Terminal)) {
                                                Token op_name = (Token) child_elm;
                                                switch (op_name.type()) {
                                                        case NOT:
                                                                return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.LogicalNot, pos));
                                                }
                                        }
                                        break;
                                }
                                
                                case DESIGNATOR: {
                                        Token var_name = (Token) node.get_child(0).element();
                                        List<Token> symbols = new ArrayList<>();
                                        symbols.add(var_name);
                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.AddressOf, pos, symbols));
                                }
                                
                                case ASSIGNMENT_STATEMENT: {
                                        return ast.add_child(id, new AbstractMetaData(AbstractMetaData.Type.Dereference, pos));
                                }
                        }
                }
                return ast;
        }
        
        private void build_ast(GeneralNode node, GeneralNode ast, int id) {
                GeneralNode child = process(node, ast, id);
                for (int i = 0; i <= node.max_id(); i ++) {
                        build_ast(node.get_child(i), child, id + i);
                }
        }
  
        @Override
        public AST analyze(ParseTree tree) throws ErrorReport {
                check(tree.get_root());
                if (!m_errs.is_empty()) {
                        throw m_errs;
                }
                AST ast = new AST();
                Token tok = (Token) tree.get_root().bottom_left();
                GeneralNode ast_root = ast.create_root(new AbstractMetaData(AbstractMetaData.Type.DeclarationList, tok.file_pointer()));
                build_ast(tree.get_root(), ast_root, 0);
                return ast;
        }

}
