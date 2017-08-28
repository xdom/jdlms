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
