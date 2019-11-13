package com.ibm.opl.customdatasource.sql;

/**
 * Iterates over a string with the ability to look ahead one char
 * @author kong
 *
 */
public class LookAheadStringIterator {
  String _s;
  int _len;
  int _index;
  
  /**
   * Construcs a new LookAheadStringIterator.
   * @param s The string to iterate over.
   */
  public LookAheadStringIterator(String s) {
    _s = s;
    _index = 0;
    if (s != null)
      _len = s.length();
    else
      _len = 0;
  }
 
  /**
   * @return The current char in the iterator.
   */
  int currentChar() {
    if (_index < _len)
      return _s.charAt(_index);
    else
      return -1;
  }
  
  /**
   * 
   * @return The next char in the iterator.
   */
  int nextChar() {
    if ((_index+1) < _len)
      return _s.charAt(_index+1);
    else
      return -1;
  }
  
  /**
   * Advance to the next char.
   */
  void next() {
    if (_index < _len)
      _index++;
  }
  
  /**
   * @return true if there are more chars available on this iterator.
   */
  boolean available() {
    return _index < _len;
  }

  /**
   * Extracts the next java identifier available from the string. The identifier starts at the current char.
   * 
   * After the identifier is extracted, nextChar() returns the first char after the identifier.
   * @return The next java identifier available.
   */
  public String extractIdentifierName() {
    int i = _index;
    while (i < _len && Character.isJavaIdentifierPart(_s.charAt(i))) {
      i++;
    }
    String name = _s.substring(_index, i);
    _index = i-1;
    return name;
  }
  
  /**
   * Swallows all chars until endChar appears in the stream
   * @param buffer The StringBuffer to append swallowed chars
   * @param endChar
   */
  void swallow(StringBuffer buffer, char endChar) {
    while (available()) {
      int c = currentChar();
      buffer.append((char)c);  // append the char in every case
      if ((char)c != endChar) {
        // nothing to do, char already swallowed
      } else if ((char)nextChar() == c) {
        // case where ' or " is escaped (so '' or "")
        next();
        buffer.append((char)c);
      } else {
        break;
      }
      next();
    }
  }
}
