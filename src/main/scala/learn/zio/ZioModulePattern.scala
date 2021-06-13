package learn.zio

import zio._
import zio.magic._


object ZioModulePattern extends App {

  // ServiceA
  trait ServiceA {
    def foo(): ZIO[Any, Nothing, String]
  }

  object ServiceA {
    def foo(): ZIO[Has[ServiceA], Nothing, String] = ZIO.accessM(_.get.foo())

    val live: ZLayer[Has[ServiceB], Nothing, Has[ServiceA]] =
      (ServiceALive.apply _).toLayer

    val test: ZLayer[Any, Nothing, Has[ServiceA]] =
      (ServiceATest.apply _).toLayer
  }

  case class ServiceALive(serviceB: ServiceB) extends ServiceA {
    override def foo(): ZIO[Any, Nothing, String] = for {
      a <- ZIO.succeed("ServiceA - Live")
      b <- serviceB.bar()
    } yield (s"$a ... $b")
  }

  case class ServiceATest() extends ServiceA {
    override def foo(): ZIO[Any, Nothing, String] =
      ZIO.succeed("ServiceA - Test")
  }

  // ServiceB
  trait ServiceB {
    def bar(): ZIO[Any, Nothing, String]
  }

  object ServiceB {
    def bar(): ZIO[Has[ServiceB], Nothing, String] = ZIO.accessM(_.get.bar())

    val live: ZLayer[Any, Nothing, Has[ServiceB]] =
      (ServiceBLive.apply _).toLayer

    val test: ZLayer[Any, Nothing, Has[ServiceB]] =
      (ServiceBTest.apply _).toLayer
  }

  case class ServiceBLive() extends ServiceB {
    override def bar(): ZIO[Any, Nothing, String] =
      ZIO.succeed("ServiceB - Live")
  }

  case class ServiceBTest() extends ServiceB {
    override def bar(): ZIO[Any, Nothing, String] =
      ZIO.succeed("ServiceB - Test")
  }

  // ServiceC
  trait ServiceC {
    def baz(): ZIO[Any, Nothing, String]
  }

  object ServiceC {
    def baz(): ZIO[Has[ServiceC], Nothing, String] = ZIO.accessM(_.get.baz())

    val live: ZLayer[Any, Nothing, Has[ServiceC]] =
      (ServiceCLive.apply _).toLayer

    val test: ZLayer[Any, Nothing, Has[ServiceC]] =
      (ServiceCTest.apply _).toLayer
  }

  case class ServiceCLive() extends ServiceC {
    override def baz(): ZIO[Any, Nothing, String] =
      ZIO.succeed("ServiceC - Live")
  }

  case class ServiceCTest() extends ServiceC {
    override def baz(): ZIO[Any, Nothing, String] =
      ZIO.succeed("ServiceC - Test")
  }

  // logic
  val logic = for {
    a <- ServiceA.foo()
    b <- ServiceB.bar()
    k <- ServiceC.baz()
  } yield (a)

  // dependency injection 1.0
  val aLayer       = ServiceB.live >>> ServiceA.live
  val bLayer       = ServiceB.live
  val cLayer       = ServiceC.live
  val fullLayer    = aLayer ++ bLayer ++ cLayer
  val liveManually = logic.provideCustomLayer(fullLayer)

  // dependency injection with 2.0 (zio-magic)
  val live = logic.inject(
    ServiceA.live,
    ServiceB.live,
    ServiceC.live
  )

  val test = logic.inject(
    ServiceA.test,
    ServiceB.test,
    ServiceC.test
  )

  val program = for {
    lm <- liveManually
    l  <- live
    t  <- test
  } yield ()

  def run(args: List[String]) = program.exitCode

}
