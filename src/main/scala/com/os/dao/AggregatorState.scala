package com.os.dao

import com.os.interpolation.NQueue

/**
 * @author Vadim Bobrov
 */
class AggregatorState(val customer:String, val location: String, val interpolatorStates: Traversable[(String, NQueue)]) extends Saveable
