package aterm;

public interface ATermVisitor
{
  public boolean visitInt(ATermInt arg);
  public boolean visitReal(ATermReal arg);
  public boolean visitAppl(ATermAppl arg);
  public boolean visitList(ATermList arg);
  public boolean visitPlaceholder(ATermPlaceholder arg);
  public boolean visitBlob(ATermBlob arg);
}
