package squiddo

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  val run = Http4sscala3Server.run[IO]
