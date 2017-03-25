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
public class ASTNode extends GeneralNode {
        
        private final FilePointer       m_pos;
	
	public ASTNode(ASTNode parent, FilePointer fp, int id) {
		super(parent, null, id);
                m_pos = fp;
	}
        
        public FilePointer get_pos() {
                return m_pos;
        }
	
	public int get_id() {
		return m_id;
	}

	public void set_element(SyntacticElement elm) {
		m_element = elm;
	}
	
	public void set_parent(ASTNode parent) {
		m_parent = parent;
	}

	public ASTNode make_child(int id, FilePointer fp) {
                ASTNode node = new ASTNode(this, fp, id);
                m_children.put(id, node);
                m_max_id = Math.max(m_max_id, id);
                return node;
	}

	public ASTNode set_child(int id, ASTNode child) {
		m_children.put(id, child);
                m_max_id = Math.max(m_max_id, id);
		return child;
	}
	
}
