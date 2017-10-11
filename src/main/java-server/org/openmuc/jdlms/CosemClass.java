/**
 * Copyright 2012-17 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.jdlms;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * The {@link CosemClass} annotation tells jDLMS stack that the annotated class is a COSEM class.
 * </p>
 * 
 * <pre>
 * &#64;{@link CosemClass}(id = 1, version = 0)
 * public class Data extends {@linkplain CosemInterfaceObject} {
 *      ...
 * 
 *      public Data({@linkplain ObisCode} instanceId) {
 *          super(instanceId)
 *      }
 * 
 * }
 * </pre>
 * 
 * @see CosemMethod
 * @see CosemAttribute
 * @see CosemInterfaceObject
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CosemClass {

    /**
     * The COSEM class ID. Value greater than zero.
     * 
     * @return the class ID.
     */
    int id();

    /**
     * The version of the COSEM class.
     * 
     * <p>
     * The version must be a positive integer.
     * </p>
     * 
     * @return the version.
     */
    int version() default 0;
}
