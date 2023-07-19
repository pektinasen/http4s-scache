package squiddo


import cats.effect.Sync
import cats.effect.std.Console
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import com.evolution.scache.Cache
import cats.effect.IO

object Http4sscala3Routes:

  def helloWorldRoutes[F[_]: Sync: Console](H: HelloWorld[F], cache: Cache[F, String, String]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F]{}
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          values <- cache.values
          _ <- cache.put(name, "foo")
          _ <- values.iterator.toList.traverse((k, fv) => fv.flatMap(v => Console[F].println(s"$k: $v")))
          greeting <- H.hello(HelloWorld.Name(name))
          resp <- Ok(greeting)
        } yield resp
    }
