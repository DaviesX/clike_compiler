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

        private SymbolTable             m_table = new SymbolTable();
        private final ErrorReport       m_errs = new ErrorReport();
        private Symbol                  m_curr_func = null;
        private boolean                 m_has_main = false;

        public SemanticsAnalyzer() {
                // Preload symbols.
                m_table.put("readInt", new Symbol("readInt",
                        new StaticType(
                                new StaticType(StaticType.T.INT),
                                new ArrayList<>())
                ));
                m_table.put("readFloat", new Symbol("readFloat", 
                        new StaticType(
                                new StaticType(StaticType.T.FLOAT), 
                                new ArrayList<>())
                ));
                m_table.put("printBool", new Symbol("printBool", 
                        new StaticType(
                                new StaticType(StaticType.T.VOID), 
                                new ArrayList<StaticType>() {{
                                        add(new StaticType(StaticType.T.BOOL));
                                }})
                ));
                m_table.put("printInt", new Symbol("printInt", 
                        new StaticType(
                                new StaticType(StaticType.T.VOID), 
                                new ArrayList<StaticType>() {{
                                        add(new StaticType(StaticType.T.INT));
                                }})
                ));
                m_table.put("printFloat", new Symbol("printFloat", 
                        new StaticType(
                                new StaticType(StaticType.T.VOID), 
                                new ArrayList<StaticType>() {{
                                        add(new StaticType(StaticType.T.FLOAT));
                                }})
                ));
                m_table.put("println", new Symbol("println", 
                        new StaticType(
                                new StaticType(StaticType.T.VOID), 
                                new ArrayList<>())
                ));
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
        
        private void log_type_error(FilePointer fp, String error) {
                m_errs.add(new TypeError(fp, error));
        }

        private Symbol declare_symbol(Token t, IType type) {
                Symbol s = new Symbol(t.attribute(), type);
                if (!m_table.put(t.attribute(), s)) {
                        m_errs.add(new DeclareSymbolError(t));
                        log_current_symbols();
                }
                return s;
        }

        private Symbol resolve_symbol(Token t) {
                Symbol sym = m_table.search(t.attribute());
                if (null == sym) {
                        m_errs.add(new ResolveSymbolError(t));
                        log_current_symbols();
                }
                return sym;
        }
        
        private static List<StaticType> get_operand_type(ASTNode node) {
                List<StaticType> types = new ArrayList<>();
                for (int i = 0; i <= node.max_id(); i ++) {
                        GeneralNode op = node.get_child(i);
                        if (op != null)
                                types.add((StaticType) ((AbstractMetaData) op.get_element()).get_type());
                }
                return types;
        }

	private void check(ASTNode node) throws ErrorReport {
		if (node == null) {
			return;
		}

		AbstractMetaData data = (AbstractMetaData) node.get_element();
		switch (data.type()) {
			case StatementList:
				if (!m_table.is_function_scope()) {
					m_table = m_table.enter_scope();
				} else {
					m_table.unset_function_scope();
				}
				break;
			case FunctionDefinition:
				List<Token> tokens = data.terminals();
				m_curr_func = declare_symbol(tokens.get(0), data.get_type());
				m_table = m_table.enter_scope();
				m_table.set_function_scope();
                                List<IType> decls = data.get_type().sub_decls();
				for (int i = 1; i < tokens.size(); i ++)
					declare_symbol(tokens.get(i), decls.get(i - 1));
				break;
			case VariableDeclaration:
			case ArrayDeclaration:
				declare_symbol((Token) data.terminals().get(0), data.get_type());
				break;
			case Call:
				resolve_symbol((Token) data.terminals().get(0));
				break;
			case AddressOf:
				resolve_symbol((Token) data.terminals().get(0));
				break;
		}
		
                for (int i = 0; i < node.children_size(); i++) {
			check((ASTNode) node.get_child(i));
		}
                
                List<StaticType> types = get_operand_type(node);
                IType t;
                Symbol s;
		switch (data.type()) {
			case StatementList:
				m_table = m_table.leave_scope();
				break;

                        case FunctionDefinition:
                                try {
                                        if ("main".equals(data.terminals().get(0).attribute())) {
                                                m_has_main = true;

                                                data.get_type().check_entrance();
                                        }
                                        data.get_type().check_decl(m_curr_func.name());
                                } catch (TypeError e) {
                                        log_type_error(node.get_pos(), e.get_msg());
                                }

                                m_curr_func = null;
                                break;
                                
                        case VariableDeclaration:
                        case ArrayDeclaration:
                                try {
                                        data.get_type().check_decl(data.terminals().get(0).attribute());
                                } catch (TypeError err) {
                                        log_type_error(node.get_pos(), err.get_msg());
                                }
                                break;

                        case Comparison:
                                try {
                                        t = types.get(0).compare(types.get(1));
                                        data.set_type(t);
                                } catch (TypeError err) {
                                        log_type_error(node.get_pos(), err.get_msg());
                                        data.set_type(new StaticType(err));
                                }
                                break;
                                
                        case LogicalOr:
                                try {
                                        t = types.get(0).or(types.get(1));
                                        data.set_type(t);
                                } catch (TypeError err) {
                                        log_type_error(node.get_pos(), err.get_msg());
                                        data.set_type(new StaticType(err));
                                }
                                break;
                                
                        case LogicalAnd:
                                try {
                                        t = types.get(0).and(types.get(1));
                                        data.set_type(t);
                                } catch (TypeError err) {
                                        log_type_error(node.get_pos(), err.get_msg());
                                        data.set_type(new StaticType(err));
                                }
                                break;
                                
                        case LogicalNot:
                                try {
                                        t = types.get(0).not();
                                        data.set_type(t);
                                } catch (TypeError err) {
                                        log_type_error(node.get_pos(), err.get_msg());
                                        data.set_type(new StaticType(err));
                                }
                                break;
                                
                        case Assignment:
                                try {
                                        t = types.get(0).assign(types.get(1));
                                        data.set_type(t);
                                } catch (TypeError err) {
                                        log_type_error(node.get_pos(), err.get_msg());
                                        data.set_type(new StaticType(err));
                                }
                                break;
                                
                        case AddressOf:
                                s = resolve_symbol((Token) data.terminals().get(0));
                                if (s == null)
                                        data.set_type(new StaticType(StaticType.T.VOID));
                                else {
                                        data.set_type(s.get_type());
                                }
                                break;
                                
                        case Index:
                                t = types.get(0).index(types.get(1));
                                if (t == null) {
                                        log_type_error(node.get_pos(), "Index must be of type int.");
                                        data.set_type(new StaticType(StaticType.T.BOOL));
                                }
                                break;
                                
                        case Dereference:
                                data.set_type(types.get(0));
                                break;
                                
                        case Call:
                                s = resolve_symbol((Token) data.terminals().get(0));
                                if (s == null)
                                        data.set_type(new StaticType(StaticType.T.VOID));
                                else {
                                        ASTNode explist = (ASTNode) node.get_child(0);
                                        List<StaticType> args = get_operand_type(explist);
                                        try {
                                                s.get_type().call(new StaticType(args), s.name());
                                                data.set_type(s.get_type());
                                        } catch (TypeError err) {
                                                log_type_error(node.get_pos(), err.get_msg());
                                        }
                                }
                                break;
                                
                        case Return:
                                try {
                                        m_curr_func.get_type().ret(types.get(0), m_curr_func.name());
                                        data.set_type(types.get(0));
                                } catch (TypeError e) {
                                        log_type_error(node.get_pos(), e.get_msg());
                                        data.set_type(m_curr_func.get_type());
                                }
                                break;
                                
                        case Addition:
                                try {
                                        t = types.get(0).add(types.get(1));
                                        data.set_type(t);
                                } catch (TypeError err) {
                                        log_type_error(node.get_pos(), err.get_msg());
                                        data.set_type(new StaticType(err));
                                }
                                break;
                                
                        case Subtraction:
                                t = types.get(0).sub(types.get(1));
                                if (t == null) {
                                        log_type_error(node.get_pos(), "Sub must have same type.");
                                        data.set_type(types.get(0));
                                } else {
                                        data.set_type(t);
                                }
                                break;
                                
                        case Multiplication:
                                t = types.get(0).mul(types.get(1));
                                if (t == null) {
                                        log_type_error(node.get_pos(), "Mul must have same type.");
                                        data.set_type(types.get(0));
                                } else {
                                        data.set_type(t);
                                }
                                break;
                                
                        case Division:
                                t = types.get(0).div(types.get(1));
                                if (t == null) {
                                        log_type_error(node.get_pos(), "Div must have same type.");
                                        data.set_type(types.get(0));
                                } else {
                                        data.set_type(t);
                                }
                                break;
		}
	}

        @Override
        public void analyze(AST tree) throws ErrorReport {
                m_has_main = false;
                check(tree.get_root());
                if (!m_has_main)
                        log_type_error(tree.get_root().get_pos(), "Doesn't have main.");
                if (!m_errs.is_empty()) {
                        throw m_errs;
                }
        }

}
