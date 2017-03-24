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

import java.util.List;

/**
 * @author davis
 */
public interface IType {
        List<IType> sub_decls();
	IType add(IType that);
	IType sub(IType that);
	IType mul(IType that);
	IType div(IType that);
	IType and(IType that);
	IType or(IType that);
	IType not();
	IType compare(IType that);
	IType deref();
	IType index(IType that);
	IType call(IType args);
	IType assign(IType source);
        IType ret(IType value);
}
