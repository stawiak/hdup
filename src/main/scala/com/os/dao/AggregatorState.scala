package com.os.dao

import com.os.interpolation.NQueue
import write.Saveable

/**
 * @author Vadim Bobrov
 */
class TimeWindowState(val aggs: Traversable[AggregatorState]) extends Saveable
class AggregatorState(val customer:String, val location: String, val interpolatorStates: Traversable[(String, NQueue)]) extends Saveable
