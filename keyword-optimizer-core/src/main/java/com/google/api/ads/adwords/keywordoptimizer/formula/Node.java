// Copyright 2017 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.adwords.keywordoptimizer.formula;

/**
 * Base class for nodes in the abstract syntax tree (AST) representing the formula grammar.
 */
public interface Node {

  /**
   * This method is called after the node has been made the current node. It indicates that child
   * nodes can now be added to it.
   */
  public void jjtOpen();

  /** 
   * This method is called after all the child nodes have been added.
   */
  public void jjtClose();

  /** 
   * This method is used to inform the node of its parent. 
   */
  public void jjtSetParent(Node n);

  /** 
   * Returns the parent node. 
   */
  public Node jjtGetParent();

  /** 
   * This method tells the node to add its argument to the node's list of children.
   */
  public void jjtAddChild(Node n, int i);

  /** 
   * This method returns a child node. The children are numbered from zero, left to right.
   */
  public Node jjtGetChild(int i);

  /** 
   * Returns the number of children the node has.
   */
  public int jjtGetNumChildren();

  /** 
   * Accepts a visitor. 
   */
  public Object jjtAccept(FormulaParserVisitor visitor, Object data);
}
