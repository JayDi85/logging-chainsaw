/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.log4j.chainsaw.logevents.ChainsawLoggingEvent;

/**
 * A Rule class implementing logical not.
 *
 * @author Scott Deboy (sdeboy@apache.org)
 */
public class NotRule extends AbstractRule {
    /**
     * Serialization ID.
     */
  static final long serialVersionUID = -6827159473117969306L;
    /**
     * Enclosed rule.
     */
  private final Rule rule;

    /**
     * Create new instance.
     * @param rule enclosed rule.
     */
  private NotRule(final Rule rule) {
    super();
    this.rule = rule;
  }

    /**
     * Create new instance.
     * @param rule enclosed rule.
     * @return new rule.
     */
  public static Rule getRule(final Rule rule) {
      return new NotRule(rule);
  }

    /**
     * Create new instance from top element of stack.
     * @param stack stack
     * @return new rule.
     */
  public static Rule getRule(final Stack stack) {
      if (stack.size() < 1) {
          throw new IllegalArgumentException(
                  "Invalid NOT rule - expected one rule but received "
                          + stack.size());
      }
      Object o1 = stack.pop();
      if (o1 instanceof Rule) {
        Rule p1 = (Rule) o1;
        return new NotRule(p1);
      }
      throw new IllegalArgumentException(
              "Invalid NOT rule: - expected rule but received " + o1);
  }

    /** {@inheritDoc} */
  public boolean evaluate(final ChainsawLoggingEvent event, Map matches) {
    if (matches == null) {
      return !(rule.evaluate(event, null));
    }
    Map tempMatches = new HashMap();
    boolean result = !(rule.evaluate(event, tempMatches));
    if (result) {
        for (Iterator iter = tempMatches.entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            Object key = entry.getKey();
            Set value = (Set)entry.getValue();
            Set mainSet = (Set) matches.get(key);
            if (mainSet == null) {
                mainSet = new HashSet();
                matches.put(key, mainSet);
            }
            mainSet.addAll(value);
        }
    }
    return result;
  }
}
