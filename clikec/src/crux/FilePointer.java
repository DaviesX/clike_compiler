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

import java.io.BufferedReader;
import java.io.IOException;

/**
 * A file pointer with line-column information.
 * 
 * @author davis
 */
public class FilePointer {
        
        // Current location
        private int                     m_i;
        private int                     m_j;
        
        // Marker location.
        private int                     m_mi;
        private int                     m_mj;
        
        // System file pointer.
        private final BufferedReader    m_reader;
        
        private static final int        MAX_READ_AHEAD = 1024;

        /**
         * This construction accept a system file pointer. 
         * @param line_no zero-offset line number.
         * @param col zero-offset column number.
         * @param reader System pointer.
         */
        public FilePointer(int line_no, int col, BufferedReader reader) {
                m_i = line_no;
                m_j = col;
                m_reader = reader;

                m_mi = m_i;
                m_mj = m_j;
        }
        
        /**
         * This construction omitted the system file pointer.
         * system file pointer is set to null implicitly.
         * @param line_no zero-offset line number.
         * @param col zero-offset column number.
         */
        public FilePointer(int line_no, int col) {
                m_i = line_no;
                m_j = col;
                m_reader = null;

                m_mi = m_i;
                m_mj = m_j;
        }

        public BufferedReader get_reader() {
                return m_reader;
        }

        /**
         * @return zero-offset line number.
         */
        public int line_no() {
                return m_i;
        }

        /**
         * @return zero-offset column number.
         */
        public int column() {
                return m_j;
        }

        /**
         * Mark the current location. The next FilePointer.reset() will reset to this location.
         * @throws IOException 
         */
        public void mark() throws IOException {
                m_reader.mark(MAX_READ_AHEAD);
                m_mi = m_i;
                m_mj = m_j;
        }

        /**
         * Read next character.
         * @return the character, or the integer -1 if EOF is encountered.
         * @throws IOException 
         */
        public int next() throws IOException {
                int c = m_reader.read();
                if (c == '\n') {
                        m_i++;
                        m_j = 0;
                } else {
                        m_j++;
                }
                return c;
        }

        /**
         * Reset to the most recent marker location.
         * @throws IOException 
         */
        public void reset() throws IOException {
                m_reader.reset();
                m_i = m_mi;
                m_j = m_mj;
        }

        @Override
        public String toString() {
                return "(lineNum:" + (line_no() + 1) + ", charPos:" + column() + ")";
        }
}
