package fr.mercury.nucleus.scenegraph;

import fr.alchemy.utilities.Validator;

public class MaterialVariable {

    /**
     * The name of the material data.
     */
    final String name;
    /**
     * The internal data.
     */
    volatile Object value;
    
    ValueType valueType;
    
    MaterialVariable(String name, Object value) {
        this(name, value, ValueType.UNIFORM_VALUE);
    }

    MaterialVariable(String name, Object value, ValueType valueType) {
        Validator.nonEmpty(name, "The name can't be empty or null!");
        this.name = name;
        this.value = value;
        this.valueType = valueType;
    }

    public Object value() {
        return value;
    }

    public String getName() {
        return name;
    }

    public ValueType getValueType() {
        return valueType;
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name= " + name + ", value= " + value + "]";
    }
    
    public enum ValueType {
        
        UNIFORM_VALUE,
        
        RENDERER_MATRIX,
        
        PREFAB_UNIFORMS;
    }
}
