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
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author davis
 */
public class Scanner {

        private final FilePointer m_fp;
        private final Map<String, Token.Lexeme> m_keywords;

        public Scanner(BufferedReader reader) {
                m_fp = new FilePointer(0, 0, reader);

                m_keywords = new HashMap<>();
                m_keywords.put("and", Token.Lexeme.AND);
                m_keywords.put("or", Token.Lexeme.OR);
                m_keywords.put("not", Token.Lexeme.NOT);
                m_keywords.put("let", Token.Lexeme.LET);
                m_keywords.put("var", Token.Lexeme.VAR);
                m_keywords.put("array", Token.Lexeme.ARRAY);
                m_keywords.put("func", Token.Lexeme.FUNC);
                m_keywords.put("if", Token.Lexeme.IF);
                m_keywords.put("else", Token.Lexeme.ELSE);
                m_keywords.put("while", Token.Lexeme.WHILE);
                m_keywords.put("true", Token.Lexeme.TRUE);
                m_keywords.put("false", Token.Lexeme.FALSE);
                m_keywords.put("return", Token.Lexeme.RETURN);
        }

        private static boolean match_next_integer(FilePointer fp, StringBuilder sb) throws IOException {
                fp.mark();
                int first = fp.next();
                if (Character.isDigit(first)) {
                        sb.append((char) first);

                        fp.mark();
                        int next = fp.next();
                        while (Character.isDigit(next)) {
                                fp.mark();
                                sb.append((char) next);
                                next = fp.next();
                        }
                        fp.reset();
                        return true;
                } else {
                        fp.reset();
                        return false;
                }
        }

        private static Token.Lexeme match_next_number(FilePointer fp, StringBuilder sb) throws IOException {
                if (match_next_integer(fp, sb)) {
                        if (match_next_char(fp, '.', true)) {
                                sb.append('.');
                                match_next_integer(fp, sb);
                                return Token.Lexeme.FLOAT;
                        } else {
                                return Token.Lexeme.INTEGER;
                        }
                } else {
                        return Token.Lexeme.ERROR;
                }
        }

        private static Token.Lexeme match_next_identifier(FilePointer fp,
                                                          Map<String, Token.Lexeme> keywords,
                                                          StringBuilder sb) throws IOException {
                fp.mark();
                int first = fp.next();
                if (Character.isAlphabetic(first) || first == '_') {
                        sb.append((char) first);

                        fp.mark();
                        int next = fp.next();
                        while (Character.isAlphabetic(next) || Character.isDigit(next) || next == '_') {
                                fp.mark();
                                sb.append((char) next);
                                next = fp.next();
                        }
                        fp.reset();
                        Token.Lexeme kw = keywords.get(sb.toString());
                        if (kw == null) {
                                return Token.Lexeme.IDENTIFIER;
                        } else {
                                return kw;
                        }
                } else {
                        fp.reset();
                        return Token.Lexeme.ERROR;
                }
        }

        private static boolean match_next_char(FilePointer fp, char tc, boolean reset) throws IOException {
                fp.mark();
                int c = fp.next();
                if (c == tc) {
                        return true;
                } else {
                        if (reset) {
                                fp.reset();
                        }
                        return false;
                }
        }

        private static boolean match_next_comment(FilePointer fp) throws IOException {
                fp.mark();
                int c = fp.next();
                if (c == '/') {
                        c = fp.next();
                        while (c != '\n' && c != -1) {
                                fp.mark();
                                c = fp.next();
                        }
                        fp.reset();
                        return true;
                } else {
                        fp.reset();
                        return false;
                }
        }

        static class MatchResult {

                public Token.Lexeme type;
                public int length;

                public MatchResult(Token.Lexeme type, int length) {
                        this.type = type;
                        this.length = length;
                }
        }

        private static MatchResult match_next_everything(
                FilePointer fp, Map<String, Token.Lexeme> keywords, StringBuilder builder) throws IOException {
                fp.mark();
                while (true) {
                        int c = fp.next();
                        switch (c) {
                                case -1:
                                        return new MatchResult(Token.Lexeme.EOF, 1);
                                case Character.SPACE_SEPARATOR:
                                case Character.LINE_SEPARATOR:
                                case '\n':
                                case ' ':
                                        fp.mark();
                                        break;
                                case '(':
                                        return new MatchResult(Token.Lexeme.OPEN_PAREN, 1);
                                case ')':
                                        return new MatchResult(Token.Lexeme.CLOSE_PAREN, 1);
                                case '{':
                                        return new MatchResult(Token.Lexeme.OPEN_BRACE, 1);
                                case '}':
                                        return new MatchResult(Token.Lexeme.CLOSE_BRACE, 1);
                                case '[':
                                        return new MatchResult(Token.Lexeme.OPEN_BRACKET, 1);
                                case ']':
                                        return new MatchResult(Token.Lexeme.CLOSE_BRACKET, 1);
                                case '+':
                                        return new MatchResult(Token.Lexeme.ADD, 1);
                                case '-':
                                        return new MatchResult(Token.Lexeme.SUB, 1);
                                case '*':
                                        return new MatchResult(Token.Lexeme.MUL, 1);
                                case '/':
                                        if (match_next_comment(fp)) {
                                                break;
                                        } else {
                                                return new MatchResult(Token.Lexeme.DIV, 1);
                                        }
                                case ',':
                                        return new MatchResult(Token.Lexeme.COMMA, 1);
                                case ';':
                                        return new MatchResult(Token.Lexeme.SEMICOLON, 1);
                                case '<':
                                        if (!match_next_char(fp, '=', true)) {
                                                return new MatchResult(Token.Lexeme.LESS_THAN, 1);
                                        } else {
                                                return new MatchResult(Token.Lexeme.LESSER_EQUAL, 2);
                                        }
                                case '>':
                                        if (!match_next_char(fp, '=', true)) {
                                                return new MatchResult(Token.Lexeme.GREATER_THAN, 1);
                                        } else {
                                                return new MatchResult(Token.Lexeme.GREATER_EQUAL, 2);
                                        }
                                case '=':
                                        if (!match_next_char(fp, '=', true)) {
                                                return new MatchResult(Token.Lexeme.ASSIGN, 1);
                                        } else {
                                                return new MatchResult(Token.Lexeme.EQUAL, 2);
                                        }
                                case ':':
                                        if (!match_next_char(fp, ':', true)) {
                                                return new MatchResult(Token.Lexeme.COLON, 1);
                                        } else {
                                                return new MatchResult(Token.Lexeme.CALL, 2);
                                        }
                                case '!':
                                        if (match_next_char(fp, '=', true)) {
                                                return new MatchResult(Token.Lexeme.NOT_EQUAL, 2);
                                        } else {
                                                builder.append("Unexpected character: ").append((char) c);
                                                return new MatchResult(Token.Lexeme.ERROR, 1);
                                        }
                                default:
                                        fp.reset();
                                        Token.Lexeme r;

                                        if (Token.Lexeme.ERROR != (r = match_next_identifier(fp, keywords, builder))) {
                                                int l = builder.length();
                                                if (r != Token.Lexeme.IDENTIFIER) {
                                                        builder.setLength(0);
                                                }
                                                return new MatchResult(r, l);
                                        } else if (Token.Lexeme.ERROR != (r = match_next_number(fp, builder))) {
                                                return new MatchResult(r, builder.length());
                                        } else {
                                                fp.next();
                                                builder.append("Unexpected character: ").append((char) c);
                                                return new MatchResult(Token.Lexeme.ERROR, 1);
                                        }
                        }
                }
        }

        public Token scan_next() throws IOException {
                StringBuilder sb = new StringBuilder();
                MatchResult res = match_next_everything(m_fp, m_keywords, sb);
                return new Token(res.type,
                                 new FilePointer(m_fp.line_no(), 
                                                 m_fp.column() - res.length + 1),
                                 sb.toString());
        }
}
