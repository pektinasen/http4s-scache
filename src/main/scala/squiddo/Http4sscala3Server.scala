package squiddo

import cats.effect.Async
import cats.syntax.all.*
import cats.effect.syntax.all.*
import cats.effect.implicits.*
import com.evolutiongaming.catshelper.CatsHelper.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger
import com.evolution.scache
import cats.Parallel
import com.evolution.scache.ExpiringCache
import scala.concurrent.duration.* 
object Http4sscala3Server:

  def run[F[_]: Async: Network: Parallel: cats.effect.std.Console]: F[Nothing] = {

    val  cache = scache.Cache.expiring[F, String, String](
      scache.ExpiringCache.Config[F, String, String](expireAfterRead = 1.minute), partitions = None,
    )

    for {
      client <- EmberClientBuilder.default[F].build
      cache <- cache
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg = Jokes.impl[F](client)


      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract segments not checked
      // in the underlying routes.
      httpApp = (
        Http4sscala3Routes.helloWorldRoutes[F](helloWorldAlg, cache)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      _ <- 
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
