package shared;

public interface SharedObject {
  Object clone();
  boolean equivalent(SharedObject o);
  int hashCode();
}
