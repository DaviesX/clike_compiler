package crux;

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
                
                if (node.get_element().is(SyntacticElement.Type.NonTerminal)) {
                        NonTerminal nt = (NonTerminal) node.get_element();
                        switch (nt.type()) {
                                case STATEMENT_BLOCK:
                                        if (!m_table.is_function_scope()) {
                                                m_table = m_table.enter_scope();
                                        } else {
                                                m_table.unset_function_scope();
                                        }
                                        break;
                                case FUNCTION_DEFINITION:
                                        declare_symbol((Token) node.get_child(1).get_element());
                                        m_table = m_table.enter_scope();
                                        m_table.set_function_scope();
                                        break;
                                case VARIABLE_DECLARATION:
                                case ARRAY_DECLARATION:
                                        declare_symbol((Token) node.get_child(1).get_element());
                                        break;
                                case PARAMETER:
                                        declare_symbol((Token) node.get_child(0).get_element());
                                        break;
                                case CALL_EXPRESSION:
                                        resolve_symbol((Token) node.get_child(1).get_element());
                                        break;
                                case DESIGNATOR:
                                        resolve_symbol((Token) node.get_child(0).get_element());
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
  
        @Override
        public void analyze(ParseTree tree) throws ErrorReport {
                check(tree.get_root());
                if (!m_errs.is_empty()) {
                        throw m_errs;
                }
        }

}
