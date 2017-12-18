package ch.rethinc.reply.core

data class Coordinate(val x: Int, val y: Int)

data class FeedbackItem(val number: Int, val message: String, val coordinate: Coordinate)
