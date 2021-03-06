package scott.barleydb.build.specification.vendor;

/*
 * #%L
 * BarleyDB
 * %%
 * Copyright (C) 2014 Scott Sinclair <scottysinclair@gmail.com>
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import scott.barleydb.api.core.types.JavaType;
import scott.barleydb.api.core.types.JdbcType;
import scott.barleydb.api.specification.DefinitionsSpec;
import scott.barleydb.api.specification.EntitySpec;
import scott.barleydb.api.specification.NodeSpec;
import scott.barleydb.server.jdbc.converter.LongToStringTimestampConverter;

public class MySqlSpecConverter {

	/**
	 * Converts the given spec to work with MySql DB.
	 * @param definitionsSpec
	 * @return
	 */
	public static DefinitionsSpec convertSpec(DefinitionsSpec definitionsSpec) {
		for (EntitySpec entitySpec: definitionsSpec.getEntitySpecs()) {
			for (NodeSpec nodeSpec: entitySpec.getNodeSpecs()) {
				if (nodeSpec.getJavaType() != JavaType.LONG) {
					continue;
				}
				if (nodeSpec.getJdbcType() != JdbcType.TIMESTAMP) {
					continue;
				}
				nodeSpec.setJdbcType(JdbcType.VARCHAR);
				nodeSpec.setLength(50);
				nodeSpec.setTypeConverter(LongToStringTimestampConverter.class.getName());
			}
		}
		return definitionsSpec;
	}
}
