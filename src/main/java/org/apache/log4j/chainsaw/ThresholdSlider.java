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

package org.apache.log4j.chainsaw;

import org.apache.log4j.chainsaw.logevents.Level;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;


/**
 * A Slider implementation that allows a user to
 * choose a particular Threshold
 * .
 *
 * @author Paul Smith &lt;psmith@apache.org&gt;
 */
final class ThresholdSlider extends JSlider {
    final List priorityList;

    ThresholdSlider() {
        Level[] levels =
            new Level[]{
                Level.OFF, Level.FATAL, Level.ERROR, Level.WARN, Level.INFO,
                Level.DEBUG, Level.TRACE, Level.ALL
            };

        priorityList = Arrays.asList(levels);
        Collections.sort(priorityList);

        setModel(
            new DefaultBoundedRangeModel(
                priorityList.indexOf(Level.TRACE), 0, 0, priorityList.size() - 1));

        Hashtable<Integer, JLabel> labelMap = new Hashtable<>();

        for (Object aPriorityList : priorityList) {
            Level item = (Level) aPriorityList;
            labelMap.put(
                priorityList.indexOf(item), new JLabel(item.toString()));

            //      System.out.println("creating levels for :: " + item.toInt() + "," + item.toString());
        }

        setOrientation(SwingConstants.VERTICAL);
        setInverted(true);
        setLabelTable(labelMap);

        setPaintLabels(true);

        //    setPaintTicks(true);
        setSnapToTicks(true);

        //    setMajorTickSpacing(10000);
        //    setPaintTrack(true);
    }

    void setChosenLevel(Level level) {
        setValue(priorityList.indexOf(level));
    }

    /**
     * Returns the Log4j Level that is currently selected in this slider
     *
     * @return
     */
    Level getSelectedLevel() {
        Level level = (Level) priorityList.get(getValue());

        if (level == null) {
            level = Level.TRACE;
        }

        return level;
    }
}
