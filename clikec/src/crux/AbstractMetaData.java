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
                DeclarationList,
                StatementList,
                ExpressionList,
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
        private final FilePointer m_pos;
        private final List<Token> m_toks = new ArrayList<>();
        
        public AbstractMetaData(Type type, FilePointer pos) {
                super(SyntacticElement.Type.Abstract);
                m_type = type;
                m_pos = pos;
        }
        
        public AbstractMetaData(Type type, FilePointer pos, List<Token> token) {
                super(SyntacticElement.Type.Abstract);
                m_type = type;
                m_pos = pos;
                m_toks.addAll(token);
        }
        
        public void add_token(Token tok) {
                m_toks.add(tok);
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
                        case Call:
                        case Comparison:
                                attri = m_toks.get(0).attribute();
                                break;
                        case VariableDeclaration:
                        case ArrayDeclaration:
                                attri = "Symbol(" + m_toks.get(0).attribute() + ")";
                                break;
                        case FunctionDefinition:
                                attri = "Symbol(" + m_toks.get(0).attribute() + "), [";
                                if (m_toks.size() > 1) {
                                        attri += "Symbol(" + m_toks.get(1).attribute() + ")";
                                        for (int i = 2; i < m_toks.size(); i ++)
                                                attri += ", Symbol(" + m_toks.get(i).attribute() + ")";
                                }
                                attri += "]";
                                break;
                }
                String ans = "ast." + m_type.toString();
                if (m_pos != null)
                        ans += "(" + (m_pos.line_no() + 1) + "," + m_pos.column() + ")";
                if (attri != null) {
                        ans += "[" + attri + "]";
                }
                return ans;
        }
}
