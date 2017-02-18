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
 * Abstract syntax tree.
 * @author davis
 */
public class ParseTree {
        
        public class Node {
                private final Node                      m_parent;
                private final NonTerminal               m_nt;
                private final Token                     m_t;
                private final Map<Integer, Node>        m_children = new HashMap<>();
                
                public Node(Node parent, NonTerminal nt) {
                        m_parent = parent;
                        m_nt = nt;
                        m_t = null;
                }
                
                public Node(Node parent, Token t) {
                        m_parent = parent;
                        m_nt = null;
                        m_t = t;
                }
                
                public NonTerminal nonterminal() {
                        return m_nt;
                }
                
                public Token terminal() {
                        return m_t;
                }
                
                public Node get_parent(Node node) {
                        return m_parent;
                }
                
                public Node add_child(int id, NonTerminal nt) {
                        Node node = new Node(this, nt);
                        m_children.put(id, node);
                        return node;
                }
                
                public Node add_child(int id, Token t) {
                        Node node = new Node(this, t);
                        m_children.put(id, node);
                        return node;
                }
                
                public Node get_child(int id) {
                        return m_children.get(id);
                }
                
                public int children_size() {
                        return m_children.size();
                }
        }
        
        private Node    m_root = null;
        
        
        public Node create_root(NonTerminal nt) {
                m_root = new Node(null, nt);
                return m_root;
        }
        
        public Node get_root() {
                return m_root;
        }
        
        private int             m_d = 0;
        private StringBuilder   m_print_buffer = new StringBuilder();

        private void enter_node(NonTerminal nonterminal) {

                String node_data = new String();
                for (int i = 0; i < m_d; i++) {
                        node_data += "  ";
                }
                node_data += nonterminal.toString();
                m_print_buffer.append(node_data).append("\n");
                m_d ++;
        }

        private void exit_node(NonTerminal nonterminal) {
                m_d --;
        }
        
        private void to_string(Node node) {
                if (node == null || 
                    node.nonterminal() == null)
                        return;
                enter_node(node.nonterminal());
                for (int i = 0; i < node.children_size(); i ++) {
                        to_string(node.get_child(i));
                }
                exit_node(node.nonterminal());
        }
        
        @Override
        public String toString() {
                m_print_buffer.setLength(0);
                to_string(m_root);
                return m_print_buffer.toString();
        }
}
