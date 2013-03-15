package com.os.util

import org.apache.hadoop.hbase.util.Bytes
import com.os.measurement.TimedValue
import collection.mutable.ListBuffer
import com.os.interpolation.{NQueueImpl, NQueue}

/**
 * @author Vadim Bobrov
 */
object BytesWrapper {
	val empty = new BytesWrapper(Array.empty[Byte])

	def apply(bytes: Array[Byte] = Array.empty[Byte]): BytesWrapper = new BytesWrapper(bytes)

	implicit def pimpBytes(in: Array[Byte]): BytesWrapper = new BytesWrapper(in)

	implicit def pimpBytes(in: String): BytesWrapper = new BytesWrapper(Bytes.toBytes(in))
	implicit def pimpBytes(in: Long): BytesWrapper = new BytesWrapper(Bytes.toBytes(in))
	implicit def pimpBytes(in: Double): BytesWrapper = new BytesWrapper(Bytes.toBytes(in))
	implicit def pimpBytes(in: NQueue): BytesWrapper = new BytesWrapper << in
	implicit def pimpBytes(in: TimedValue): BytesWrapper = new BytesWrapper(Bytes.add(Bytes.toBytes(in.timestamp()), Bytes.toBytes(in.value())))

	implicit def stringToBytes(in: String): Array[Byte] = Bytes.toBytes(in)
	implicit def longToBytes(in: Long): Array[Byte] = Bytes.toBytes(in)
	implicit def doubleToBytes(in: Double): Array[Byte] = Bytes.toBytes(in)

	implicit def nQueueToBytes(in: NQueue): Array[Byte] = {
		if (in.content.isEmpty)
			Array.empty[Byte]
		else
			in.content map(BytesWrapper.pimpBytes(_)) reduce(_ << _)
	}

	implicit def timedValueToBytes(in: TimedValue): Array[Byte] = Bytes.add(Bytes.toBytes(in.timestamp()), Bytes.toBytes(in.value()))

	implicit def bytesToNQueue(in: Array[Byte]): NQueue = new BytesWrapper(in)

	implicit def bytesWrapperToNQueue(in: BytesWrapper): NQueue = {
		val queue = new NQueueImpl
		in.extractTimedValues foreach (queue offer _)
		queue
	}

	implicit def bytesWrapperToTimedValues(in: BytesWrapper): Traversable[TimedValue] = in.extractTimedValues

	implicit def bytesToString(in: Array[Byte]): String = Bytes.toString(in)
	implicit def bytesToLong(in: Array[Byte]): Long = Bytes.toLong(in)
	implicit def bytesToDouble(in: Array[Byte]): Double = Bytes.toDouble(in)

	implicit def wrapperToBytes(wrapper: BytesWrapper): Array[Byte] = wrapper.bytes
}

class BytesWrapper(val bytes: Array[Byte] = Array.empty[Byte]) {

	def <<(in: Byte): Array[Byte] = {
		Bytes.add(bytes, Array[Byte](in))
	}

	def <<(in: Array[Byte]): Array[Byte] = {
		Bytes.add(bytes, in)
	}

	def <<(in: BytesWrapper): Array[Byte] = {
		Bytes.add(bytes, in)
	}

	def <<(in: String): Array[Byte] = {
		Bytes.add(bytes, Bytes.toBytes(in))
	}

	def <<(in: Long): Array[Byte] = {
		Bytes.add(bytes, Bytes.toBytes(in))
	}

	def <<(in: Double): Array[Byte] = {
		Bytes.add(bytes, Bytes.toBytes(in))
	}

	def <<(in: TimedValue): Array[Byte] = {
		Bytes.add(bytes, Bytes.add(Bytes.toBytes(in.timestamp()), Bytes.toBytes(in.value())))
	}

	def <<(in: NQueue): Array[Byte] = {
		Bytes.add(bytes, BytesWrapper.nQueueToBytes(in))
	}

	def extractTimedValues: Traversable[TimedValue] = {

		val output = ListBuffer[TimedValue]()

		for ( pos <- 0 until bytes.length by Bytes.SIZEOF_LONG + Bytes.SIZEOF_DOUBLE)
			output += new TimedValue(Bytes.toLong(bytes, pos), Bytes.toDouble(bytes, pos + Bytes.SIZEOF_LONG))

		output.toList
	}

}
