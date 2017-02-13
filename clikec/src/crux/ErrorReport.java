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
public class ErrorReport extends RuntimeException {
        
        private final List<CompilingError>      m_errors;
        
        public ErrorReport() {
                m_errors = new ArrayList<>();
        }
        
        public ErrorReport(CompilingError err) {
                m_errors = new ArrayList<>();
                m_errors.add(err);
        }
        
        public ErrorReport(List<CompilingError> errors) {
                m_errors = errors;
        }
        
        public void add(CompilingError err) {
                m_errors.add(err);
        }
        
        public List<CompilingError> get() {
                return m_errors;
        }
        
        public boolean is_empty() {
                return m_errors.isEmpty();
        }
        
        @Override
        public String toString() {
                StringBuilder s = new StringBuilder();
                m_errors.forEach((err) -> {
                        s.append(err.toString()).append('\n');
                });
                s.setLength(s.length() - 1);
                return s.toString();
        }
}
