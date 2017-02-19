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

import java.util.Objects;

/**
 * A token storage class with lexeme type, attribute and file pointer.
 * @author davis
 */
public class Token extends SyntacticElement {

        /**
         * All possible Lexeme specified by the language crux.
         */
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

        /**
         * Token construction with lexeme type, file location and attribute.
         * @param type lexeme type.
         * @param fp file location.
         * @param attri additional attribute (optional).
         */
        public Token(Lexeme type, FilePointer fp, String attri) {
                super(SyntacticElement.Type.Terminal);
                m_fp = fp;
                m_type = type;
                m_attri = attri;
        }

        /**
         * @return if the token is an EOF.
         */
        boolean is_eof() {
                return m_type == Lexeme.EOF;
        }
        
        /**
         * @return if the token is an error.
         */
        boolean is_err() {
                return m_type == Lexeme.ERROR;
        }
        
        Lexeme type() {
                return m_type;
        }
        
        String attribute() {
                return m_attri;
        }
        
        FilePointer file_pointer() {
                return m_fp;
        }
        
        @Override
        public boolean equals(Object o) {
                Token other = (Token) o;
                return m_type == other.m_type &&
                       m_attri.equals(other.m_attri);
        }

        @Override
        public int hashCode() {
                int hash = 5;
                hash = 11 * hash + Objects.hashCode(this.m_type);
                hash = 11 * hash + Objects.hashCode(this.m_attri);
                return hash;
        }

        @Override
        public String toString() {
                return m_type.toString()
                       + (m_attri == null || m_attri.isEmpty() ? "" : "(" + m_attri + ")") + m_fp;
        }
}
