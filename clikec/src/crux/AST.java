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
public class AST {
        
        private ASTNode m_root = null;

        public ASTNode create_root() {
                m_root = new ASTNode(null, 0);
                return m_root;
        }
        
        public ASTNode get_root() {
                return m_root;
        }
        
        private void print_node(AbstractMetaData meta, int depth, StringBuilder pb) {
                String node_data = new String();
                for (int i = 0; i < depth; i++) {
                        node_data += "  ";
                }
                node_data += meta.toString();
//		System.out.println(node_data);
                pb.append(node_data).append("\n");
        }

        private void to_string(ASTNode node, int depth, StringBuilder pb) {
                if (node == null)
                        return;
                
                print_node((AbstractMetaData) node.get_element(), depth, pb);
                for (int i = 0; i <= node.max_id(); i ++) {
                        to_string((ASTNode) node.get_child(i), depth + 1, pb);
                }
        }
        
        @Override
        public String toString() {
                StringBuilder pb = new StringBuilder();
                to_string(m_root, 0, pb);
                return pb.toString();
        }
}
