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
public class ParseTree {

        private GeneralNode m_root;

        public GeneralNode create_root(NonTerminal nt) {
                m_root = new GeneralNode(null, nt, 0);
                return m_root;
        }
        
        public GeneralNode get_root() {
                return m_root;
        }
        
        private void print_node(NonTerminal nonterminal, int depth, StringBuilder pb) {

                String node_data = new String();
                for (int i = 0; i < depth; i++) {
                        node_data += "  ";
                }
                node_data += nonterminal.toString();
                pb.append(node_data).append("\n");
        }
        
        private void print_node(Token terminal, int depth, StringBuilder pb) {

                String node_data = new String();
                for (int i = 0; i < depth; i++) {
                        node_data += "  ";
                }
                node_data += terminal.toString();
                pb.append(node_data).append("\n");
        }

        private void to_string(GeneralNode node, int depth, StringBuilder pb) {
                if (node == null)
                        return;
                if (node.get_element().is(SyntacticElement.Type.Terminal)) {
                        print_node((Token) node.get_element(), depth, pb);
                } else {
                        print_node((NonTerminal) node.get_element(), depth, pb);
                }
                for (int i = 0; i < node.children_size(); i ++) {
                        to_string((GeneralNode) node.get_child(i), depth + 1, pb);
                }
        }
        
        @Override
        public String toString() {
                StringBuilder pb = new StringBuilder();
                to_string(m_root, 0, pb);
                return pb.toString();
        }
}
