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

import java.util.HashMap;
import java.util.Map;

/**
 * @author davis
 */
public class GeneralNode {

	protected final int m_id;
        protected GeneralNode m_parent;
        protected SyntacticElement m_element;
        protected Map<Integer, GeneralNode> m_children = new HashMap<>();
        protected int m_max_id = -1;

        public GeneralNode(GeneralNode parent, SyntacticElement elm, int id) {
                m_parent = parent;
                m_element = elm;
		m_id = id;
        }

        public SyntacticElement get_element() {
                return m_element;
        }

        public GeneralNode get_parent() {
                return m_parent;
        }

        public GeneralNode add_child(int id, SyntacticElement elm) {
                GeneralNode node = new GeneralNode(this, elm, id);
                m_children.put(id, node);
                m_max_id = Math.max(m_max_id, id);
                return node;
        }
        
        public GeneralNode get_child(int id) {
                return m_children.get(id);
        }

        public int children_size() {
                return m_children.size();
        }
        
        public int max_id() {
                return m_max_id;
        }
        
        private SyntacticElement bottom_left(GeneralNode node) {
                if (node.children_size() == 0) {
                        return node.get_element();
                } else {
                        return bottom_left(node.get_child(0));
                }
        }
        
        public SyntacticElement bottom_left() {
                return bottom_left(this);
        }
}
