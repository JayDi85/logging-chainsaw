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

import org.apache.log4j.spi.LoggingEventFieldResolver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.log4j.chainsaw.logevents.ChainsawLoggingEvent;


/**
 * A Rule class which returns the result of
 * performing equals against two strings.
 *
 * @author Scott Deboy (sdeboy@apache.org)
 */
public class EqualsRule extends AbstractRule {
    /**
     * Serialization ID.
     */
  static final long serialVersionUID = 1712851553477517245L;
    /**
     * Resolver.
     */
  private static final LoggingEventFieldResolver RESOLVER =
    LoggingEventFieldResolver.getInstance();
    /**
     * Value.
     */
  private final String value;
    /**
     * Field.
     */
  private final String field;

    /**
     * Create new instance.
     * @param field field
     * @param value value
     */
  private EqualsRule(final String field, final String value) {
    super();
    if (!RESOLVER.isField(field)) {
      throw new IllegalArgumentException(
        "Invalid EQUALS rule - " + field + " is not a supported field");
    }

    this.field = field;
    this.value = value;
  }

    /**
     * Create new instance from top two elements of stack.
     * @param stack stack
     * @return new instance
     */
  public static Rule getRule(final Stack stack) {
    if (stack.size() < 2) {
      throw new IllegalArgumentException(
        "Invalid EQUALS rule - expected two parameters but received "
        + stack.size());
    }

    String p2 = stack.pop().toString();
    String p1 = stack.pop().toString();

    return getRule(p1, p2);
  }

    /**
     * Create new instance.
     * @param p1 field, special treatment for level and timestamp.
     * @param p2 value
     * @return new instance
     */
  public static Rule getRule(final String p1, final String p2) {
    if (p1.equalsIgnoreCase(LoggingEventFieldResolver.LEVEL_FIELD)) {
        return LevelEqualsRule.getRule(p2);
    } else if (p1.equalsIgnoreCase(LoggingEventFieldResolver.TIMESTAMP_FIELD)) {
        return TimestampEqualsRule.getRule(p2);
    } else {
        return new EqualsRule(p1, p2);
    }
  }

    /** {@inheritDoc} */
  public boolean evaluate(final ChainsawLoggingEvent event, Map matches) {
    Object p2 = RESOLVER.getValue(field, event);

    boolean result = (p2 != null) && p2.toString().equals(value);
    if (result && matches != null) {
        Set entries = (Set) matches.get(field.toUpperCase());
        if (entries == null) {
            entries = new HashSet();
            matches.put(field.toUpperCase(), entries);
        }
        entries.add(value);
    }
    return result;
  }
}
