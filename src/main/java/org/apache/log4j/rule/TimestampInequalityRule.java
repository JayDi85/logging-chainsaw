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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.chainsaw.logevents.ChainsawLoggingEvent;

import org.apache.log4j.spi.LoggingEventFieldResolver;

/**
 * A Rule class implementing inequality evaluation for timestamps.
 *
 * @author Scott Deboy (sdeboy@apache.org)
 */
public class TimestampInequalityRule extends AbstractRule {
    /**
     * Serialization ID.
     */
  static final long serialVersionUID = -4642641663914789241L;
    /**
     * Resolver.
     */
  private static final LoggingEventFieldResolver RESOLVER =
            LoggingEventFieldResolver.getInstance();
    /**
     * Date format.
     */
  private static final DateFormat DATE_FORMAT =
          new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    /**
     * Inequality symbol.
     */
  private transient String inequalitySymbol;
    /**
     * Timestamp.
     */
  private long timeStamp;

    /**
     * Create new instance.
     * @param inequalitySymbol inequality symbol.
     * @param value string representation of date.
     */
  private TimestampInequalityRule(
    final String inequalitySymbol, final String value) {
    super();
    this.inequalitySymbol = inequalitySymbol;
    try {
        timeStamp = DATE_FORMAT.parse(value).getTime();
    } catch (ParseException pe) {
        throw new IllegalArgumentException("Could not parse date: " + value);
    }
  }

    /**
     * Create new instance.
     * @param inequalitySymbol inequality symbol
     * @param value string representation of date
     * @return new instance
     */
  public static Rule getRule(final String inequalitySymbol,
                             final String value) {
      return new TimestampInequalityRule(inequalitySymbol, value);
  }

    /** {@inheritDoc} */
  public boolean evaluate(final ChainsawLoggingEvent event, Map matches) {
    String eventTimeStampString = RESOLVER.getValue(LoggingEventFieldResolver.TIMESTAMP_FIELD, event).toString();
    long eventTimeStamp = Long.parseLong(
                eventTimeStampString) / 1000 * 1000;
    boolean result = false;
    long first = eventTimeStamp;
    long second = timeStamp;

    if ("<".equals(inequalitySymbol)) {
      result = first < second;
    } else if (">".equals(inequalitySymbol)) {
      result = first > second;
    } else if ("<=".equals(inequalitySymbol)) {
      result = first <= second;
    } else if (">=".equals(inequalitySymbol)) {
      result = first >= second;
    }
    if (result && matches != null) {
        Set entries = (Set) matches.get(LoggingEventFieldResolver.TIMESTAMP_FIELD);
        if (entries == null) {
            entries = new HashSet();
            matches.put(LoggingEventFieldResolver.TIMESTAMP_FIELD, entries);
        }
        entries.add(eventTimeStampString);
    }
    return result;
  }
}
