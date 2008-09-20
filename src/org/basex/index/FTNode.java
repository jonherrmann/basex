package org.basex.index;

import org.basex.util.Array;
import org.basex.util.IntList;

/**
 * XPath Value representing a full-text Node.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTNode {
  /** Fulltext data for a node. */
  private IntList ip = null;
  /** Pre value of the current node. */
  private int pre;
  /** Pointer for idpos - each idpos has a pointer at 
   * its search string position in the xpath query. 
   * poi[0] = max. max pointer value in poi*/
  public IntList p;
  /** Counter for pos values. */
  private int c = 0;
  /** Flag for negative node. */
  public boolean not = false;
  /** List for tokens from query. */
  private FTTokenizer[] tok;
  /** Number of stored values.. */
  public int size;

  /**
   * Constructor.
   */
  public FTNode() {
    ip = new IntList();
    size = 0;
  }
  
  /**
   * Constructor.
   * @param idpos ftdata, pre, pos1, ..., posn
   * @param pointer pointer on query tokens
   */
  public FTNode(final int[] idpos, final int[] pointer) {
    ip = new IntList(idpos);
    p = new IntList(pointer);
    size = idpos.length;
  }
  
  /**
   * Constructor.
   * @param idpos ftdata, pre, pos1, ..., posn
   * @param pointer initial pointer on query token
   */
  FTNode(final int[] idpos, final int pointer) {
    ip = new IntList(idpos);
    genPointer(pointer); 
    size = 1;
  }

  /**
   * Constructor.
   * @param prevalue pre value of the current node
   */
  FTNode(final int prevalue) {
    pre = prevalue;
    ip = new IntList(new int[] { prevalue });
    size = 1;
  }
  
  /**
   * Generates pointer with value v.
   * @param v value
   */
  public void genPointer(final int v) {
    int[] t = new int[ip.size];
    for (int i = 0; i < t.length; i++) t[i] = v;
    p = new IntList(t);
  }
  
  /**
   * Getter for the prevalue.
   * @return pre value
   */
  public int getPre() {
    if (ip != null) return ip.get(0);
    return pre;
  }
  
  /**
   * Test is any pos value is remaining.
   * @return boolean
   */
  public boolean morePos() {
    return ++c < ip.size;
  }
  
  /**
   * Setter for FTTokenizer.
   * @param token FTTokenizer
   */
  public void setToken(final FTTokenizer[] token) {
    tok = token;
  }
  
  /**
   * Getter for the FTTokenizer.
   * @return FTTokenizer
   */
  public FTTokenizer[] getToken() {
    return tok;
  }
  
  /**
   * Get next pos value.
   * @return pos value
   */
  public int nextPos() {
    return ip.get(c);
  }
  
  /**
   * Get number of tokens from query for this node.
   * @return number of tokens
   */
  public int getNumTokens() {
    return p.get(0);
  }
  
  /**
   * Merges n to the current FTNode.
   * Pointer are node updated.
   * @param n ftnode to be merged
   * @param w distance between the pos values 
   * @return boolean 
   */
  public boolean merge(final FTNode n, final int w) {
    if (not != n.not) return false;
    
    boolean mp = morePos();
    boolean nmp = n.morePos();
    if (getPre() != n.getPre() || !(mp && nmp)) return false;
    IntList il = new IntList();
    IntList pn = (p != null) ? initNewPointer(n.p) : null;
    il.add(getPre());
    int d;
    while(mp && nmp) {
      d = nextPos() - n.nextPos() + w;
      if (d == 0) {
        add(this, il, pn);
        if (w > 0) add(n, il, pn);
        mp = morePos();
        nmp = n.morePos();
      } else if (d < 0) {
        if (w == 0) add(this, il, pn);
        mp = morePos();
      } else {
        if (w == 0) add(n, il, pn);
        nmp = n.morePos();
      }
    }
    if (w == 0) while(mp) {
      add(this, il, pn);
      mp = morePos();
    }
    if (w == 0) while(nmp) {
      add(n, il, pn);
      nmp = n.morePos();
    }
    
    ip = new IntList(il.finish());
    if (tok != null) {
      FTTokenizer[] tmp = new FTTokenizer[tok.length + n.tok.length];
      System.arraycopy(tok, 0, tmp, 0, tok.length);
      System.arraycopy(n.tok, 0, tmp, tok.length, n.tok.length);
      tok = tmp;
    }
    p = (p != null) ? new IntList(pn.finish()) : null;
    return ip.size > 1; 
  }
 
  /**
   * Checks if current node and n are a phrase with distance w.
   * @param n second node
   * @param w distance to first node
   * @return boolean phrase
  public boolean phrase(final FTNode n, final int w) {
    boolean mp = morePos();
    boolean nmp = n.morePos();
    if(getPre() != n.getPre() || !(mp && nmp)) return false;
    IntList il = new IntList();
    IntList pn = (p != null) ? initNewPointer(n.p) : null;
    il.add(getPre());
    int d;
    while(mp && nmp) {
      d = nextPos() - n.nextPos() + w;
      if(d == 0) add(this, il, pn);
      if(d <= 0) mp = morePos();
      if(d >= 0) nmp = n.morePos();
    }
    
    ip = il;
    if (tok != null) {
      FTTokenizer[] tmp = new FTTokenizer[tok.length + n.tok.length];
      System.arraycopy(tok, 0, tmp, 0, tok.length);
      System.arraycopy(n.tok, 0, tmp, tok.length, n.tok.length);
      tok = tmp;
    }
    p = (p != null) ? new IntList(pn.finish()) : null;
    return ip.size > 1; 
  }
   */
  
  /**
   * Initialize pointer list.
   * @param n IntList
   * @return IntList
   */
  private IntList initNewPointer(final IntList n) {
    IntList i = new IntList();
    i.add(p.get(0) > n.get(0) ? p.get(0) : n.get(0));
    return i;
  }
  
  /**
   * Add node n to il and ns pointer to il.
   * @param n node to add
   * @param il IntList takes the new node
   * @param pn IntList with pointers
   */
  private void add(final FTNode n, final IntList il, final IntList pn) {
    il.add(n.nextPos());
    if (pn != null) {
      pn.add(n.nextPoi());
    }
  }
  
  /**
   * Converts the pos values in the following style.
   * poi0, pos0, ..., posk
   * poi1, pos0, ..., posj
   * @return IntList[]
   */
  public IntList[] convertPos() {
    IntList[] il = new IntList[p.get(0)];
    for (int k = 0; k < il.length; k++) il[k] = new IntList();
    c = 0;
    while(morePos()) {
      il[nextPoi() - 1].add(nextPos());
      //il[p.get(i++) - 1].add(nextPos());
    }
    return il;
  }
  
  /**
   * Get next pointer.
   * @return next pointer
   */
  public int nextPoi() {
    return p.get(c);
  }

  /**
   * Returns the complete ftnode.
   * @return current ftnode
   */
  public int[] getFTNode() {
    return ip.finish();
  }

  @Override
  public String toString() {
    return "FTNode [Pre=" + getPre() + "; Pos=" 
    + Array.toString(ip.finish(), 0, ip.size)
    + "; Poi=" + Array.toString(p.finish(), 0, p.size) + "]";
  }
}
