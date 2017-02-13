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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author davis
 */
public class SymbolTable {

        private int                             m_curr_depth = 0;
        private boolean                         m_is_function = false;
        private SymbolTable                     m_parent = null;
        private final Map<String, Symbol>       m_symbols = new LinkedHashMap<>();
        
        public SymbolTable() {
        }
        
        public SymbolTable enter_scope() {
                SymbolTable next = new SymbolTable();
                next.m_parent = this;
                next.m_curr_depth = m_curr_depth + 1;
                return next;
        }
        
        public SymbolTable leave_scope() {
                return m_parent;
        }
        
        public boolean put(String name, Symbol symbol) {
                if (m_symbols.containsKey(name))
                        return false;
                m_symbols.put(name, symbol);
                return true;
        }
        
        public Symbol search(String name) {
                Symbol found = null;
                SymbolTable scope = this;
                do {
                        found = scope.m_symbols.get(name);
                        scope = scope.m_parent;
                } while (scope != null && found == null);
                return found;
        }
        
        public void set_function_scope() {
                m_is_function = true;
        }
        
        public boolean is_function_scope() {
                return m_is_function;
        }
        
        public void unset_function_scope() {
                m_is_function = false;
        }
        
        public int depth() {
                return m_curr_depth;
        }
        
        public List<Symbol> log_symbols() {
                List<SymbolTable> tables = new ArrayList<>();
                
                SymbolTable scope = this;
                int d = 0;
                do {
                        tables.add(scope);
                        scope = scope.m_parent;
                        d ++;
                } while (scope != null);
                
                List<Symbol> ss = new ArrayList<>();
                
                do {
                        scope = tables.get(-- d);
                        for (Map.Entry<String, Symbol> entry : scope.m_symbols.entrySet()) {
                                Symbol s = entry.getValue();
                                s.set_depth(tables.size() - d - 1);
                                ss.add(s);
                        }
                } while (d > 0);
                
                return ss;
        }
}
