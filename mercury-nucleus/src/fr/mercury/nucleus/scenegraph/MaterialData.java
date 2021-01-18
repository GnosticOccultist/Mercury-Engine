package fr.mercury.nucleus.scenegraph;

import fr.alchemy.utilities.Validator;

public class MaterialData {

    /**
     * The name of the material data.
     */
    final String name;
    /**
     * The internal data.
     */
    volatile Object value;

    MaterialData(String name, Object value) {
        Validator.nonEmpty(name, "The name can't be empty or null!");
        Validator.nonNull(value, "The material data value can't be null!");
        this.name = name;
        this.value = value;
    }

    public Object value() {
        return value;
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
}
