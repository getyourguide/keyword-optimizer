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
 * This class represents a general expression in the syntax tree for the formula grammar.
 */
public class ASTExpression extends SimpleNode {

  public ASTExpression(int id) {
    super(id);
  }

  @Override
  public double calculateScore(FormulaContext context) throws FormulaException {
    // This should never happen as per grammar.
    if (children.length != 1) {
      throw new FormulaException("Invalid formula: " + toString());
    }
  
    return ((SimpleNode) children[0]).calculateScore(context);
  }

  @Override
  public Object jjtAccept(FormulaParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();
    for (Node node : children) {
      out.append(node.toString());
    }
    return out.toString();
  }
}
