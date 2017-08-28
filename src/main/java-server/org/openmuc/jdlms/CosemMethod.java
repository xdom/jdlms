package org.openmuc.jdlms;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

/**
 * This annotation is used to define a COSEM method in a COSEM class.
 * 
 * <p>
 * The annotate method must be public. It can have a single parameter of type {@link DataObject}, it can return a
 * {@link DataObject} and it can throw an {@link IllegalMethodAccessException}.
 * </p>
 * 
 * <pre>
 * &#64;{@link CosemClass}(id = 99, version = 2)
 * public class DemoClass extends {@linkplain CosemInterfaceObject} {
 *      ...
 *      &#64;{@link CosemMethod}(id = 1)
 *      public void foo() {
 *          System.out.println("Hello World");
 *      }
 * 
 *      &#64;{@link CosemMethod}(id = 2, consumes = Type.OCTET_STRING)
 *      public void bar(DataObject param) {
 *          byte[] octetStr = param.getValue();
 *          System.out.println("Hello " + new String(octetStr, StandardCharsets.US_ASCII));
 *      }
 * 
 *      &#64;{@link CosemMethod}(id = 3, consumes = Type.OCTET_STRING)
 *      public DataObject fooBar(DataObject param, Long connectionId) {
 *          // evaluate connection ID
 * 
 *          byte[] octetStr = param.getValue();
 *          System.out.println("Hello " + new String(octetStr));
 * 
 *          return DataObject.newNullData();
 *      }
 * }
 * </pre>
 * 
 * <p>
 * NOTE: Ambiguous method ID's in a class are not allowed.
 * </p>
 * 
 * @see CosemClass
 * @see DataObject
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CosemMethod {

    /**
     * The method ID. The value must be greater than 0.
     * 
     * @return the method ID &gt; 0.
     */
    byte id();

    /**
     * The type of the parameter if it exsits.
     * 
     * @return the parameter type.
     */
    Type consumes() default Type.DONT_CARE;

    /**
     * Restrict the access mode of the method.
     * 
     * @return the access mode of the method.
     */
    MethodAccessMode accessMode() default MethodAccessMode.ACCESS;
}
