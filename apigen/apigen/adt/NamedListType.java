package apigen.adt;

import apigen.adt.api.Factory;

public class NamedListType extends ListType {
    private String opName;

    public NamedListType(
        String id,
				String moduleName,
        String elementType,
        String opName,
        Factory factory) {
        super(id, moduleName, elementType, factory);
        this.opName = opName;
        
    }

    public String getOpName() {
        return opName;
    }

}
