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

/**
 * @author davis
 */
public class NonTerminal {

        public enum Type {
                DESIGNATOR,
                TYPE,
                LITERAL,
                CALL_EXPRESSION,
                OP0,
                OP1,
                OP2,
                EXPRESSION3,
                EXPRESSION2,
                EXPRESSION1,
                EXPRESSION0,
                EXPRESSION_LIST,
                PARAMETER,
                PARAMETER_LIST,
                VARIABLE_DECLARATION,
                ARRAY_DECLARATION,
                FUNCTION_DEFINITION,
                DECLARATION,
                DECLARATION_LIST,
                ASSIGNMENT_STATEMENT,
                CALL_STATEMENT,
                IF_STATEMENT,
                WHILE_STATEMENT,
                RETURN_STATEMENT,
                STATEMENT_BLOCK,
                STATEMENT,
                STATEMENT_LIST,
                PROGRAM,
        };
        
        private final Type m_type;
        
        public NonTerminal(Type type) {
                m_type = type;
        }
        
        public Type type() {
                return m_type;
        }
        
        @Override
        public String toString() {
                return m_type.toString();
        }
}
