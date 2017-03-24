package crux;

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
public class Symbol {

        private final String    m_name;
        private final IType     m_type;
        private int             m_depth;

        public Symbol(String name, IType type) {
                m_name = name;
                m_type = type;
        }
        
        public void set_depth(int depth) {
                m_depth = depth;
        }
        
        public int get_depth() {
                return m_depth;
        }

        public String name() {
                return m_name;
        }
        
        public IType get_type() {
                return m_type;
        }

        @Override
        public String toString() {
                return "Symbol(" + m_name + ")";
        }
}
