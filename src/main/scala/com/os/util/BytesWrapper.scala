package com.os.util

import org.apache.hadoop.hbase.util.Bytes
import com.os.measurement.TimedValue

/**
 * @author Vadim Bobrov
 */
object BytesWrapper {
	val empty = new BytesWrapper(Array.empty[Byte])

	implicit def pimpBytes(in: Array[Byte]): BytesWrapper = new BytesWrapper(in)
	implicit def pimpBytes(in: String): BytesWrapper = new BytesWrapper(Bytes.toBytes(in))
	implicit def pimpBytes(in: Long): BytesWrapper = new BytesWrapper(Bytes.toBytes(in))
	implicit def pimpBytes(in: Double): BytesWrapper = new BytesWrapper(Bytes.toBytes(in))
	implicit def pimpBytes(in: TimedValue): BytesWrapper = new BytesWrapper(Bytes.add(Bytes.toBytes(in.timestamp()), Bytes.toBytes(in.value())))


	implicit def wrapperToBytes(wrapper: BytesWrapper): Array[Byte] = wrapper.bytes
}

class BytesWrapper(val bytes: Array[Byte]) {

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

}
