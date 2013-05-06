package cc.twittertools.post;

import java.util.Map;

/**
 * An immuatable tuple
 * @author bfeeney
 *
 */
public final class Pair<L,R> implements Map.Entry<L, R>
{ private final L left;
  private final R right;
  
  
  
  public Pair(L left, R right) {
    super();
    this.left = left;
    this.right = right;
  }
  
  /**
   * Convenience method to allow pairs to be created without specifying generic
   * types
   */
  public final static <K, V> Pair<K, V> of (K left, V right)
  { return new Pair<K, V>(left, right);
  }
  
  @Override
  public String toString() {
    return left + " \t-\t " + right;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((left == null) ? 0 : left.hashCode());
    result = prime * result + ((right == null) ? 0 : right.hashCode());
    return result;
  }



  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Pair<?,?> other = (Pair<?,?>) obj;
    if (left == null) {
      if (other.left != null)
        return false;
    } else if (!left.equals(other.left))
      return false;
    if (right == null) {
      if (other.right != null)
        return false;
    } else if (!right.equals(other.right))
      return false;
    return true;
  }

  public L getLeft() {
    return left;
  }

  @Override
  public L getKey() {
    return left;
  }
  
  public R getRight() {
    return right;
  }

  @Override
  public R getValue() {
    return right;
  }

  /**
   * Not supported. Pairs are immutable.
   * @throws UnsupportedOperationException as this is an <strong>immutable</strong>
   * pair.
   */
  @Override
  public R setValue(R newValue) {
    throw new UnsupportedOperationException("Pairs are immutable, values cannot be changed");
  }

}
