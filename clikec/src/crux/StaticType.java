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
public class StaticType implements IType {

	public enum T {
                NULL,
		INT,
		FLOAT,
		BOOL,
		VOID,
		ARRAY,
		FUNC,
		ARGS,
	};

	private final T			m_type;
        private String                  m_unknown = null;
	private final StaticType 	m_sub_type;
	private final List<StaticType>	m_types;
        private int                     m_dim;

	public StaticType(String typename) {
                switch (typename) {
                        case "int":
                                m_type = T.INT;
                                break;
                        case "float":
                                m_type = T.FLOAT;
                                break;
                        case "bool":
                                m_type = T.BOOL;
                                break;
                        case "void":
                                m_type = T.VOID;
                                break;
                        default:
                                m_type = T.NULL;
                                m_unknown = typename;
                                break;
                }
		m_sub_type = null;
		m_types = new ArrayList<>();
	}
        
        public StaticType(TypeError err) {
                m_type = T.NULL;
                m_unknown = "ErrorType(" + err.get_msg() + ")";
                m_sub_type = null;
		m_types = new ArrayList<>();
	}
        
        public StaticType(T type) {
                m_type = type;
		m_sub_type = null;
		m_types = new ArrayList<>();
	}

	public StaticType(StaticType sub_type, int dim) {
		m_type = T.ARRAY;
		m_sub_type = sub_type;
                m_dim = dim;
		m_types = new ArrayList<>();
	}

	public StaticType(StaticType sub_type, List<StaticType> types) {
		m_type = T.FUNC;
		m_sub_type = sub_type;
		m_types = types;
	}

	public StaticType(List<StaticType> types) {
		m_type = T.ARGS;
		m_sub_type = null;
		m_types = types;
	}
        
	public StaticType return_type() {
		switch (m_type) {
			case INT:
			case FLOAT:
			case BOOL:
			case VOID:
			case ARRAY:
                        case NULL:
				return this;
			case FUNC:
				return m_sub_type;
		}
		return null;
	}

	/*public boolean equivalent(StaticType rhs) {
		return (m_sub_type == null && rhs.m_sub_type == m_sub_type) || 
		       ((m_sub_type != null && rhs.m_sub_type != null) && 
		        (m_type == rhs.m_type && m_sub_type.equals(rhs) && m_sub_type.m_types.equals(rhs.m_types)));
	}*/
        
        @Override
        public List<IType> sub_decls() {
                List<IType> sub_decls = new ArrayList<>();
                m_types.forEach((t) -> {
                        sub_decls.add(t);
                });
                return sub_decls;
        }
        
        private String build_array_string(StringBuilder b, StaticType type) {
                b.append("array[").append(m_dim).append(",");
                if (type.m_sub_type.m_type != T.ARRAY)  b.append(type.type_string());
                else                                    b.append(build_array_string(b, type.m_sub_type));
                b.append("]");
                return b.toString();
        }
        
        private String build_args_string(StringBuilder b, List<StaticType> args) {
                b.append("TypeList(");
                if (!args.isEmpty()) {
                        b.append(args.get(0).type_string());
                        for (int i = 1; i < args.size(); i ++)
                                b.append(",").append(args.get(i).type_string());
                }
                b.append(")");
                return b.toString();
        }
        
        private String type_string() {
                if (m_unknown != null) {
                        return m_unknown;
                } else {
                        switch (m_type) {
                                case ARRAY:
                                        return build_array_string(new StringBuilder(), this);
                                case FUNC:
                                        return "(" + build_args_string(new StringBuilder(), m_types) + "):" + m_sub_type.type_string();
                                case ARGS:
                                        return build_args_string(new StringBuilder(), m_types);
                                default:
                                        return m_type.toString().toLowerCase();
                        }
                }
        }

	@Override
	public IType add(IType that) throws TypeError {
		if (return_type().m_type == ((StaticType) that).m_type && 
		    (return_type().m_type == T.INT || return_type().m_type == T.FLOAT)) {
                        return return_type();
                } else {
                        throw new TypeError("Cannot add " + return_type().type_string()
                                            + " with " + ((StaticType) that).type_string() + ".");
                }
	}

	@Override
	public IType sub(IType that) throws TypeError {
                if (return_type().m_type == ((StaticType) that).m_type && 
		    (return_type().m_type == T.INT || return_type().m_type == T.FLOAT)) {
                        return return_type();
                } else {
                        throw new TypeError("Cannot subtract " + return_type().type_string()
                                            + " with " + ((StaticType) that).type_string() + ".");
                }
	}

	@Override
	public IType mul(IType that) throws TypeError {
                if (return_type().m_type == ((StaticType) that).m_type && 
		    (return_type().m_type == T.INT || return_type().m_type == T.FLOAT)) {
                        return return_type();
                } else {
                        throw new TypeError("Cannot multiply " + return_type().type_string()
                                            + " with " + ((StaticType) that).type_string() + ".");
                }
	}

	@Override
	public IType div(IType that) throws TypeError {
                if (return_type().m_type == ((StaticType) that).m_type && 
		    (return_type().m_type == T.INT || return_type().m_type == T.FLOAT)) {
                        return return_type();
                } else {
                        throw new TypeError("Cannot divide " + return_type().type_string()
                                            + " with " + ((StaticType) that).type_string() + ".");
                }
	}

	@Override
	public IType and(IType that) throws TypeError {
		if (return_type().m_type == ((StaticType) that).m_type && 
		    return_type().m_type == T.BOOL) {
                        return return_type();
                } else {
                        throw new TypeError("Cannot compute " + return_type().type_string() 
                                + " and " + ((StaticType) that).type_string() + ".");
                }
	}

	@Override
	public IType or(IType that) throws TypeError {
                if (return_type().m_type == ((StaticType) that).m_type && 
		    return_type().m_type == T.BOOL) {
                        return return_type();
                } else {
                        throw new TypeError("Cannot compute " + return_type().type_string() 
                                + " or " + ((StaticType) that).type_string() + ".");
                }
	}

	@Override
	public IType not() throws TypeError {
		if (return_type().m_type == T.BOOL) {
                        return return_type();
                } else {
                        throw new TypeError("Cannot negate " + return_type().type_string() + ".");
                }
	}

	@Override
	public IType compare(IType that) throws TypeError {
		if (return_type().m_type == ((StaticType) that).m_type && 
		    (return_type().m_type == T.INT || return_type().m_type == T.FLOAT)) {
                        return return_type();
                } else {
                        throw new TypeError("Cannot compare " + return_type().type_string() + " with " + ((StaticType) that).type_string() + ".");
                }
	}

	@Override
	public IType deref() {
		return return_type();
	}

	@Override
	public IType index(IType that) {
		return ((StaticType) that).m_type == T.INT ? return_type().m_sub_type : null;
	}

	@Override
	public IType call(IType args, String func_name) throws TypeError {
                boolean is_compatible = true;
		StaticType t = (StaticType) args;
		if (t.m_type != T.ARGS || t.m_types.size() != m_types.size()) {
			is_compatible = false;
                } else {
                        for (int i = 0; i < t.m_types.size(); i ++) {
                                if (m_types.get(i).return_type().m_type != t.m_types.get(i).return_type().m_type) {
                                        is_compatible = false;
                                        break;
                                }
                        }
                }
                if (is_compatible) {
                        return return_type();
                } else {
                        throw new TypeError("Cannot call " + func_name + type_string() + " using " + ((StaticType) args).type_string() + ".");
                }
	}
        
        @Override
        public IType ret(IType value, String func_name) throws TypeError {
                if (return_type().m_type == ((StaticType) value).return_type().m_type) {
                        return value;
                } else {
                        throw new TypeError("Function " + func_name + " returns " 
                                + return_type().m_type.toString().toLowerCase() + " not " 
                                + ((StaticType) value).return_type().type_string() + ".");
                }
        }

	@Override
	public IType assign(IType source) throws TypeError {
		if (return_type().m_type == ((StaticType) source).m_type && 
		    (return_type().m_type == T.INT || return_type().m_type == T.FLOAT || return_type().m_type == T.BOOL)) {
                        return return_type();
                } else {
                        throw new TypeError("Cannot assign "
                                        + ((StaticType) source).type_string() + " to Address(" + return_type().type_string() + ")");
                }
	}
        
        @Override
        public void check_entrance() throws TypeError {
                if (m_type != T.FUNC || m_sub_type.m_type != T.VOID || !m_types.isEmpty())
                        throw new TypeError("Function main has invalid signature.");
        }
        
        @Override
        public void check_decl(String name) throws TypeError {
                switch (m_type) {
                        case FUNC:
                                for (int i = 0; i < m_types.size(); i ++) {
                                        if (m_types.get(i).m_type == T.VOID)
                                                throw new TypeError("Function " + name 
                                                        + " has a void argument in position " + i + ".");
                                        if (m_types.get(i).m_unknown != null)
                                                throw new TypeError("Function " + name 
                                                        + " has an error in argument in position " + i + ": Unknown type: "
                                                        + m_types.get(i).m_unknown);
                                }
                                break;
                        case ARRAY:
                                break;
                        case VOID:
                                if (m_type == T.VOID)
                                        throw new TypeError("Variable " + name + " has invalid type void.");
                                break;
                }
        }
}
