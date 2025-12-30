import fs2._

'a'.toInt
'z'.toInt
97.toChar
122.toChar


def letterIter: Stream[Pure, Char] =
  Stream.iterate('a')(c => (c + 1).toChar).take(26)

letterIter.toList


