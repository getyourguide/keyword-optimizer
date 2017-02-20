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
 * This class represents a multiplication operation in the syntax tree for the formula grammar.
 */
public class ASTMult extends SimpleNode {

  private static final String DIVIDE = "/";
  private static final String MULTIPLY = "*";
  
  private String operator;

  public ASTMult(int id) {
    super(id);
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  @Override
  public double calculateScore(FormulaContext context) throws FormulaException {
    if (MULTIPLY.equals(operator)) {
      // If the operator is *, then multiply all children.
      double result = 1;
      for (Node child : children) {
        if (child instanceof SimpleNode) {
          SimpleNode childCasted = (SimpleNode) child;
          result *= childCasted.calculateScore(context);
        } else {
          throw new FormulaException("Unknown grammar node found: " + child.getClass());
        }
      }
      return result;
      
    } else if (DIVIDE.equals(operator)) {
      // If the operator is /, then find first child and divide by the following ones.
      double result = Double.NaN;
      boolean firstChild = true;
      for (Node child : children) {
        if (child instanceof SimpleNode) {
          SimpleNode childCasted = (SimpleNode) child;
  
          if (firstChild) {
            result = childCasted.calculateScore(context);
            firstChild = false;
          } else {
            result /= childCasted.calculateScore(context);
          }
        } else {
          throw new FormulaException("Unknown grammar node found: " + child.getClass());
        }
      }
      if (Double.isInfinite(result)) {
        return Double.NaN;
      }
  
      return result;
    } else {
      // This should never happen, as per grammar only * and / area allowed here.
      throw new FormulaException("Unknown operator: " + operator);
    }
  }

  @Override
  public Object jjtAccept(FormulaParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();
    boolean first = true;
    for (Node child : children) {
      if (!first) {
        out.append(operator);
      }
      out.append(child.toString());
      first = false;
    }
    return out.toString();
  }
}
