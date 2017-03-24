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
		INT,
		FLOAT,
		BOOL,
		VOID,
		ARRAY,
		FUNC,
		ARGS,
	};

	private final T			m_type;
	private final StaticType 	m_sub_type;
	private final List<StaticType>	m_types;

	public StaticType(T type) {
		m_type = type;
		m_sub_type = null;
		m_types = new ArrayList<>();
	}

	public StaticType(StaticType sub_type) {
		m_type = T.ARRAY;
		m_sub_type = sub_type;
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

	@Override
	public IType add(IType that) {
		return return_type().m_type == ((StaticType) that).m_type && 
			(return_type().m_type == T.INT || return_type().m_type == T.FLOAT) ? return_type() : null;
	}

	@Override
	public IType sub(IType that) {
		return return_type().m_type == ((StaticType) that).m_type && 
			(return_type().m_type == T.INT || return_type().m_type == T.FLOAT) ? return_type() : null;
	}

	@Override
	public IType mul(IType that) {
		return return_type().m_type == ((StaticType) that).m_type && 
			(return_type().m_type == T.INT || return_type().m_type == T.FLOAT) ? return_type() : null;
	}

	@Override
	public IType div(IType that) {
		return return_type().m_type == ((StaticType) that).m_type && 
			(return_type().m_type == T.INT || return_type().m_type == T.FLOAT) ? return_type() : null;
	}

	@Override
	public IType and(IType that) {
		return return_type().m_type == ((StaticType) that).m_type && 
		       return_type().m_type == T.BOOL ? return_type() : null;
	}

	@Override
	public IType or(IType that) {
		return return_type().m_type == ((StaticType) that).m_type && 
		       return_type().m_type == T.BOOL ? return_type() : null;
	}

	@Override
	public IType not() {
		return return_type().m_type == T.BOOL ? return_type() : null;
	}

	@Override
	public IType compare(IType that) {
		return return_type().m_type == ((StaticType) that).m_type && 
			(return_type().m_type == T.INT || return_type().m_type == T.FLOAT) ? return_type() : null;
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
	public IType call(IType args) {
		StaticType t = (StaticType) args;
		if (t.m_type != T.ARGS || t.m_types.size() != m_types.size())
			return null;
		for (int i = 0; i < t.m_types.size(); i ++) {
			if (m_types.get(i).return_type().m_type != t.m_types.get(i).return_type().m_type)
				return null;
		}
		return return_type();
	}
        
        @Override
        public IType ret(IType value) {
                return return_type().m_type == ((StaticType) value).return_type().m_type ? value : null;
        }

	@Override
	public IType assign(IType source) {
		return return_type().m_type == ((StaticType) source).m_type && 
			(return_type().m_type == T.INT || return_type().m_type == T.FLOAT || return_type().m_type == T.BOOL) ? return_type() : null;
	}
}
