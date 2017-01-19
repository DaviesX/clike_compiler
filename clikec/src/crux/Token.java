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
public class Token {

        public enum Lexeme {
                AND,
                OR,
                NOT,
                LET,
                VAR,
                ARRAY,
                FUNC,
                IF,
                ELSE,
                WHILE,
                TRUE,
                FALSE,
                RETURN,
                OPEN_PAREN,
                CLOSE_PAREN,
                OPEN_BRACE,
                CLOSE_BRACE,
                OPEN_BRACKET,
                CLOSE_BRACKET,
                ADD,
                SUB,
                MUL,
                DIV,
                GREATER_EQUAL,
                LESSER_EQUAL,
                NOT_EQUAL,
                EQUAL,
                GREATER_THAN,
                LESS_THAN,
                ASSIGN,
                COMMA,
                SEMICOLON,
                COLON,
                CALL,
                INTEGER,
                FLOAT,
                IDENTIFIER,
                ERROR,
                EOF,
        };

        private final FilePointer m_fp;
        private final Lexeme m_type;
        private final String m_attri;

        public Token(Lexeme type, FilePointer fp, String attri) {
                m_fp = fp;
                m_type = type;
                m_attri = attri;
        }

        boolean is_eof() {
                return m_type == Lexeme.EOF;
        }

        @Override
        public String toString() {
                return m_type.toString()
                       + (m_attri == null || m_attri.isEmpty() ? "" : "(" + m_attri + ")") + m_fp;
        }
}
