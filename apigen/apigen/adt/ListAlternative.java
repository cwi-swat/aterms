package apigen.adt;

public class ListAlternative extends Alternative {
	private String typeId;
	private String elementTypeId;

	public ListAlternative(String typeId, String elementTypeId) {
		setTypeId(typeId);
		setElementTypeId(elementTypeId);
	}

	public void setElementTypeId(String elementTypeId) {
		this.elementTypeId = elementTypeId;
	}

	public String getElementTypeId() {
		return elementTypeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getTypeId() {
		return typeId;
	}
    
  public String toString() {
      return "list [" + getTypeId() + "," + getElementTypeId() + "]";
  }
  
  public boolean isList() {
      return true;
  }
}
