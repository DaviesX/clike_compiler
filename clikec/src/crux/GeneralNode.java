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

        protected final GeneralNode m_parent;
        protected SyntacticElement m_element;
        protected Map<Integer, GeneralNode> m_children = new HashMap<>();

        public GeneralNode(GeneralNode parent, SyntacticElement elm) {
                m_parent = parent;
                m_element = elm;
        }

        public SyntacticElement element() {
                return m_element;
        }

        public GeneralNode get_parent(GeneralNode node) {
                return m_parent;
        }

        public GeneralNode add_child(int id, SyntacticElement elm) {
                GeneralNode node = new GeneralNode(this, elm);
                m_children.put(id, node);
                return node;
        }
        
        public GeneralNode get_child(int id) {
                return m_children.get(id);
        }

        public int children_size() {
                return m_children.size();
        }
        
        private void simplify(GeneralNode node) {
                switch (node.children_size()) {
                        case 0:
                                // Do nothing.
                                break;
                        case 1:
                                GeneralNode down = skip(node);
                                node.m_element = down.m_element;
                                node.m_children = down.m_children;
                                simplify(node);
                                break;
                        default:
                                for (int i = 0; i < node.children_size(); i ++) {
                                        simplify(node.get_child(i));
                                }
                                break;
                }
        }
        
        public void simplify() {
                simplify(this);
        }
        
        private GeneralNode skip(GeneralNode node) {
                if (node.children_size() == 1) {
                        return skip(node.get_child(0));
                } else {
                        return node;
                }
        }
        
        public GeneralNode skip() {
                return skip(this);
        }
        
        private GeneralNode bottom_left(GeneralNode node) {
                if (node.children_size() == 0) {
                        return node;
                } else {
                        return skip(node.get_child(0));
                }
        }
        
        public GeneralNode bottom_left() {
                return skip(this);
        }
}
