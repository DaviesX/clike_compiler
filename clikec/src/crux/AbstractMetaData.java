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
import java.util.List;

/**
 * @author davis
 */
public class AbstractMetaData extends SyntacticElement {
        
        public enum Type {
                ArrayDeclaration,
                VariableDeclaration,
                FunctionDefinition,
                LiteralBoolean,
                LiteralFloat,
                LiteralInteger,
                AddressOf,
                Dereference,
                Addition,
                Subtraction,
                Multiplication,
                Division,
                Comparison,
                LogicalAnd,
                LogicalOr,
                LogicalNot,
                Index,
                Call,
                Assignment,
                IfElseBranch,
                WhileLoop,
                Return,
                Error,
        }
        
        private final Type m_type;
        private final List<Token> m_toks = new ArrayList<>();
        
        public AbstractMetaData(Type type, Token token) {
                super(SyntacticElement.Type.Abstract);
                m_type = type;
                m_toks.add(token);
        }
        
        public AbstractMetaData(Type type, ArrayList<Token> token) {
                super(SyntacticElement.Type.Abstract);
                m_type = type;
                m_toks.addAll(token);
        }
        
        public Type type() {
                return m_type;
        }
        
        public List<Token> terminals() {
                return m_toks;
        }
        
        @Override
        public String toString() {
                String attri = null;
                switch (m_type) {
                        case LiteralFloat:
                        case LiteralInteger:
                        case LiteralBoolean:
                        case AddressOf:
                        case Dereference:
                                attri = m_toks.get(0).attribute();
                                break;
                        case VariableDeclaration:
                        case ArrayDeclaration:
                        case FunctionDefinition:
                                attri = "Symbol(" + m_toks.get(0).attribute() + ")";
                                break;
                }
                String ans = "ast." + m_type.toString() + m_toks.get(0).file_pointer();
                if (attri != null) {
                        ans += "[" + attri + "]";
                }
                return ans;
        }
}
